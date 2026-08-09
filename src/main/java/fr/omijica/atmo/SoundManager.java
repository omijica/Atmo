package fr.omijica.atmo;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

// Gère les sons d'ambiance, la musique et le blockSound pour chaque joueur
public class SoundManager implements Listener {

    private final Atmo plugin;
    private final Ambient ambient;
    private Map<String, Ambient.AmbientData> ambiances; // liste des ambiances chargées
    private ConfigClass.GeneralAreaData generalArea; // config générale (zone par défaut)
    private final Map<UUID, PlayerMusicState> states = new HashMap<>(); // état son/musique par joueur

    // Données du blockSound : sons liés à des entités détectées, et sons liés à des positions fixes
    private BlockSoundClass.BlockSoundData blockSoundData;
    private Map<String, BlockSoundClass.EntityEntry> blockSounds;
    private Map<String, BlockSoundClass.PositionEntry> positionSounds;

    public SoundManager(Atmo plugin) {
        this.plugin = plugin;
        this.ambient = plugin.getAmbient();
        this.ambiances = Ambient.load(plugin.getDataFolder());
        this.generalArea = ConfigClass.load(plugin.getDataFolder());
        this.blockSoundData = BlockSoundClass.load(plugin.getDataFolder());
        this.blockSounds = blockSoundData.getEntities();
        this.positionSounds = blockSoundData.getPositions();
    }

    // Stocke l'état audio courant d'un joueur
    private static class PlayerMusicState {
        String currentAmbientName; // nom de l'ambiance active
        int backgroundCooldown; // temps avant rejouer le son de fond
        public String playingSound; // son de fond en cours
        Map<String, Integer> randomCooldowns = new HashMap<>(); // cooldowns des sons aléatoires

        String currentMusicName; // nom de la musique active
        int musicCooldown; // temps avant rejouer la musique
        String playingMusicSound; // musique en cours

        // Cooldowns du blockSound, par nom d'entrée (partagé entre "positions" et "blocks")
        Map<String, Integer> positionCooldowns = new HashMap<>(); // cooldowns des "positions"
        Map<String, Integer> entityCooldowns = new HashMap<>();   // cooldowns des "blocks"
    }

