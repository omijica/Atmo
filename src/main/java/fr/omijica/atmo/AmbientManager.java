package fr.omijica.atmo;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

// Gère les sons d'ambiance et la musique pour chaque joueur
public class AmbientManager implements Listener {

    private final Atmo plugin;
    private final Ambient ambient;
    private Map<String, Ambient.AmbientData> ambiances; // liste des ambiances chargées
    private ConfigClass.GeneralAreaData generalArea; // config générale (zone par défaut)
    private final Map<UUID, PlayerMusicState> states = new HashMap<>(); // état son/musique par joueur

    public AmbientManager(Atmo plugin) {
        this.plugin = plugin;
        this.ambient = plugin.getAmbient();
        this.ambiances = Ambient.load(plugin.getDataFolder());
        this.generalArea = plugin.getConfigClass().load(plugin.getDataFolder());
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
    }

    // Gère l'ambiance et la musique d'un joueur selon sa zone
    private void handlePlayer(Player player) {
        ZoneClass zone = plugin.getZoneChecker().getPlayerZone(player);
        UUID uuid = player.getUniqueId();
        PlayerMusicState state = states.computeIfAbsent(uuid, k -> new PlayerMusicState());

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

        stopAll(player,state);
        states.remove(player.getUniqueId());
    }

    // --- Ambiance ---

    // Vérifie si l'ambiance est activée (zone ou config générale)
    private boolean isAmbientEnabledEffective(ZoneClass zone) {
        return zone != null ? zone.getAmbientEnabled() : generalArea.getAmbient().getEnabled();
    }

    // Récupère le nom de l'ambiance active (zone ou config générale)
    private String getAmbientNameEffective(ZoneClass zone) {
        return zone != null ? zone.getAmbientName() : generalArea.getAmbient().getName();
    }

    // --- Musique ---

    // Vérifie si la musique est activée (zone ou config générale)
    private boolean isMusicEnabledEffective(ZoneClass zone) {
        return zone != null ? zone.getMusicEnabled() : generalArea.getMusic().getEnabled();
    }

    // Récupère le nom de la musique active (zone ou config générale)
    private String getMusicNameEffective(ZoneClass zone) {
        return zone != null ? zone.getMusicName() : generalArea.getMusic().getName();
    }

    // Récupère le volume de la musique (zone ou config générale)
    private int getMusicVolumeEffective(ZoneClass zone) {
        return zone != null ? zone.getMusicVolume() : generalArea.getMusic().getVolume();
    }

    // Récupère la durée de la musique (zone ou config générale)
    private int getMusicDurationEffective(ZoneClass zone) {
        return zone != null ? zone.getMusicDuration() : generalArea.getMusic().getDuration();
    }

    // Arrête le son d'ambiance du joueur
    private void stopAmbient(Player player, PlayerMusicState state) {
        if (state == null) return;
        if (state.playingSound != null) {
            player.stopSound(state.playingSound);
            state.playingSound = null;
            state.currentAmbientName = null;
        }
    }

    // Arrête la musique du joueur
    private void stopMusic(Player player, PlayerMusicState state) {
        if (state == null) return;
        if (state.playingMusicSound != null) {
            player.stopSound(state.playingMusicSound, SoundCategory.RECORDS);
            state.playingMusicSound = null;
            state.currentMusicName = null;
        }
    }

    // Arrête tous les sons (ambiance + musique)
    private void stopAll(Player player, PlayerMusicState state) {
        stopAmbient(player, state);
        stopMusic(player, state);
    }
}