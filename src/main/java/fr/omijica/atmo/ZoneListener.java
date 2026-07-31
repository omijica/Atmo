package fr.omijica.atmo;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZoneListener implements Listener {

    MiniMessage mm = MiniMessage.miniMessage();

    private final Atmo plugin;
    private final Map<UUID, ZoneCreationSession> activeSessions = new HashMap<>();

    public ZoneListener(Atmo plugin) {
        this.plugin = plugin;
    }

    public void enableCreationMode(Player player, String zoneName) {
        activeSessions.put(player.getUniqueId(), new ZoneCreationSession(zoneName));
        player.sendMessage(mm.deserialize("<green>Edit mode enabled for :<white> " + zoneName + " <gray>/atmo editor to exit."));
        player.sendMessage(mm.deserialize("<gray>- <white>Left-click <gray>on a block : Position 1"));
        player.sendMessage(mm.deserialize("<gray>- <white>Right-click <gray>on a block : Position 2"));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!activeSessions.containsKey(uuid)) { return; }
        if (event.getClickedBlock() == null) { return; }

        ZoneCreationSession session = activeSessions.get(uuid);
        Location clickedLocation = event.getClickedBlock().getLocation();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            session.setPos1(clickedLocation);
            player.sendMessage(mm.deserialize("<green>Position 1 defined : " + formatLoc(clickedLocation)));
            checkLoc(player, session);
        }
        else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            session.setPos2(clickedLocation);
            player.sendMessage(mm.deserialize("<green>Position 2 defined : " + formatLoc(clickedLocation)));
            checkLoc(player, session);
        }
    }

    private void checkLoc(Player player, ZoneCreationSession session) {
        if (session.isComplete()) {
            if (!session.getPos1().getWorld().equals(session.getPos2().getWorld())) {
                player.sendMessage(mm.deserialize("<red>Error: Both positions must be in the same world."));
                return;
            }

            saveZoneToYml(session);
            activeSessions.remove(player.getUniqueId());
            player.sendMessage(mm.deserialize("<green>✔ <white>Area <green>" + session.getZoneName() + " <white>successfully saved to area.yml !"));
        }
    }

    private String formatLoc(Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }

    private void saveZoneToYml(ZoneCreationSession session) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File file = new File(plugin.getDataFolder(), "area.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Unable to create the area.yml file!");
                e.printStackTrace();
                return;
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String zonePath = "areas." + session.getZoneName();

        String path = zonePath + ".";
        Location p1 = session.getPos1();
        Location p2 = session.getPos2();

        config.set(path + "world", p1.getWorld().getName());
        config.set(path + "pos1.x", p1.getBlockX());
        config.set(path + "pos1.y", p1.getBlockY());
        config.set(path + "pos1.z", p1.getBlockZ());

        config.set(path + "pos2.x", p2.getBlockX());
        config.set(path + "pos2.y", p2.getBlockY());
        config.set(path + "pos2.z", p2.getBlockZ());

        config.set(path + "priority", 1);

        config.set(path + "music.enabled", false);
        config.set(path + "music.name", "");

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Unable to save the zone to area.yml!");
            e.printStackTrace();
        }
    }
}
