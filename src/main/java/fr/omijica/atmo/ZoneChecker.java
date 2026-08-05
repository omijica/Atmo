package fr.omijica.atmo;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZoneChecker {

    private final List<ZoneClass> zones = new ArrayList<>();

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
            String ambientName = config.getString(chemin + "ambient.name");
            boolean ambientEnabled = config.getBoolean(chemin + "ambient.enabled");

            zones.add(new ZoneClass(cle, world, x1, y1, z1, x2, y2, z2, priority, ambientName, ambientEnabled));
        }
    }

    public List<ZoneClass> getZones() { return zones; }

    public ZoneClass getZoneByName(String name) {
        return zones.stream().filter(zone -> zone.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

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


