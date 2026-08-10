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
                double chance = toDouble(map.get("chance"), 0.0);
                int minDelay = toInt(map.get("min_delay"), 0);
                int maxDelay = toInt(map.get("max_delay"), 0);
                double volume = toDouble(map.get("volume"), 1.0);
                boolean pitchVariation = toBoolean(map.get("pitch_variation"), false);

                randomSounds.add(new RandomSoundData(sound, chance, minDelay, maxDelay, volume, pitchVariation));
            }

            ambiances.put(key, new AmbientData(bgSound, bgVolume, bgInterval, List.copyOf(randomSounds)));
        }

        return ambiances;
    }

    // Convertit n'importe quel Number YAML (Integer, Double, Long...) en double, avec une valeur par défaut si absent/invalide
    private static double toDouble(Object raw, double defaultValue) {
        if (raw instanceof Number number) {
            return number.doubleValue();
        }
        return defaultValue;
    }

    // Convertit n'importe quel Number YAML en int, avec une valeur par défaut si absent/invalide
    private static int toInt(Object raw, int defaultValue) {
        if (raw instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }

    // Convertit en boolean, avec une valeur par défaut si absent/invalide
    private static boolean toBoolean(Object raw, boolean defaultValue) {
        if (raw instanceof Boolean bool) {
            return bool;
        }
        return defaultValue;
    }
}