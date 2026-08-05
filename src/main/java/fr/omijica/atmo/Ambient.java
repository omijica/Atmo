package fr.omijica.atmo;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ambient {

    public record RandomSoundData (
        String sound,
        double chance,
        int minDelay,
        int maxDelay,
        double volume,
        boolean pitchVariation
    ) {}

    public record AmbientData(
        String backgroundSound,
        double backgroundVolume,
        int backgroundInterval,
        List<RandomSoundData> randomSounds
    ) {}

    public static Map<String, AmbientData> load(File dataFolder) {
        File file = new File(dataFolder, "ambient.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Map<String, AmbientData> ambiances = new HashMap<>();

        for (String key : config.getKeys(false)) {
            if (!config.isConfigurationSection(key)) {
                continue;
            }

            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            String bgSound = section.getString("background_sound");
            double bgVolume = section.getDouble("background_volume");
            int bgInterval = section.getInt("background_interval");

            List<RandomSoundData> randomSounds = new ArrayList<>();
            List<Map<?, ?>> rawList = section.getMapList("random_sounds");
            for (Map<?, ?> map : rawList) {
                String sound = (String) map.get("sound");
                double chance = (double) map.get("chance");
                int minDelay = (int) map.get("min_delay");
                int maxDelay = (int) map.get("max_delay");
                double volume = (double) map.get("volume");
                boolean pitchVariation = (boolean) map.get("pitch_variation");

                randomSounds.add(new RandomSoundData(sound, chance, minDelay, maxDelay, volume, pitchVariation));
            }

            ambiances.put(key, new AmbientData(bgSound, bgVolume, bgInterval, List.copyOf(randomSounds)));
        }

        return ambiances;
    }
}
