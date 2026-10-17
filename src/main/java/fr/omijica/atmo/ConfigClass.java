package fr.omijica.atmo;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Lit les options globales et les réglages sonores appliqués en dehors des zones définies. */
public class ConfigClass {

    /** Activation de l'ambiance et nom du profil correspondant dans ambient.yml. */
    public record AmbientSettings(
            boolean enabled,
            String name
    ) {
        public boolean getEnabled() { return enabled; }
        public String getName() { return name; }
    }

    /** Réglages musicaux : clé du son, volume en pourcentage et durée en secondes. */
    public record MusicSettings(
            boolean enabled,
            String name,
            int volume,
            int duration
    ) {
        public boolean getEnabled() { return enabled; }
        public String getName() { return name; }
        public int getVolume() { return volume; }
        public int getDuration() { return duration; }
    }

    /** Ambiance et musique de repli, limitées aux mondes explicitement listés. */
    public record GeneralAreaData(
            AmbientSettings ambient,
            MusicSettings music,
            List<String> worlds
    ) {
        public AmbientSettings getAmbient() { return ambient; }
        public MusicSettings getMusic() { return music; }
        public List<String> getWorlds() { return worlds; }
    }

    /** Charge config.general_area ; une section absente désactive les sons de repli. */
    public static GeneralAreaData load(File dataFolder) {
        File file = new File(dataFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection section = config.getConfigurationSection("config.general_area");
        if (section == null) {
            return new GeneralAreaData(
                    new AmbientSettings(false, ""),
                    new MusicSettings(false, "", 0, 0),
                    new ArrayList<>()
            );
        }

        boolean ambientEnabled = section.getBoolean("ambient.enabled", false);
        String ambientName = section.getString("ambient.name", "");

        boolean musicEnabled = section.getBoolean("music.enabled", false);
        String musicName = section.getString("music.name", "");
        int musicVolume = section.getInt("music.volume", 100);
        int musicDuration = section.getInt("music.duration", 0);

        List<String> worlds = section.getStringList("worlds");
        if (worlds == null) worlds = new ArrayList<>();

        return new GeneralAreaData(
                new AmbientSettings(ambientEnabled, ambientName),
                new MusicSettings(musicEnabled, musicName, musicVolume, musicDuration), worlds
        );
    }

    /**
     * Charge les autorisations globales de désactivation des canaux audio.
     * Par défaut, chaque joueur peut les désactiver sans permission spécifique.
     */
    public static GeneralConfigData loadBaseConfig(File dataFolder) {
        File file = new File(dataFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection section = config.getConfigurationSection("config.default");

        if (section == null) {
            return new GeneralConfigData(true, true);
        }

        boolean allowPlayerDisableMusic = section.getBoolean("AllowPlayerDisableMusic", true);
        boolean allowPlayerDisableAmbient = section.getBoolean("AllowPlayerDisableAmbient", true);

        return new GeneralConfigData(allowPlayerDisableMusic, allowPlayerDisableAmbient);
    }

    /** Options consultées par les commandes music et ambient et par leur autocomplétion. */
    public record GeneralConfigData(
            boolean allowPlayerDisableMusic,
            boolean allowPlayerDisableAmbient
    ) {
        public boolean getAllowPlayerDisableMusic() { return allowPlayerDisableMusic; }
        public boolean getAllowPlayerDisableAmbient() { return allowPlayerDisableAmbient; }
    }
}
