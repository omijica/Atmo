package fr.omijica.atmo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AtmoCommand implements CommandExecutor {

    MiniMessage mm = MiniMessage.miniMessage(); //couleur

    @Override
    public boolean onCommand(CommandSender player, Command command, String label, String[] args) {

        if (!player.hasPermission("atmo.use")) {
            player.sendMessage(mm.deserialize("<red>You do not have permission to use this command."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(mm.deserialize("<red>Usage: /atmo"));
            return true;
        }

        String arg1 = args[0].toLowerCase();

        switch (arg1) {

            case "reload":
                break;

            default:
                player.sendMessage(mm.deserialize("<red>Usage: /atmo"));
                break;

        }

        return true;

    }

}