    // Lance la boucle qui met à jour les joueurs toutes les 2 secondes (40 ticks)
    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 40L);
    }

    // Appelée à chaque tick de la boucle, traite tous les joueurs en ligne
    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            handlePlayer(player);
        }
    }

    // Recharge la config et coupe tous les sons en cours
    public void reload() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerMusicState state = states.get(player.getUniqueId());
            stopAll(player, state);
        }
        states.clear();
        this.ambiances = Ambient.load(plugin.getDataFolder());
        this.generalArea = ConfigClass.load(plugin.getDataFolder());

        // appel statique direct, aligné sur le constructeur (au lieu de passer par une instance)
        this.blockSoundData = BlockSoundClass.load(plugin.getDataFolder());
        this.blockSounds = blockSoundData.getEntities();
        this.positionSounds = blockSoundData.getPositions();
    }

    public void stop() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerMusicState state = states.get(player.getUniqueId());
            stopAll(player, state);
        }
    }

    // Gère l'ambiance, la musique et le blockSound d'un joueur selon sa zone
    private void handlePlayer(Player player) {
        ZoneClass zone = plugin.getZoneChecker().getPlayerZone(player);
        UUID uuid = player.getUniqueId();
        PlayerMusicState state = states.computeIfAbsent(uuid, k -> new PlayerMusicState());

        // Le blockSound est indépendant de la zone : une radio doit s'entendre partout
        handleBlockSounds(player, state);

        // Gestion de la musique
        if (isMusicEnabledEffective(zone)) {
            handleMusic(player, zone, state);
        } else {
            stopMusic(player, state);
        }

        // Gestion de l'ambiance sonore
        if (isAmbientEnabledEffective(zone)) {
            String ambientName = getAmbientNameEffective(zone);

            // Si l'ambiance a changé, on coupe l'ancien son
            if (!Objects.equals(state.currentAmbientName, ambientName)) {

                if (state.playingSound != null) {
                    player.stopSound(state.playingSound);
                    state.playingSound = null;
                }
                state.currentAmbientName = ambientName;
                state.backgroundCooldown = 0;
                state.randomCooldowns.clear();
            }

            Ambient.AmbientData data = ambiances.get(ambientName);
            if (data == null) {
                return; // ambiance inconnue, on arrête ici
            }

            state.backgroundCooldown -= 2; // on décompte le temps (2 secondes par tick)

            // Rejoue le son de fond quand le cooldown est fini
            if (state.backgroundCooldown <= 0) {
                if (state.playingSound != null) {
                    player.stopSound(state.playingSound);
                }

                player.playSound(player.getLocation(), data.backgroundSound(), (float) data.backgroundVolume(), 1.0f);
                state.playingSound = data.backgroundSound();
                state.backgroundCooldown = data.backgroundInterval();
            }

            // Gestion des sons aléatoires (oiseaux, bruits, etc.)
            for (Ambient.RandomSoundData sound : data.randomSounds()) {
                int cooldown = state.randomCooldowns.getOrDefault(sound.sound(), 0);
                cooldown -= 2;

                if (cooldown <= 0) {
                    // Le son a une chance de se jouer
                    if (Math.random() < sound.chance()) {
                        float pitch = sound.pitchVariation() ? 0.9f + (float) (Math.random() * 0.2f) : 1.0f;

                        Location soundLocation = randomLocationAround(player, 3.0, 8.0);
                        player.playSound(soundLocation, sound.sound(), (float) sound.volume(), pitch);
                    }
                    // Nouveau délai aléatoire avant le prochain essai
                    cooldown = ThreadLocalRandom.current().nextInt(sound.minDelay(), sound.maxDelay() + 1);
                }
                state.randomCooldowns.put(sound.sound(), cooldown);
            }
        } else {
            stopAmbient(player, state); // ambiance désactivée, on coupe le son
        }
    }

    // Gère la lecture de la musique pour un joueur
    private void handleMusic(Player player, ZoneClass zone, PlayerMusicState state) {
        String musicName = getMusicNameEffective(zone);

        // Si la musique a changé, on coupe l'ancienne
        if (!Objects.equals(state.currentMusicName, musicName)) {
            if (state.playingMusicSound != null) {
                player.stopSound(state.playingMusicSound, SoundCategory.RECORDS);
            }
            state.currentMusicName = musicName;
            state.playingMusicSound = null;
            state.musicCooldown = 0;
        }

        if (musicName == null || musicName.isEmpty()) {
            return; // pas de musique définie
        }

        state.musicCooldown -= 2; // décompte du temps

        // Rejoue la musique quand le cooldown est fini
        if (state.musicCooldown <= 0) {
            float volume = getMusicVolumeEffective(zone) / 100f;
            player.playSound(player.getLocation(), musicName, SoundCategory.RECORDS, volume, 1.0f);

            state.playingMusicSound = musicName;
            state.musicCooldown = getMusicDurationEffective(zone);
        }
    }

    // Point d'entrée du blockSound : positions fixes puis entités détectées à proximité
    private void handleBlockSounds(Player player, PlayerMusicState state) {
        handlePositionsSounds(player, state);
        handleEntityEntries(player, state);
    }

    // Gère les sons liés à des coordonnées fixes (ex: voix dans une grotte)
    private void handlePositionsSounds(Player player, PlayerMusicState state) {
        for (Map.Entry<String, BlockSoundClass.PositionEntry> entry : positionSounds.entrySet()) {
            String key = entry.getKey();
            BlockSoundClass.PositionEntry data = entry.getValue();

            if (!data.getEnabled()) continue;

            // Cooldown par entrée : on décrémente de 2s (durée du tick) et on attend s'il n'est pas fini
            int cooldown = state.positionCooldowns.getOrDefault(key, 0) - 2;
            if (cooldown > 0) {
                state.positionCooldowns.put(key, cooldown);
                continue;
            }

            // On cherche si le joueur est proche d'au moins une des positions listées
            Location matchedLocation = null;

            for (String rawLocation : data.getLocations()) {
                Location target = parseLocation(rawLocation);
                if (target == null || target.getWorld() != player.getWorld()) continue; // mondes différents = pas de calcul de distance possible

                if (target.distance(player.getLocation()) <= data.getRadius()) {
                    matchedLocation = target;
                    break;
                }
            }

            // Si une position correspond, on tente le son selon la "chance" configurée
            if (matchedLocation != null && Math.random() < data.getChance()) {
                float pitch = (float) (data.getPitchBase() + (Math.random() * 2 - 1) * data.getPitchVariation());
                player.playSound(matchedLocation, data.getSound(), (float) data.getVolume(), pitch); // joué depuis la position, pas depuis le joueur
            }

            // Nouveau cooldown, qu'il y ait eu un son joué ou non
            int nextCooldown = ThreadLocalRandom.current().nextInt(data.getMinDelay(), data.getMaxDelay() + 1);
            state.positionCooldowns.put(key, nextCooldown);
        }
    }

    // Gère les sons liés à des entités détectées autour du joueur (radio, furniture ItemsAdder...)
    private void handleEntityEntries(Player player, PlayerMusicState state) {
        for (Map.Entry<String, BlockSoundClass.EntityEntry> entry : blockSounds.entrySet()) {
            String key = entry.getKey();
            BlockSoundClass.EntityEntry data = entry.getValue();

            if (!data.getEnabled()) continue;

            // si l'entrée dépend d'ItemsAdder mais que le plugin n'est pas là, on saute
            // toute l'entrée avant même de scanner les entités à proximité (évite un scan pour rien)
            if ("ITEMSADDER".equalsIgnoreCase(data.getType()) && !plugin.getItemsAdderEnabled()) {
                continue;
            }

            int cooldown = state.entityCooldowns.getOrDefault(key, 0) - 2;
            if (cooldown > 0) {
                state.entityCooldowns.put(key, cooldown);
                continue;
            }

            Entity matchingEntity = isNearMatchingEntity(player, data);

            if (matchingEntity != null && Math.random() < data.getChance()) {
                float pitch = (float) (data.getPitchBase() + (Math.random() * 2 - 1) * data.getPitchVariation());
                player.playSound(matchingEntity.getLocation(), data.getSound(), (float) data.getVolume(), pitch); // joué depuis l'entité, pas depuis le joueur
            }

            int nextCooldown = ThreadLocalRandom.current().nextInt(data.getMinDelay(), data.getMaxDelay() + 1);
            state.entityCooldowns.put(key, nextCooldown);
        }
    }

    // Cherche une entité proche du joueur qui correspond à l'entrée blockSound (vanilla ou ItemsAdder)
    private Entity isNearMatchingEntity(Player player, BlockSoundClass.EntityEntry data) {
        double r = data.getRadius();

        for (Entity nearby : player.getNearbyEntities(r, r, r)) {

            if ("ITEMSADDER".equalsIgnoreCase(data.getType())) {
                // La vérification "itemsAdderEnabled" est déjà faite en amont dans handleEntityEntries
                String furnitureId = ItemsAdderHook.getFurnitureId(nearby);
                if (furnitureId != null && furnitureId.equalsIgnoreCase(data.getEntity())) {
                    return nearby;
                }

            } else {
                // Cas vanilla : on compare le type d'entité, puis éventuellement le customModelData
                if (!nearby.getType().name().equalsIgnoreCase(data.getEntity())) continue;
                if (data.getCustomModelData() == 0) return nearby; // pas de modèle précis exigé, le type suffit

                if (nearby instanceof ArmorStand standEntity) {
                    ItemStack helmet = standEntity.getEquipment().getHelmet();
                    if (helmet != null && helmet.hasItemMeta()
                            && helmet.getItemMeta().hasCustomModelData()
                            && helmet.getItemMeta().getCustomModelData() == data.getCustomModelData()) {
                        return nearby;
                    }
                }
            }
        }
        return null; // aucune entité correspondante trouvée à proximité
    }

    // Calcule une position aléatoire autour du joueur (pour les sons ambiants)
    private Location randomLocationAround(Player player, double minRadius, double maxRadius) {
        Location base = player.getLocation();

        double angle = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);
        double distance = ThreadLocalRandom.current().nextDouble(minRadius, maxRadius);

        double x = base.getX() + Math.cos(angle) * distance;
        double z = base.getZ() + Math.sin(angle) * distance;

        return new Location(base.getWorld(), x, base.getY(), z);
    }

    // Coupe les sons du joueur quand il quitte le serveur
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerMusicState state = states.get(player.getUniqueId());

        stopAll(player, state);
        states.remove(player.getUniqueId());
    }

    // --- Ambiance ---

    private boolean isAmbientEnabledEffective(ZoneClass zone) {
        return zone != null ? zone.getAmbientEnabled() : generalArea.getAmbient().getEnabled();
    }

    private String getAmbientNameEffective(ZoneClass zone) {
        return zone != null ? zone.getAmbientName() : generalArea.getAmbient().getName();
    }

    // --- Musique ---

    private boolean isMusicEnabledEffective(ZoneClass zone) {
        return zone != null ? zone.getMusicEnabled() : generalArea.getMusic().getEnabled();
    }

    private String getMusicNameEffective(ZoneClass zone) {
        return zone != null ? zone.getMusicName() : generalArea.getMusic().getName();
    }

    private int getMusicVolumeEffective(ZoneClass zone) {
        return zone != null ? zone.getMusicVolume() : generalArea.getMusic().getVolume();
    }

    private int getMusicDurationEffective(ZoneClass zone) {
        return zone != null ? zone.getMusicDuration() : generalArea.getMusic().getDuration();
    }

    private void stopAmbient(Player player, PlayerMusicState state) {
        if (state == null) return;
        if (state.playingSound != null) {
            player.stopSound(state.playingSound);
            state.playingSound = null;
            state.currentAmbientName = null;
        }
    }

    private void stopMusic(Player player, PlayerMusicState state) {
        if (state == null) return;
        if (state.playingMusicSound != null) {
            player.stopSound(state.playingMusicSound, SoundCategory.RECORDS);
            state.playingMusicSound = null;
            state.currentMusicName = null;
        }
    }

    private void stopAll(Player player, PlayerMusicState state) {
        stopAmbient(player, state);
        stopMusic(player, state);
    }

    // Convertit une ligne "monde, x, y, z" du YAML en Location Bukkit exploitable
    private Location parseLocation(String raw) {
        String[] parts = raw.split(",");
        if (parts.length != 4) return null;

        World world = Bukkit.getWorld(parts[0].trim());
        if (world == null) return null;

        try {
            double x = Double.parseDouble(parts[1].trim());
            double y = Double.parseDouble(parts[2].trim());
            double z = Double.parseDouble(parts[3].trim());
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null; // ligne mal écrite dans le YAML
        }
    }
}