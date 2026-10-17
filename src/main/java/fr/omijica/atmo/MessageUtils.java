package fr.omijica.atmo;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/** Uniformise les messages du plugin : préfixe, couleurs MiniMessage et retour sonore. */
public class MessageUtils {

    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final String PREFIX = "<gradient:#4facfe:#00f2fe><b>ATMO</b></gradient> <dark_gray>»</dark_gray> ";

    /** Affiche un message général en gris ; les balises MiniMessage du texte restent interprétées. */
    public static void sendMessage(Player player, String message) {
        player.sendMessage(mm.deserialize(PREFIX + "<gray>" + message));
        playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1f);
    }

    /** Signale une réussite en vert avec le son de récupération d'expérience. */
    public static void sendSuccess(Player player, String message) {
        player.sendMessage(mm.deserialize(PREFIX + "<green>" + message));
        playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
    }

    /** Signale une erreur en rouge avec un son de refus. */
    public static void sendError(Player player, String message) {
        player.sendMessage(mm.deserialize(PREFIX + "<red>" + message));
        playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }

    /** Affiche une information en bleu clair avec un carillon. */
    public static void sendInfo(Player player, String message) {
        player.sendMessage(mm.deserialize(PREFIX + "<aqua>" + message));
        playSound(player, Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1f);
    }

    /** Joue le retour sonore à la position du joueur, uniquement pour ce joueur. */
    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /** Retour sonore commun aux boutons des menus. */
    public static void playClick(Player player) {
        playSound(player, Sound.UI_BUTTON_CLICK, 0.5f, 1f);
    }
}
