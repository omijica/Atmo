package fr.omijica.atmo;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lit blockSound.yml et sépare les sources liées à des entités des sources à coordonnées fixes.
 * Les probabilités vont de 0 à 1, les délais sont en secondes et les rayons en blocs.
 */
public class BlockSoundClass {

    /**
     * Source attachée à une entité Bukkit ou à un meuble ItemsAdder selon type.
     * Pour un porte-armure, customModelData filtre le casque ; 0 désactive ce filtre.
     */
    public record EntityEntry(
            String entity,
            int customModelData,
            int radius,
            String sound,
            double chance,
            int minDelay,
            int maxDelay,
            double volume,
            double pitchVariation,
            double pitchBase,
            boolean enabled,
            String type
    ) {
        public String getEntity() { return entity; }
        public int getCustomModelData() { return customModelData; }
        public int getRadius() { return radius; }
        public String getSound() { return sound; }
        public double getChance() { return chance; }
        public int getMinDelay() { return minDelay; }
        public int getMaxDelay() { return maxDelay; }
        public double getVolume() { return volume; }
        public double getPitchVariation() { return pitchVariation; }
        public double getPitchBase() { return pitchBase; }
        public boolean getEnabled() { return enabled; }
        public String getType() {return type;}
    }

    /** Source partageant les mêmes réglages pour une liste de positions « monde, x, y, z ». */
    public record PositionEntry(
            String sound,
            int radius,
            double chance,
            int minDelay,
            int maxDelay,
            double volume,
            double pitchVariation,
            double pitchBase,
            boolean enabled,
            List<String> locations
    ) {
        public String getSound() { return sound; }
        public int getRadius() { return radius; }
        public double getChance() { return chance; }
        public int getMinDelay() { return minDelay; }
        public int getMaxDelay() { return maxDelay; }
        public double getVolume() { return volume; }
        public double getPitchVariation() { return pitchVariation; }
        public double getPitchBase() { return pitchBase; }
        public boolean getEnabled() { return enabled; }
        public List<String> getLocations() { return locations; }
    }

    /** Regroupe les deux types de sources, chacun indexé par le nom de son entrée YAML. */
    public record BlockSoundData(
            Map<String, EntityEntry> entities,
            Map<String, PositionEntry> positions
    ) {
        public Map<String, EntityEntry> getEntities() { return entities; }
        public Map<String, PositionEntry> getPositions() { return positions; }
    }

    /** Charge les entrées disponibles ; une section absente produit une collection vide. */
    public static BlockSoundData load(File dataFolder) {
        File file = new File(dataFolder, "blockSound.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Conserve l'ordre du fichier pour parcourir les sources dans un ordre prévisible.
        Map<String, EntityEntry> entities = new LinkedHashMap<>();
        Map<String, PositionEntry> positions = new LinkedHashMap<>();

        ConfigurationSection blocksSection = config.getConfigurationSection("blockSound.entities");
        if (blocksSection != null) {
            for (String key : blocksSection.getKeys(false)) {
                ConfigurationSection entrySection = blocksSection.getConfigurationSection(key);
                if (entrySection == null) continue;

                // Accepte les deux graphies présentes dans les configurations : Entity et entity.
                String entity = entrySection.getString("Entity", entrySection.getString("entity", ""));
                int customModelData = entrySection.getInt("customModelData", 0);
                int radius = entrySection.getInt("radius", 5);
                String sound = entrySection.getString("sound", "");
                double chance = entrySection.getDouble("chance", 0.0);
                int minDelay = entrySection.getInt("min_delay", 0);
                int maxDelay = entrySection.getInt("max_delay", 0);
                double volume = entrySection.getDouble("volume", 1.0);
                double pitchVariation = entrySection.getDouble("pitchVariation", 0.0);
                double pitchBase = entrySection.getDouble("pitchBase", 1.0);
                boolean enabled = entrySection.getBoolean("enabled", false);
                String type = entrySection.getString("type", "");

                entities.put(key, new EntityEntry(
                        entity, customModelData, radius, sound,
                        chance, minDelay, maxDelay, volume, pitchVariation, pitchBase, enabled, type
                ));
            }
        }

        ConfigurationSection positionsSection = config.getConfigurationSection("blockSound.positions");
        if (positionsSection != null) {
            for (String key : positionsSection.getKeys(false)) {
                ConfigurationSection entrySection = positionsSection.getConfigurationSection(key);
                if (entrySection == null) continue;

                String sound = entrySection.getString("sound", "");
                int radius = entrySection.getInt("radius", 5);
                double chance = entrySection.getDouble("chance", 0.0);
                int minDelay = entrySection.getInt("min_delay", 0);
                int maxDelay = entrySection.getInt("max_delay", 0);
                double volume = entrySection.getDouble("volume", 1.0);
                double pitchVariation = entrySection.getDouble("pitchVariation", 0.0);
                double pitchBase = entrySection.getDouble("pitchBase", 1.0);
                boolean enabled = entrySection.getBoolean("enabled", false);

                // Les coordonnées seront converties en positions Bukkit par le moteur sonore.
                List<String> locations = entrySection.getStringList("locations");
                if (locations == null) locations = new ArrayList<>();

                positions.put(key, new PositionEntry(
                        sound, radius, chance, minDelay, maxDelay, volume,
                        pitchVariation, pitchBase, enabled, locations
                ));
            }
        }

        return new BlockSoundData(entities, positions);
    }
}
