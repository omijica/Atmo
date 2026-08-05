package fr.omijica.atmo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AmbientManager {

    private final Atmo plugin;
    private final Ambient ambient;
    private final Map<String, Ambient.AmbientData> ambiances;
    private final Map<UUID, PlayerAmbientState> states = new HashMap<>();

    public AmbientManager(Atmo plugin) {
        this.plugin = plugin;
        this.ambient = plugin.getAmbient();
        this.ambiances = Ambient.load(plugin.getDataFolder());
    }

    private static class PlayerAmbientState {
        String currentAmbientName;
        int backgroundCooldown;
        public String playingSound;
        Map<String, Integer> randomCooldowns = new HashMap<>();
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 40L);
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            handlePlayer(player);
        }
    }

    private void handlePlayer(Player player) {
        ZoneClass zone = plugin.getZoneChecker().getPlayerZone(player);
        UUID uuid = player.getUniqueId();
        PlayerAmbientState state = states.computeIfAbsent(uuid, k -> new PlayerAmbientState());

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

    }
}