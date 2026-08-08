package fr.omijica.atmo;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BlockSoundClass {

    public record BlockEntry(
            String type,
            String entity,
            String block,
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
        public String getType() { return type; }
        public String getEntity() { return entity; }
        public String getBlock() { return block; }
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
            Map<String, BlockEntry> blocks,
            Map<String, PositionEntry> positions
    ) {
        public Map<String, BlockEntry> getBlocks() { return blocks; }
        public Map<String, PositionEntry> getPositions() { return positions; }
    }

    public static BlockSoundData load(File dataFolder) {
        File file = new File(dataFolder, "blockSound.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, BlockEntry> blocks = new LinkedHashMap<>();
        Map<String, PositionEntry> positions = new LinkedHashMap<>();

        ConfigurationSection blocksSection = config.getConfigurationSection("blockSound.blocks");
        if (blocksSection != null) {
            for (String key : blocksSection.getKeys(false)) {
                ConfigurationSection entrySection = blocksSection.getConfigurationSection(key);
                if (entrySection == null) continue;

                String type = entrySection.getString("Type", "");
                String entity = entrySection.getString("Entity", entrySection.getString("entity", ""));
                String block = entrySection.getString("block", null);
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

                blocks.put(key, new BlockEntry(
                        type, entity, block, customModelData, radius, sound,
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

        return new BlockSoundData(blocks, positions);
    }
}