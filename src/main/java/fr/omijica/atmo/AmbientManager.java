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

public class AmbientManager implements Listener {

    private final Atmo plugin;
    private final Ambient ambient;
    private Map<String, Ambient.AmbientData> ambiances;
    private final Map<UUID, PlayerMusicState> states = new HashMap<>();

    public AmbientManager(Atmo plugin) {
        this.plugin = plugin;
        this.ambient = plugin.getAmbient();
        this.ambiances = Ambient.load(plugin.getDataFolder());
    }

    private static class PlayerMusicState {
        String currentAmbientName;
        int backgroundCooldown;
        public String playingSound;
        Map<String, Integer> randomCooldowns = new HashMap<>();

        String currentMusicName;
        int musicCooldown;
        String playingMusicSound;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 40L);
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            handlePlayer(player);
        }
    }

    public void reload() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerMusicState state = states.get(player.getUniqueId());
            stopAll(player, state);
        }
        states.clear();
        this.ambiances = Ambient.load(plugin.getDataFolder());
    }

    private void handlePlayer(Player player) {
        ZoneClass zone = plugin.getZoneChecker().getPlayerZone(player);
        UUID uuid = player.getUniqueId();
        PlayerMusicState state = states.computeIfAbsent(uuid, k -> new PlayerMusicState());

        if (zone == null) {
            stopAll(player, state);
            return;
        }

        if (zone.getMusicEnabled() && zone.getMusicName() != null) {
            handleMusic(player, zone, state);
        }

        if (zone.getAmbientEnabled()) {
            String zoneAmbientName = (zone != null && zone.getAmbientEnabled()) ? zone.getAmbientName() : null;
            if (!Objects.equals(state.currentAmbientName, zoneAmbientName)) {

                if (state.playingSound != null) {
                    player.stopSound(state.playingSound);
                    state.playingSound = null;
                }
                state.currentAmbientName = zoneAmbientName;
                state.backgroundCooldown = 0;
                state.randomCooldowns.clear();
            }

            Ambient.AmbientData data = ambiances.get(zoneAmbientName);
            if (data == null) {
                return;
            }

            state.backgroundCooldown -= 2;

            if (state.backgroundCooldown <= 0) {
                if (state.playingSound != null) {
                    player.stopSound(state.playingSound);
                }

                player.playSound(player.getLocation(), data.backgroundSound(), (float) data.backgroundVolume(), 1.0f);
                state.playingSound = data.backgroundSound();
                state.backgroundCooldown = data.backgroundInterval();
            }

            for (Ambient.RandomSoundData sound : data.randomSounds()) {
                int cooldown = state.randomCooldowns.getOrDefault(sound.sound(), 0);
                cooldown -= 2;

                if (cooldown <= 0) {
                    if (Math.random() < sound.chance()) {
                        float pitch = sound.pitchVariation() ? 0.9f + (float) (Math.random() * 0.2f) : 1.0f;

                        Location soundLocation = randomLocationAround(player, 3.0, 8.0);
                        player.playSound(soundLocation, sound.sound(), (float) sound.volume(), pitch);
                    }
                    cooldown = ThreadLocalRandom.current().nextInt(sound.minDelay(), sound.maxDelay() + 1);
                }
                state.randomCooldowns.put(sound.sound(), cooldown);
            }
        }
    }

    private void handleMusic(Player player, ZoneClass zone, PlayerMusicState state) {
        String zoneMusicName = zone.getMusicName();

        if (!Objects.equals(state.currentMusicName, zoneMusicName)) {
            if (state.playingMusicSound != null) {
                player.stopSound(state.playingMusicSound, SoundCategory.RECORDS);
            }
            state.currentMusicName = zoneMusicName;
            state.playingMusicSound = null;
            state.musicCooldown = 0;
        }

        if (zoneMusicName == null) {
            return;
        }

        state.musicCooldown -= 2;

        if (state.musicCooldown <= 0) {
            float volume = zone.getMusicVolume() / 100f;
            player.playSound(player.getLocation(), zoneMusicName, SoundCategory.RECORDS, volume, 1.0f);

            state.playingMusicSound = zoneMusicName;
            state.musicCooldown = zone.getMusicDuration();
        }
    }

    private void stopAll(Player player, PlayerMusicState state) {
        if (state == null) return;
        if (state.playingSound != null) {
            player.stopSound(state.playingSound);
            state.playingSound = null;
            state.currentAmbientName = null;
        }
        if (state.playingMusicSound != null) {
            player.stopSound(state.playingMusicSound, SoundCategory.RECORDS);
            state.playingMusicSound = null;
            state.currentMusicName = null;
        }
    }

    private Location randomLocationAround(Player player, double minRadius, double maxRadius) {
        Location base = player.getLocation();

        double angle = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);
        double distance = ThreadLocalRandom.current().nextDouble(minRadius, maxRadius);

        double x = base.getX() + Math.cos(angle) * distance;
        double z = base.getZ() + Math.sin(angle) * distance;

        return new Location(base.getWorld(), x, base.getY(), z);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerMusicState state = states.get(player.getUniqueId());

        stopAll(player,state);
        states.remove(player.getUniqueId());
    }
}