package fr.omijica.atmo;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Transforme les clics sur des blocs en sélections de zones, puis enregistre leurs limites. */
public class ZoneListener implements Listener {

    private final Atmo plugin;
    private final ZoneChecker zoneChecker;
    // Chaque joueur dispose de sa propre sélection, supprimée une fois terminée ou à sa déconnexion.
    private final Map<UUID, ZoneCreationSession> activeSessions = new HashMap<>();

    public ZoneListener(Atmo plugin, ZoneChecker zoneChecker) {
        this.plugin = plugin;
        this.zoneChecker = zoneChecker;
    }

    /** permet à /atmo editor d'annuler la session réellement conservée par ce listener. */
    public boolean disableCreationMode(Player player) {
        return activeSessions.remove(player.getUniqueId()) != null;
    }

    /** Commence une nouvelle sélection, y compris pour redéfinir les limites d'une zone existante. */
    public void enableCreationMode(Player player, String zoneName) {
        activeSessions.put(player.getUniqueId(), new ZoneCreationSession(zoneName));
        MessageUtils.sendInfo(player, "Edit mode enabled for: <white>" + zoneName + " <gray>(/atmo editor to exit)");
        MessageUtils.sendMessage(player, "<gray>- <white>Left-click <gray>on a block: Position 1");
        MessageUtils.sendMessage(player, "<gray>- <white>Right-click <gray>on a block: Position 2");
    }

    /** Réserve les clics gauche et droit aux deux coins pendant une session d'édition. */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!activeSessions.containsKey(uuid)) { return; }
        if (event.getClickedBlock() == null) { return; }

        ZoneCreationSession session = activeSessions.get(uuid);
        Location clickedLocation = event.getClickedBlock().getLocation();

        // Annule l'action normale sur le bloc pour que le clic serve à la sélection.
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            session.setPos1(clickedLocation);
            MessageUtils.sendSuccess(player, "Position 1 defined: <white>" + formatLoc(clickedLocation));
            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
            checkLoc(player, session);
        }
        else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            session.setPos2(clickedLocation);
            MessageUtils.sendSuccess(player, "Position 2 defined: <white>" + formatLoc(clickedLocation));
            player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
            checkLoc(player, session);
        }
    }

    /** Attend les deux coins dans un même monde avant de demander la sauvegarde et de fermer la session. */
    private void checkLoc(Player player, ZoneCreationSession session) {
        if (session.isComplete()) {
            if (!session.getPos1().getWorld().equals(session.getPos2().getWorld())) {
                MessageUtils.sendError(player, "Both positions must be in the same world.");
                return;
            }


            // conserve la sélection et signale l'erreur si la zone n'a pas pu être enregistrée.
            if (!saveZoneToYml(session)) {
                MessageUtils.sendError(player, "Unable to save the zone. Your selection has been kept; try again.");
                return;
            }
            activeSessions.remove(player.getUniqueId());
            MessageUtils.sendSuccess(player, "Area <white>" + session.getZoneName() + " <green>successfully saved!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    private String formatLoc(Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }

    /**  renvoie un résultat pour ne fermer la session qu'après une sauvegarde réussie. */
    private boolean saveZoneToYml(ZoneCreationSession session) {
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
                // propage l'échec de création du fichier à la méthode qui gère la sélection.
                return false;
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

        // Lors d'une redéfinition, conserve les réglages audio et la priorité déjà présents.
        // Pour une nouvelle zone, initialise les valeurs manquantes avec les sons désactivés.
        if (!config.contains(path + "priority")) {
            config.set(path + "priority", 1);
        }
        if (!config.contains(path + "music.enabled")) {
            config.set(path + "music.enabled", false);
        }
        if (!config.contains(path + "music.name")) {
            config.set(path + "music.name", "");
        }
        if (!config.contains(path + "music.volume")) {
            config.set(path + "music.volume", 100);
        }
        if (!config.contains(path + "music.duration")) {
            config.set(path + "music.duration", "");
        }
        if (!config.contains(path + "ambient.enabled")) {
            config.set(path + "ambient.enabled", false);
        }
        if (!config.contains(path + "ambient.name")) {
            config.set(path + "ambient.name", "");
        }
        if (!config.contains(path + "activationPerPlayer")) {
            config.set(path + "activationPerPlayer", false);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Unable to save the zone to area.yml!");
            e.printStackTrace();
            // empêche d'annoncer un succès ou de recharger les zones après un échec d'écriture.
            return false;
        }

        zoneChecker.setZones(ZoneConfig.load(plugin.getDataFolder()));
        // confirme la réussite seulement après l'écriture du fichier et le rechargement des zones.
        return true;
    }

    /** Libère la sélection inachevée lorsque le joueur quitte le serveur. */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        activeSessions.remove(event.getPlayer().getUniqueId());
    }
}
