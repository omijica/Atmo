package fr.omijica.atmo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class AtmoCommand implements CommandExecutor {

    MiniMessage mm = MiniMessage.miniMessage(); //couleur

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

            default:
                sender.sendMessage(mm.deserialize("<red>Usage: /atmo"));
                break;

        }

        return true;

    }

}
