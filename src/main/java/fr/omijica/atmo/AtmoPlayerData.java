package fr.omijica.atmo;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class AtmoPlayerData {

    private static NamespacedKey Music_Key;
    private static NamespacedKey Ambient_Key;
    private static NamespacedKey AmbientVolume_Key;
    private static NamespacedKey MusicVolume_Key;

    public static void init(Atmo plugin) {
        Music_Key = new NamespacedKey(plugin, "music_enabled");
        Ambient_Key = new NamespacedKey(plugin, "ambient_enabled");
        MusicVolume_Key = new NamespacedKey(plugin, "music_volume");
        AmbientVolume_Key = new NamespacedKey(plugin, "ambient_volume");
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

    public static void setAmbientVolume(Player player, int vol) {
        player.getPersistentDataContainer().set(AmbientVolume_Key, PersistentDataType.INTEGER, vol);
    }

    public static void setMusicVolume(Player player, int vol) {
        player.getPersistentDataContainer().set(MusicVolume_Key, PersistentDataType.INTEGER, vol);
    }

    public static int getMusicVolume(Player player) {
        Integer value = player.getPersistentDataContainer().get(MusicVolume_Key, PersistentDataType.INTEGER);
        return value != null ? value : 100;
    }

    public static int getAmbientVolume(Player player) {
        Integer value = player.getPersistentDataContainer().get(AmbientVolume_Key, PersistentDataType.INTEGER);
        return value != null ? value : 100;
    }
}
