package fr.omijica.atmo;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

/**
 * Conserve les préférences audio dans les données persistantes Bukkit de chaque joueur.
 * Attention : malgré les noms « Enabled », les indicateurs à true signifient actuellement
 * que le joueur a désactivé le canal correspondant. Une valeur absente laisse le canal actif.
 */
public class AtmoPlayerData {

    private static NamespacedKey Music_Key;
    private static NamespacedKey Ambient_Key;
    private static NamespacedKey AmbientVolume_Key;
    private static NamespacedKey MusicVolume_Key;

    /** Initialise les clés propres au plugin au démarrage, avant tout accès aux préférences. */
    public static void init(Atmo plugin) {
        Music_Key = new NamespacedKey(plugin, "music_enabled");
        Ambient_Key = new NamespacedKey(plugin, "ambient_enabled");
        MusicVolume_Key = new NamespacedKey(plugin, "music_volume");
        AmbientVolume_Key = new NamespacedKey(plugin, "ambient_volume");
    }

    /** Renvoie true lorsque la musique est désactivée par le joueur (convention actuelle). */
    public static boolean isMusicEnabled(Player player) {
        Byte value = player.getPersistentDataContainer().get(Music_Key, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    /** Renvoie true lorsque l'ambiance est désactivée par le joueur (convention actuelle). */
    public static boolean isAmbientEnabled(Player player) {
        Byte value = player.getPersistentDataContainer().get(Ambient_Key, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    /** Stocke l'indicateur de désactivation : true écrit 1, false écrit 0. */
    public static void setMusicEnabled(Player player, boolean enabled) {
        player.getPersistentDataContainer().set(Music_Key, PersistentDataType.BYTE, (byte) (enabled ? 1 : 0));
    }

    /** Stocke l'indicateur de désactivation de l'ambiance selon la même convention. */
    public static void setAmbientEnabled(Player player, boolean enabled) {
        player.getPersistentDataContainer().set(Ambient_Key, PersistentDataType.BYTE, (byte) (enabled ? 1 : 0));
    }

    // La commande valide les volumes entre 0 et 100 avant de les enregistrer ici.
    public static void setAmbientVolume(Player player, int vol) {
        player.getPersistentDataContainer().set(AmbientVolume_Key, PersistentDataType.INTEGER, vol);
    }

    public static void setMusicVolume(Player player, int vol) {
        player.getPersistentDataContainer().set(MusicVolume_Key, PersistentDataType.INTEGER, vol);
    }

    /** Utilise 100 % tant que le joueur n'a pas enregistré de volume musical personnel. */
    public static int getMusicVolume(Player player) {
        Integer value = player.getPersistentDataContainer().get(MusicVolume_Key, PersistentDataType.INTEGER);
        return value != null ? value : 100;
    }

    /** Utilise 100 % tant que le joueur n'a pas enregistré de volume d'ambiance personnel. */
    public static int getAmbientVolume(Player player) {
        Integer value = player.getPersistentDataContainer().get(AmbientVolume_Key, PersistentDataType.INTEGER);
        return value != null ? value : 100;
    }
}
