package fr.omijica.atmo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AtmoCommand implements CommandExecutor {

    MiniMessage mm = MiniMessage.miniMessage(); //couleur
    private final Map<UUID, ZoneCreationSession> activeSessions = new HashMap<>();
    private final Atmo plugin;
    private final ZoneChecker zoneChecker;

    public AtmoCommand(Atmo plugin, ZoneChecker zoneChecker) {
        this.plugin = plugin;
        this.zoneChecker = zoneChecker; 
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("atmo.use")) {
            sender.sendMessage(mm.deserialize("<red>You do not have permission to use this command."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(mm.deserialize("<red>Usage: /atmo"));
            return true;
        }

        String arg1 = args[0].toLowerCase();

        switch (arg1) {

            case "menu":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>Only a player can open the menu."));
                    return true;
                }

                AtmoMenu.openMenu(player);
                break;

            case "editor":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>Only a player can run this command."));
                    return true;
                }

                UUID uuid = player.getUniqueId();

                if (activeSessions.containsKey(uuid)) {
                    activeSessions.remove(player.getUniqueId());
                    player.sendMessage(mm.deserialize("<red>You have exited edit mode."));
                }
                else {
                    AtmoMenu.openAnvilMenu(plugin, player);
                }
                break;

            case "info":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize("<red>Only a player can run this command."));
                    return true;
                }

                ZoneClass zone = zoneChecker.getPlayerZone(player);
                if (zone == null) {
                    player.sendMessage(mm.deserialize("<red>You are not in any zone."));
                }
                else {
                    player.sendMessage(mm.deserialize("<red>You are in the area:<white> " + zone.getName() + " <gray>(Depending on the priority)"));
                }
                break;

            case "reload":
                if (!sender.hasPermission("atmo.reload")) {
                    sender.sendMessage(mm.deserialize("<red>You do not have permission to use this command."));
                    return true;
                }

                zoneChecker.loadZones(plugin.getDataFolder());
                sender.sendMessage(mm.deserialize("<green>Atmo configuration and zones reloaded successfully!"));
                break;

            default:
                sender.sendMessage(mm.deserialize("<red>Usage: /atmo"));
                break;

        }

        return true;

    }

}
