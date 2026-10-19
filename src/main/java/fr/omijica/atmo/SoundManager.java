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

/**
 * Met à jour les sons de chaque joueur selon sa zone, ses préférences et les sources proches.
 * Les délais sont décomptés à chaque passage de la tâche, espacés de 20 ticks serveur
 * (environ une seconde lorsque le serveur fonctionne à son rythme normal).
 */
public class SoundManager implements Listener {

    // Utilise la même catégorie à la lecture et à l'arrêt pour cibler les mêmes sons.
    // Les ambiances et les sources spatiales utilisent AMBIENT ; la musique utilise RECORDS.
    private static final SoundCategory AMBIENT_CATEGORY = SoundCategory.AMBIENT;

    private final Atmo plugin;
    private Map<String, Ambient.AmbientData> ambiances; // profils d'ambiance indexés par nom
    private ConfigClass.GeneralAreaData generalArea; // réglages de repli hors zone
    private final Map<UUID, PlayerMusicState> states = new HashMap<>(); // état son/musique par joueur

    // Données du blockSound : sons liés à des entités détectées, et sons liés à des positions fixes
    private BlockSoundClass.BlockSoundData blockSoundData;
    private Map<String, BlockSoundClass.EntityEntry> blockSounds;
    private Map<String, BlockSoundClass.PositionEntry> positionSounds;

    /** Charge les profils et les sources une fois, puis les conserve jusqu'au prochain rechargement. */
    public SoundManager(Atmo plugin) {
        this.plugin = plugin;
        this.ambiances = Ambient.load(plugin.getDataFolder());
        this.generalArea = ConfigClass.load(plugin.getDataFolder());
        this.blockSoundData = BlockSoundClass.load(plugin.getDataFolder());
        this.blockSounds = blockSoundData.entities();
        this.positionSounds = blockSoundData.positions();
    }

    // État propre à un joueur : les délais d'un joueur ne décalent pas ceux des autres.
    private static class PlayerMusicState {
        String currentAmbientName; // nom de l'ambiance active
        int backgroundCooldown; // délai restant avant de rejouer le son de fond, en secondes nominales
        public String playingSound; // son de fond en cours
        Map<String, Integer> randomCooldowns = new HashMap<>(); // délais par clé de son aléatoire
        java.util.Set<String> playingRandomSounds = new java.util.HashSet<>(); // clés mémorisées pour pouvoir demander leur arrêt

        String currentMusicName; // nom de la musique active
        int musicCooldown; // délai restant avant de rejouer la musique, en secondes nominales
        String playingMusicSound; // musique en cours

        // Délais des sources spatiales, par nom d'entrée YAML et en secondes nominales.
        Map<String, Integer> positionCooldowns = new HashMap<>(); // sources à coordonnées fixes
        Map<String, Integer> entityCooldowns = new HashMap<>();   // sources liées aux entités

        // Sons blockSound actuellement en train de jouer, par nom d'entrée (pour pouvoir les couper)
        Map<String, String> playingPositionSounds = new HashMap<>();
        Map<String, String> playingEntitySounds = new HashMap<>();

        // Dernière zone connue du joueur, pour détecter un changement (téléportation, etc.)
        String currentZoneName;

        // Volume personnel du joueur (0-100)
        // Chargé une fois à la création de l'état (= avant le premier son joué au joueur),
        // et rafraîchi manuellement via refreshPlayerVolume() quand il fait /atmo volume.
        int musicVolume = 100;
        int ambientVolume = 100;
    }

    // Relit les volumes après /atmo volume et coupe les sons suivis :
    // le nouveau volume sera utilisé lors des prochaines lectures, selon les délais restants.
    public void refreshPlayerVolume(Player player) {
        PlayerMusicState state = states.get(player.getUniqueId());
        if (state != null) {
            state.musicVolume = AtmoPlayerData.getMusicVolume(player);
            state.ambientVolume = AtmoPlayerData.getAmbientVolume(player);
            stopAll(player, state);
        }
    }

