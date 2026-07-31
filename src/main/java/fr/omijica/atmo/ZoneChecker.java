package fr.omijica.atmo;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ZoneChecker {

    private final List<ZoneClass> zones = new ArrayList<>();

    public void loadZones(File dataFolder) {

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

            zones.add(new ZoneClass(cle, world, x1, y1, z1, x2, y2, z2, priority));
        }
    }

    public List<ZoneClass> getZones() { return zones; }

    public ZoneClass getPlayerZone(org.bukkit.entity.Player player) {
        ZoneClass bestZone = null;

        for (ZoneClass zone : zones) { //loop les zones
            if (zone.contains(player.getLocation())) {
                if (bestZone == null || zone.getPriority() > bestZone.getPriority()) {
                    bestZone = zone;
                }
            }
        }
        return bestZone;
    }

}


