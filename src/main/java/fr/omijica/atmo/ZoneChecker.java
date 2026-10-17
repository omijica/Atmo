package fr.omijica.atmo;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Maintient les zones en mémoire et détermine laquelle s'applique à la position d'un joueur. */
public class ZoneChecker {

    private final List<ZoneClass> zones = new ArrayList<>();

    /** Remplace le contenu en mémoire par les zones présentes dans area.yml. */
    public void loadZones(File dataFolder) {

        zones.clear();
        File fichier = new File(dataFolder, "area.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(fichier);

        var section = config.getConfigurationSection("areas");
        if (section == null) return;

        for (String cle : section.getKeys(false)) {
            String chemin = "areas." + cle;

            String world = config.getString(chemin + ".world");
            double x1 = config.getDouble(chemin + ".pos1.x");
            double y1 = config.getDouble(chemin + ".pos1.y");
            double z1 = config.getDouble(chemin + ".pos1.z");
            double x2 = config.getDouble(chemin + ".pos2.x");
            double y2 = config.getDouble(chemin + ".pos2.y");
            double z2 = config.getDouble(chemin + ".pos2.z");
            int priority = config.getInt(chemin + ".priority");
            String ambientName = config.getString(chemin + ".ambient.name");
            boolean ambientEnabled = config.getBoolean(chemin + ".ambient.enabled");
            String musicName = config.getString(chemin + ".music.name");
            boolean musicEnabled = config.getBoolean(chemin + ".music.enabled");
            int musicVolume = config.getInt(chemin + ".music.volume");
            int musicDuration = config.getInt(chemin + ".music.duration");

            zones.add(new ZoneClass(cle, world, x1, y1, z1, x2, y2, z2, priority, ambientName, ambientEnabled, musicName, musicEnabled, musicVolume, musicDuration));
        }
    }

    public List<ZoneClass> getZones() { return zones; }

    /** Recherche un nom sans distinguer les majuscules ; renvoie null s'il est introuvable. */
    public ZoneClass getZoneByName(String name) {
        return zones.stream().filter(zone -> zone.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /** Retire la section YAML puis la zone en mémoire ; les erreurs de sauvegarde sont journalisées. */
    public void deleteZone(File dataFolder, String name) {
        File file = new File(dataFolder, "area.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("areas." + name, null);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        zones.removeIf(zone -> zone.getName().equalsIgnoreCase(name));
    }

    /**
     * Retient la zone contenant le joueur avec la plus grande priorité numérique.
     * À priorité égale, la première zone chargée reste sélectionnée ; hors zone, renvoie null.
     */
    public ZoneClass getPlayerZone(org.bukkit.entity.Player player) {
        ZoneClass bestZone = null;

        for (ZoneClass zone : zones) {
            if (zone.contains(player.getLocation())) {
                if (bestZone == null || zone.getPriority() > bestZone.getPriority()) {
                    bestZone = zone;
                }
            }
        }
        return bestZone;
    }

}