    // Exécute la boucle sur le fil principal, tous les 20 ticks, pour accéder à l'API Bukkit.
    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 20L);
    }

    // Chaque passage traite tous les joueurs en ligne ; il ne s'agit pas d'un appel à chaque tick serveur.
    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            handlePlayer(player);
        }
    }

    // Coupe les sons suivis avant d'oublier leurs clés, puis recharge les configurations.
    public void reload() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerMusicState state = states.get(player.getUniqueId());
            stopAll(player, state);
        }
        states.clear();
        this.ambiances = Ambient.load(plugin.getDataFolder());
        this.generalArea = ConfigClass.load(plugin.getDataFolder());
        this.blockSoundData = BlockSoundClass.load(plugin.getDataFolder());
        this.blockSounds = blockSoundData.entities();
        this.positionSounds = blockSoundData.positions();
    }

    // Demande l'arrêt des sons à la désactivation ; Bukkit annule les tâches du plugin désactivé.
    public void stop() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerMusicState state = states.get(player.getUniqueId());
            stopAll(player, state);
        }
        // vide les états après avoir arrêté les sons de tous les joueurs, et non après le premier.
        states.clear();
    }

    // Gère l'ambiance, la musique et le blockSound d'un joueur selon sa zone
    private void handlePlayer(Player player) {
        ZoneClass zone = plugin.getZoneChecker().getPlayerZone(player, plugin);
        UUID uuid = player.getUniqueId();

        // Crée l'état au premier passage et lit les volumes persistants avant la première lecture.
        PlayerMusicState state = states.computeIfAbsent(uuid, k -> {
            PlayerMusicState newState = new PlayerMusicState();
            newState.musicVolume = AtmoPlayerData.getMusicVolume(player);
            newState.ambientVolume = AtmoPlayerData.getAmbientVolume(player);
            return newState;
        });

        // Mémorise le dernier nom de zone observé, y compris null lorsque le joueur est hors zone.
        String zoneName = zone != null ? zone.getName() : null;
        if (!Objects.equals(state.currentZoneName, zoneName)) {
            state.currentZoneName = zoneName;
        }

        // Les sources spatiales sont évaluées indépendamment de la zone et du bouton d'ambiance.
        // Leur volume tient toutefois compte du volume personnel d'ambiance.
        handleBlockSounds(player, state);

        // Gestion de la musique
        if (isMusicEnabledEffective(zone, player)) {
            handleMusic(player, zone, state);
        } else {
            stopMusic(player, state);
        }

        // Gestion de l'ambiance sonore

        // Convention actuelle des données persistantes : true signifie « désactivé par le joueur ».
        if (AtmoPlayerData.isAmbientEnabled(player)) {
            stopAmbient(player, state);
            return; // le joueur a désactivé l'ambiance
        }

        if (isAmbientEnabledEffective(zone, player)) {
            String ambientName = getAmbientNameEffective(zone);

            // Si l'ambiance a changé, on coupe l'ancien son
            if (!Objects.equals(state.currentAmbientName, ambientName)) {

                if (state.playingSound != null) {
                    player.stopSound(state.playingSound, AMBIENT_CATEGORY);
                    state.playingSound = null;
                }
                stopRandomSounds(player, state); // on coupe aussi les sons aléatoires de l'ancienne ambiance
                state.currentAmbientName = ambientName;
                state.backgroundCooldown = 0;
                state.randomCooldowns.clear();
            }

            Ambient.AmbientData data = ambiances.get(ambientName);
            if (data == null) {
                return; // ambiance inconnue, on arrête ici
            }

            state.backgroundCooldown -= 1; // une seconde nominale par passage de la tâche

            // Relance le fond à intervalle fixe en arrêtant la lecture précédente de cette clé.
            if (state.backgroundCooldown <= 0) {
                if (state.playingSound != null) {
                    player.stopSound(state.playingSound, AMBIENT_CATEGORY);
                }

                // Le volume final combine celui du profil et le pourcentage personnel du joueur.
                float backgroundVolume = (float) data.backgroundVolume() * (state.ambientVolume / 100f);
                try {
                    player.playSound(player.getLocation(), data.backgroundSound(), AMBIENT_CATEGORY, backgroundVolume, 1.0f);
                } catch (IllegalArgumentException e) {
                    // concatène la vraie clé du son de fond dans le message d'erreur.
                    Bukkit.getLogger().warning("[Atmo] Invalid sound key '" + data.backgroundSound() + "' in configuration.");
                }

                state.playingSound = data.backgroundSound();
                state.backgroundCooldown = data.backgroundInterval();
            }

            // Gestion des sons aléatoires (oiseaux, bruits, etc.)
            for (Ambient.RandomSoundData sound : data.randomSounds()) {
                int cooldown = state.randomCooldowns.getOrDefault(sound.sound(), 0);
                cooldown -= 1;

                if (cooldown <= 0) {
                    if (Math.random() < sound.chance()) {
                        // Tire une hauteur autour de la valeur de base et place le son autour du joueur.
                        float pitch = (float) (sound.pitchBase() + (Math.random() * 2 - 1) * sound.pitchVariation());

                        Location soundLocation = randomLocationAround(player, 3.0, 8.0);
                        float volume = (float) sound.volume() * (state.ambientVolume / 100f);
                        try {
                            player.playSound(soundLocation, sound.sound(), AMBIENT_CATEGORY, volume, pitch);
                        } catch (IllegalArgumentException e) {
                            // affiche la clé du son aléatoire concerné, au lieu du texte de l'expression Java.
                            Bukkit.getLogger().warning("[Atmo] Invalid sound key '" + sound.sound() + "' in configuration.");
                        }

                        state.playingRandomSounds.add(sound.sound()); // on retient ce son pour pouvoir le couper
                    }
                    // Attend avant le prochain tirage, même si aucun son n'a été joué.
                    // Remet les bornes dans l'ordre ; max + 1 inclut le délai maximal dans le tirage.
                    int min = Math.min(sound.minDelay(), sound.maxDelay());
                    int max = Math.max(sound.minDelay(), sound.maxDelay());
                    cooldown = ThreadLocalRandom.current().nextInt(min, max + 1);
                }
                state.randomCooldowns.put(sound.sound(), cooldown);
            }
        } else {
            stopAmbient(player, state); // ambiance désactivée, on coupe le son
        }
    }

    // Gère la lecture de la musique pour un joueur
    private void handleMusic(Player player, ZoneClass zone, PlayerMusicState state) {
        // Même convention inversée que pour l'ambiance : true coupe la musique personnelle.
        if (AtmoPlayerData.isMusicEnabled(player)) {
            stopMusic(player, state);
            return; // le joueur a désactivé la musique
        }

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

        state.musicCooldown -= 1; // décompte du temps

        // La durée configurée détermine la répétition : l'API ne fournit pas ici la fin réelle du fichier.
        if (state.musicCooldown <= 0) {
            float volume = (getMusicVolumeEffective(zone) / 100f) * (state.musicVolume / 100f);
            try {
                player.playSound(player.getLocation(), musicName, SoundCategory.RECORDS, volume, 1.0f);
            } catch (IllegalArgumentException e) {
                // affiche le nom réel de la musique invalide.
                Bukkit.getLogger().warning("[Atmo] Invalid sound key '" + musicName + "' in configuration.");
            }

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

            if (!data.enabled()) {
                stopPositionSound(player, state, key); // entrée désactivée en cours de route = on coupe
                continue;
            }

            // On cherche si le joueur est proche d'au moins une des positions listées
            Location matchedLocation = null;
            for (String rawLocation : data.locations()) {
                Location target = parseLocation(rawLocation);
                if (target == null || target.getWorld() != player.getWorld()) continue;

                if (target.distance(player.getLocation()) <= data.radius()) {
                    matchedLocation = target;
                    break;
                }
            }

            // Sorti du rayon : on coupe le son en cours pour cette entrée, et on prépare
            // un délai à 0 pour retenter sa chance dès le retour dans le rayon.
            if (matchedLocation == null) {
                stopPositionSound(player, state, key);
                state.positionCooldowns.put(key, 0);
                continue;
            }

            // Décompte une seconde nominale pour cette entrée avant le prochain tirage.
            int cooldown = state.positionCooldowns.getOrDefault(key, 0) - 1;
            if (cooldown > 0) {
                state.positionCooldowns.put(key, cooldown);
                continue;
            }

            // Une seule position est retenue par entrée : la première de la liste dans le rayon.
            // À la fin du délai, la probabilité « chance » décide si le son est joué.
            if (Math.random() < data.chance()) {
                float pitch = (float) (data.pitchBase() + (Math.random() * 2 - 1) * data.pitchVariation());
                float volume = (float) data.volume() * (state.ambientVolume / 100f);
                try {
                    player.playSound(matchedLocation, data.sound(), AMBIENT_CATEGORY, volume, pitch); // joué depuis la position, pas depuis le joueur
                } catch (IllegalArgumentException e) {
                    // indique la clé du son associé à la position fixe.
                    Bukkit.getLogger().warning("[Atmo] Invalid sound key '" + data.sound() + "' in configuration.");
                }

                state.playingPositionSounds.put(key, data.sound()); // on mémorise le son en cours pour pouvoir le couper
            }

            // Fixe un nouveau délai, qu'un son ait été joué ou non.
            int min = Math.min(data.minDelay(), data.maxDelay());
            int max = Math.max(data.minDelay(), data.maxDelay());
            int nextCooldown = ThreadLocalRandom.current().nextInt(min, max + 1);
            state.positionCooldowns.put(key, nextCooldown);
        }
    }

    // Coupe le son "position" actuellement joué pour une entrée donnée, s'il y en a un
    private void stopPositionSound(Player player, PlayerMusicState state, String key) {
        String sound = state.playingPositionSounds.remove(key);
        if (sound != null) {
            player.stopSound(sound, AMBIENT_CATEGORY);
        }
    }

    // Gère les sons liés à des entités détectées autour du joueur (radio, meuble ItemsAdder...).
    private void handleEntityEntries(Player player, PlayerMusicState state) {
        for (Map.Entry<String, BlockSoundClass.EntityEntry> entry : blockSounds.entrySet()) {
            String key = entry.getKey();
            BlockSoundClass.EntityEntry data = entry.getValue();

            if (!data.enabled()) {
                stopEntitySound(player, state, key);
                continue;
            }

            // Évite tout appel à l'API facultative si ItemsAdder n'est pas actif.
            if ("ITEMSADDER".equalsIgnoreCase(data.type()) && !plugin.getItemsAdderEnabled()) {
                stopEntitySound(player, state, key);
                continue;
            }

            // Recherche à chaque passage, même pendant l'attente, pour détecter une sortie de proximité.
            Entity matchingEntity = isNearMatchingEntity(player, data);

            // Plus aucune entité correspondante à proximité : on coupe le son en cours
            if (matchingEntity == null) {
                stopEntitySound(player, state, key);
                state.entityCooldowns.put(key, 0);
                continue;
            }

            int cooldown = state.entityCooldowns.getOrDefault(key, 0) - 1;
            if (cooldown > 0) {
                state.entityCooldowns.put(key, cooldown);
                continue;
            }

            if (Math.random() < data.chance()) {
                float pitch = (float) (data.pitchBase() + (Math.random() * 2 - 1) * data.pitchVariation());
                float volume = (float) data.volume() * (state.ambientVolume / 100f);
                try {
                    player.playSound(matchingEntity.getLocation(), data.sound(), AMBIENT_CATEGORY, volume, pitch);
                } catch (IllegalArgumentException e) {
                    // indique la clé du son associé à l'entité.
                    Bukkit.getLogger().warning("[Atmo] Invalid sound key '" + data.sound() + "' in configuration.");
                }

                state.playingEntitySounds.put(key, data.sound());
            }

            int min = Math.min(data.minDelay(), data.maxDelay());
            int max = Math.max(data.minDelay(), data.maxDelay());
            int nextCooldown = ThreadLocalRandom.current().nextInt(min, max + 1);
            state.entityCooldowns.put(key, nextCooldown);
        }
    }

    // Coupe le son "entité" actuellement joué pour une entrée donnée, s'il y en a un
    private void stopEntitySound(Player player, PlayerMusicState state, String key) {
        String sound = state.playingEntitySounds.remove(key);
        if (sound != null) {
            player.stopSound(sound, AMBIENT_CATEGORY);
        }
    }

    // Coupe tous les sons blockSound en cours (positions + entités) pour un joueur
    private void stopAllBlockSounds(Player player, PlayerMusicState state) {
        if (state == null) return;

        for (String sound : state.playingPositionSounds.values()) {
            player.stopSound(sound, AMBIENT_CATEGORY);
        }
        state.playingPositionSounds.clear();

        for (String sound : state.playingEntitySounds.values()) {
            player.stopSound(sound, AMBIENT_CATEGORY);
        }
        state.playingEntitySounds.clear();
    }

    // Cherche une entité proche du joueur qui correspond à l'entrée blockSound (vanilla ou ItemsAdder)
    private Entity isNearMatchingEntity(Player player, BlockSoundClass.EntityEntry data) {
        double r = data.radius();

        // Bukkit recherche dans une boîte de demi-dimensions r sur chaque axe.
        // Le premier résultat correspondant est retenu, sans classement par distance.
        for (Entity nearby : player.getNearbyEntities(r, r, r)) {

            if ("ITEMSADDER".equalsIgnoreCase(data.type())) {
                String furnitureId = ItemsAdderHook.getFurnitureId(nearby);
                boolean matches = furnitureId != null && furnitureId.equalsIgnoreCase(data.entity());
                if (matches) {
                    return nearby;
                }

            } else {
                if (!nearby.getType().name().equalsIgnoreCase(data.entity())) continue;
                if (data.customModelData() == 0) return nearby;

                // Le filtre de modèle ne s'applique qu'au casque des porte-armures.
                if (nearby instanceof ArmorStand standEntity) {
                    ItemStack helmet = standEntity.getEquipment().getHelmet();
                    if (helmet != null && helmet.hasItemMeta()
                            && helmet.getItemMeta().hasCustomModelData()
                            && helmet.getItemMeta().getCustomModelData() == data.customModelData()) {
                        return nearby;
                    }
                }
            }
        }
        return null; // aucune entité correspondante trouvée à proximité
    }

    // Tire un angle et une distance entre les deux rayons, en conservant la hauteur du joueur.
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

    // La zone sélectionnée fournit tous les réglages, même si son ambiance est désactivée.
    // Le repli général s'applique uniquement hors zone et dans les mondes configurés.

    private boolean isAmbientEnabledEffective(ZoneClass zone, Player player) {
        if (zone != null) {
            return zone.getAmbientEnabled();
        }
        if (generalArea != null && generalArea.worlds().contains(player.getWorld().getName())) {
            return generalArea.ambient().enabled();
        }
        return false;
    }

    // À appeler après la vérification d'activation, qui valide aussi la disponibilité du repli.
    private String getAmbientNameEffective(ZoneClass zone) {
        return zone != null ? zone.getAmbientName() : generalArea.ambient().name();
    }

    // La musique suit la même règle : réglages de la zone, sinon repli autorisé pour le monde.

    private boolean isMusicEnabledEffective(ZoneClass zone, Player player) {
        if (zone != null) {
            return zone.getMusicEnabled();
        }
        if (generalArea != null && generalArea.worlds().contains(player.getWorld().getName())) {
            return generalArea.music().enabled();
        }
        return false;
    }

    private String getMusicNameEffective(ZoneClass zone) {
        return zone != null ? zone.getMusicName() : generalArea.music().name();
    }

    private int getMusicVolumeEffective(ZoneClass zone) {
        return zone != null ? zone.getMusicVolume() : generalArea.music().volume();
    }

    private int getMusicDurationEffective(ZoneClass zone) {
        return zone != null ? zone.getMusicDuration() : generalArea.music().duration();
    }

    // Arrête le fond et les sons aléatoires du profil ; les sources spatiales sont gérées séparément.
    private void stopAmbient(Player player, PlayerMusicState state) {
        if (state == null) return;
        if (state.playingSound != null) {
            player.stopSound(state.playingSound, AMBIENT_CATEGORY);
            state.playingSound = null;
            state.currentAmbientName = null;
        }
        stopRandomSounds(player, state);
        state.playingRandomSounds.clear();

    }

    // Coupe tous les sons aléatoires actuellement en cours pour ce joueur (utile pour les sons en boucle)
    private void stopRandomSounds(Player player, PlayerMusicState state) {
        if (state == null || state.playingRandomSounds.isEmpty()) return;
        for (String sound : state.playingRandomSounds) {
            player.stopSound(sound, AMBIENT_CATEGORY);
        }
        state.playingRandomSounds.clear();
    }

    private void stopMusic(Player player, PlayerMusicState state) {
        if (state == null) return;
        if (state.playingMusicSound != null) {
            player.stopSound(state.playingMusicSound, SoundCategory.RECORDS);
            state.playingMusicSound = null;
            state.currentMusicName = null;
        }
    }

    // Regroupe l'arrêt des trois familles de sons pour une déconnexion, un rechargement ou l'arrêt du plugin.
    private void stopAll(Player player, PlayerMusicState state) {
        stopAmbient(player, state);
        stopMusic(player, state);
        stopAllBlockSounds(player, state);
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
