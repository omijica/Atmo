package fr.omijica.atmo;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Charge les profils d'ambiance d'ambient.yml, composés d'un fond et de sons aléatoires. */
public final class Ambient {

    private Ambient() {}

    /**
     * Son ponctuel : probabilité entre 0 et 1, délais en secondes et volume multiplicateur.
     * La hauteur varie autour de pitchBase dans la limite de pitchVariation.
     */
    public record RandomSoundData (
            String sound,
            double chance,
            int minDelay,
            int maxDelay,
            double volume,
            double pitchVariation,
            double pitchBase
    ) {}

    /** Son de fond, intervalle de répétition en secondes et liste des sons ponctuels associés. */
    public record AmbientData(
            String backgroundSound,
            double backgroundVolume,
            int backgroundInterval,
            List<RandomSoundData> randomSounds
    ) {
        public AmbientData {
            randomSounds = List.copyOf(randomSounds);
        }
    }

    /** Indexe les profils par leur nom, utilisé ensuite dans area.yml et config.yml. */
    public static Map<String, AmbientData> load(File dataFolder) {

        File folder = new File(dataFolder, "ambient");
        File[] files = folder.listFiles(file -> file.isFile() && file.getName().endsWith(".yml"));
        Map<String, AmbientData> ambiances = new HashMap<>();

        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

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
                    double pitchVariation = toDouble(map.get("pitch_variation"), 0.0);
                    double pitchBase = toDouble(map.get("pitch_base"), 1.0);

                    randomSounds.add(new RandomSoundData(sound, chance, minDelay, maxDelay, volume, pitchVariation, pitchBase));
                }

                ambiances.put(key, new AmbientData(bgSound, bgVolume, bgInterval, randomSounds));
            }
        }

        return Collections.unmodifiableMap(ambiances);
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

}
