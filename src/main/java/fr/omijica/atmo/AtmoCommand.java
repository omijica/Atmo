package fr.omijica.atmo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class AtmoCommand implements CommandExecutor, TabExecutor {

    private final Map<UUID, ZoneCreationSession> activeSessions = new HashMap<>();
    private final Atmo plugin;
    private final ZoneChecker zoneChecker;

    public AtmoCommand(Atmo plugin, ZoneChecker zoneChecker) {
        this.plugin = plugin;
        this.zoneChecker = zoneChecker; 
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("atmo.use")) {
            MessageUtils.sendError(player, "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            MessageUtils.sendInfo(player, "Usage: /atmo <menu|editor|info|reload>");
            return true;
        }

        String arg1 = args[0].toLowerCase();

        switch (arg1) {

            case "menu":
                AtmoMenu.openMenu(player);
                MessageUtils.playClick(player);
                break;

            case "editor":
                UUID uuid = player.getUniqueId();

                if (activeSessions.containsKey(uuid)) {
                    activeSessions.remove(player.getUniqueId());
                    MessageUtils.sendInfo(player, "You have exited edit mode.");
                }
                else {
                    AtmoMenu.openAnvilMenu(plugin, player);
                    MessageUtils.playClick(player);
                }
                break;

            case "info":
                ZoneClass zone = zoneChecker.getPlayerZone(player);
                if (zone == null) {
                    MessageUtils.sendError(player, "You are not in any zone.");
                }
                else {
                    MessageUtils.sendInfo(player, "You are in the area: <white>" + zone.getName() + " <gray>(Depending on the priority)");
                }
                break;

            case "reload":
                if (!player.hasPermission("atmo.reload")) {
                    MessageUtils.sendError(player, "You do not have permission to use this command.");
                    return true;
                }

                zoneChecker.loadZones(plugin.getDataFolder());
                plugin.getAmbientManager().reload();
                MessageUtils.sendSuccess(player, "Atmo configuration and zones reloaded successfully!");
                break;

            /* case "test":
                zoneChecker.getZones().clear();
                for (int i = 1; i <= 100; i++) {
                    zoneChecker.getZones().add(new ZoneClass("Zone #" + i, "world", i * 10, 64, i * 10, i * 10 + 5, 70, i * 10 + 5, i, "test", false));
                }

                plugin.getAtmoMenu().openPaperMenu(player, 0);
                return true; */

            default:
                MessageUtils.sendInfo(player, "Usage: /atmo <menu|editor|info|reload>");
                break;

        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(List.of("menu", "editor", "reload", "info"));

            if (!player.hasPermission("atmo.reload")) {
                subCommands.remove("reload");
            }

            StringUtil.copyPartialMatches(args[0], subCommands, completions);
        }

        Collections.sort(completions);
        return completions;
    }
}
