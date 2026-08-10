package fr.omijica.atmo;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class AtmoPlayerData {

    private static NamespacedKey Music_Key;
    private static NamespacedKey Ambient_Key;

    public static void init(Atmo plugin) {
        Music_Key = new NamespacedKey(plugin, "music_enabled");
        Ambient_Key = new NamespacedKey(plugin, "ambient_enabled");
    }

    public static boolean isMusicEnabled(Player player) {
        Byte value = player.getPersistentDataContainer().get(Music_Key, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    public static boolean isAmbientEnabled(Player player) {
        Byte value = player.getPersistentDataContainer().get(Ambient_Key, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    public static void setMusicEnabled(Player player, boolean enabled) {
        player.getPersistentDataContainer().set(Music_Key, PersistentDataType.BYTE, (byte) (enabled ? 1 : 0));
    }

    public static void setAmbientEnabled(Player player, boolean enabled) {
        player.getPersistentDataContainer().set(Ambient_Key, PersistentDataType.BYTE, (byte) (enabled ? 1 : 0));
    }
}
