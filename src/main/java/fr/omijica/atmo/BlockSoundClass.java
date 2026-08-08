package fr.omijica.atmo;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BlockSoundClass {

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
            boolean enabled
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
    }

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

    public record BlockSoundData(
            Map<String, EntityEntry> entities,
            Map<String, PositionEntry> positions
    ) {
        public Map<String, EntityEntry> getEntities() { return entities; }
        public Map<String, PositionEntry> getPositions() { return positions; }
    }

    public static BlockSoundData load(File dataFolder) {
        File file = new File(dataFolder, "blockSound.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, EntityEntry> entities = new LinkedHashMap<>();
        Map<String, PositionEntry> positions = new LinkedHashMap<>();

        ConfigurationSection blocksSection = config.getConfigurationSection("blockSound.entities");
        if (blocksSection != null) {
            for (String key : blocksSection.getKeys(false)) {
                ConfigurationSection entrySection = blocksSection.getConfigurationSection(key);
                if (entrySection == null) continue;

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

                entities.put(key, new EntityEntry(
                        entity, customModelData, radius, sound,
                        chance, minDelay, maxDelay, volume, pitchVariation, pitchBase, enabled
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