package fr.omijica.atmo;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AtmoCommand implements CommandExecutor, TabExecutor {

    private final Map<UUID, ZoneCreationSession> activeSessions = new HashMap<>();
    private final Atmo plugin;
    private final ZoneChecker zoneChecker;
    private Map<String, BlockSoundClass.PositionEntry> positionSounds;
    private BlockSoundClass.BlockSoundData blockSoundData;
    private ConfigClass.GeneralConfigData generalConfigData;

    public AtmoCommand(Atmo plugin, ZoneChecker zoneChecker) {
        this.plugin = plugin;
        this.zoneChecker = zoneChecker;
        this.blockSoundData = BlockSoundClass.load(plugin.getDataFolder());
        this.positionSounds = blockSoundData.getPositions();
        this.generalConfigData = plugin.getConfigClass().loadBaseConfig(plugin.getDataFolder());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("atmo.admin")) {
            if (!player.hasPermission("atmo.use")) {
                MessageUtils.sendError(player, "You do not have permission to use this command.");
                return true;
            }
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String arg1 = args[0].toLowerCase();

        switch (arg1) {

            case "menu":
                if (!player.hasPermission("atmo.admin")) {
                    if (!player.hasPermission("atmo.menu")) {
                        MessageUtils.sendError(player, "You do not have permission to use this command.");
                        return true;
                    }
                }
                AtmoMenu.openMenu(player);
                MessageUtils.playClick(player);
                break;

            case "editor":
                if (!player.hasPermission("atmo.admin")) {
                    if (!player.hasPermission("atmo.editor")) {
                        MessageUtils.sendError(player, "You do not have permission to use this command.");
                        return true;
                    }
                }
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
                if (!player.hasPermission("atmo.admin")) {
                    if (!player.hasPermission("atmo.info")) {
                        MessageUtils.sendError(player, "You do not have permission to use this command.");
                        return true;
                    }
                }
                ZoneClass zone = zoneChecker.getPlayerZone(player);
                if (zone == null) {
                    MessageUtils.sendError(player, "You are not in any zone.");
                }
                else {
                    MessageUtils.sendInfo(player, "You are in the area: <white>" + zone.getName() + " <gray>(Depending on the priority)");
                }
                break;

            case "reload":
                if (!player.hasPermission("atmo.admin")) {
                    if (!player.hasPermission("atmo.reload")) {
                        MessageUtils.sendError(player, "You do not have permission to use this command.");
                        return true;
                    }
                }
                zoneChecker.loadZones(plugin.getDataFolder());
                plugin.getSoundManager().reload();
                this.blockSoundData = BlockSoundClass.load(plugin.getDataFolder());
                this.positionSounds = blockSoundData.getPositions();
                this.generalConfigData = plugin.getConfigClass().loadBaseConfig(plugin.getDataFolder());
                MessageUtils.sendSuccess(player, "Atmo configuration and zones reloaded successfully!");
                break;

            case "addloc":
                if (!player.hasPermission("atmo.admin")) {
                    if (!player.hasPermission("atmo.addloc")) {
                        MessageUtils.sendError(player, "You do not have permission to use this command.");
                        return true;
                    }
                }
                if (args.length < 2) {
                    MessageUtils.sendError(player, "Usage: /atmo addloc <name>");
                    return true;
                }
                String arg2 = args[1].toLowerCase();
                if (positionSounds.containsKey(arg2)) {
                    if (!plugin.getDataFolder().exists()) {
                        plugin.getDataFolder().mkdirs();
                    }

                    File file = new File(plugin.getDataFolder(), "blockSound.yml");
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            plugin.getLogger().severe("Unable to create the blockSound.yml file!");
                            e.printStackTrace();
                            return true;
                        }
                    }

                    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                    String path = "blockSound.positions." + arg2 + ".locations";

                    String playerLoc = serializeLocation(player.getLocation());
                    List<String> locations = config.getStringList(path);
                    locations.add(playerLoc);

                    config.set(path, locations);

                    try {
                        config.save(file);
                    } catch (IOException e) {
                        plugin.getLogger().severe("Unable to save the location to blockSound.yml!");
                        e.printStackTrace();
                    }

                    this.blockSoundData = BlockSoundClass.load(plugin.getDataFolder());
                    this.positionSounds = blockSoundData.getPositions();
                    plugin.getSoundManager().reload();

                    MessageUtils.sendSuccess(player, "Position added to '" + arg2 + "' !");

                    break;

                } else {
                    MessageUtils.sendError(player, "This BlockSound name doesn't exist.");
                    return true;
                }

            case "music":
                if (!player.hasPermission("atmo.admin")) {
                    if (!player.hasPermission("atmo.music")) {
                        if (!generalConfigData.getAllowPlayerDisableMusic()) {
                            MessageUtils.sendError(player, "You do not have permission to use this command.");
                            return true;
                        }
                    }
                }

                boolean current = AtmoPlayerData.isMusicEnabled(player);
                boolean newValue = !current;

                AtmoPlayerData.setMusicEnabled(player, newValue);

                if (newValue) {
                    MessageUtils.sendInfo(player, "Background music disabled !");
                } else {
                    MessageUtils.sendSuccess(player, "Background music enabled.");
                }
                break;

            case "ambient":
                if (!player.hasPermission("atmo.admin")) {
                    if (!player.hasPermission("atmo.ambient")) {
                        if (!generalConfigData.getAllowPlayerDisableAmbient()) {
                            MessageUtils.sendError(player, "You do not have permission to use this command.");
                            return true;
                        }
                    }
                }

                boolean current2 = AtmoPlayerData.isAmbientEnabled(player);
                boolean newValue2 = !current2;

                AtmoPlayerData.setAmbientEnabled(player, newValue2);

                if (newValue2) {
                    MessageUtils.sendInfo(player, "Ambient sounds disabled !");
                } else {
                    MessageUtils.sendSuccess(player, "Ambient sounds enabled.");
                }
                break;

            case "volume":
                if (!player.hasPermission("atmo.admin")) {
                    if (!player.hasPermission("atmo.volume")) {
                        MessageUtils.sendError(player, "You do not have permission to use this command.");
                        return true;
                    }
                }

                if (args.length < 3) {
                    MessageUtils.sendError(player, "Usage: /atmo volume <music|ambient> <0-100>");
                    return true;
                }

                String volumeType = args[1].toLowerCase();
                if (!volumeType.equals("music") && !volumeType.equals("ambient")) {
                    MessageUtils.sendError(player, "Usage: /atmo volume <music|ambient> <0-100>");
                    return true;
                }

                int volume;
                try {
                    volume = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    MessageUtils.sendError(player, "The volume must be a whole number between 0 and 100.");
                    return true;
                }

                if (volume < 0 || volume > 100) {
                    MessageUtils.sendError(player, "The volume must be between 0 and 100.");
                    return true;
                }

                if (volumeType.equals("music")) {
                    AtmoPlayerData.setMusicVolume(player, volume);
                } else {
                    AtmoPlayerData.setAmbientVolume(player, volume);
                }

                MessageUtils.sendSuccess(player, "Volume for " + volumeType + " set to " + volume + "%.");
                break;

            default:
                sendUsage(player);
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
            List<String> subCommands = getAvailableSubCommands(player);
            StringUtil.copyPartialMatches(args[0], subCommands, completions);

        } else if (args.length == 2 && args[0].equalsIgnoreCase("addloc")) {
            if (player.hasPermission("atmo.admin") || player.hasPermission("atmo.addloc")) {
                List<String> subCommands = new ArrayList<>(positionSounds.keySet());
                StringUtil.copyPartialMatches(args[1], subCommands, completions);
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("volume")) {
            if (player.hasPermission("atmo.admin") || player.hasPermission("atmo.volume")) {
                StringUtil.copyPartialMatches(args[1], Arrays.asList("music", "ambient"), completions);
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("volume")) {
            if (player.hasPermission("atmo.admin") || player.hasPermission("atmo.volume")) {
                StringUtil.copyPartialMatches(args[2], Arrays.asList("0", "25", "50", "75", "100"), completions);
            }
        }

        Collections.sort(completions);
        return completions;
    }

    private List<String> getAvailableSubCommands(Player player) {
        List<String> subCommands = new ArrayList<>();

        boolean isAdmin = player.hasPermission("atmo.admin");

        if (isAdmin || player.hasPermission("atmo.menu")) {
            subCommands.add("menu");
        }
        if (isAdmin || player.hasPermission("atmo.editor")) {
            subCommands.add("editor");
        }
        if (isAdmin || player.hasPermission("atmo.info")) {
            subCommands.add("info");
        }
        if (isAdmin || player.hasPermission("atmo.reload")) {
            subCommands.add("reload");
        }
        if (isAdmin || player.hasPermission("atmo.addloc")) {
            subCommands.add("addloc");
        }
        if (isAdmin || player.hasPermission("atmo.music") || generalConfigData.getAllowPlayerDisableMusic()) {
            subCommands.add("music");
        }
        if (isAdmin || player.hasPermission("atmo.ambient") || generalConfigData.getAllowPlayerDisableAmbient()) {
            subCommands.add("ambient");
        }
        if (isAdmin || player.hasPermission("atmo.volume")) {
            subCommands.add("volume");
        }

        return subCommands;
    }

    private static final Set<String> ADMIN_SUBCOMMANDS = Set.of("reload", "addloc", "menu", "editor", "info");

    private static final Map<String, String> SUBCOMMAND_DESCRIPTIONS = Map.of(
            "menu", "Open the settings menu",
            "editor", "Enables/disables the zone editor mode",
            "info", "Shows which zone you are currently in",
            "reload", "Reloads the Atmo configuration and zones",
            "addloc", "<name> Adds your position to a BlockSound",
            "music", "Enables or disables background music",
            "ambient", "Enables or disables ambient sounds",
            "volume", "<music|ambient> <0-100> Sets the volume"
    );

    private void sendUsage(Player player) {
        List<String> available = getAvailableSubCommands(player);

        if (available.isEmpty()) {
            MessageUtils.sendError(player, "You do not have permission to use any /atmo subcommand.");
            return;
        }

        player.sendMessage(separator());

        for (String cmd : available) {
            String description = SUBCOMMAND_DESCRIPTIONS.getOrDefault(cmd, "");
            ChatColor nameColor = ADMIN_SUBCOMMANDS.contains(cmd) ? ChatColor.RED : ChatColor.GREEN;

            player.sendMessage(ChatColor.GRAY + "/atmo " + nameColor + ChatColor.BOLD + cmd + ChatColor.RESET + ChatColor.WHITE + " (" + description + ")");
        }

        player.sendMessage(separator());
    }

    private String separator() {
        return ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "                              " + ChatColor.RESET;
    }

    private String serializeLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;

        return loc.getWorld().getName() + ", " +
                loc.getX() + ", " +
                loc.getY() + ", " +
                loc.getZ();
    }
}