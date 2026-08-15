package fr.omijica.atmo;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigClass {

    public record AmbientSettings(
            boolean enabled,
            String name
    ) {
        public boolean getEnabled() { return enabled; }
        public String getName() { return name; }
    }

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

    public record GeneralAreaData(
            AmbientSettings ambient,
            MusicSettings music,
            List<String> worlds
    ) {
        public AmbientSettings getAmbient() { return ambient; }
        public MusicSettings getMusic() { return music; }
        public List<String> getWorlds() { return worlds; }
    }

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

    public record GeneralConfigData(
            boolean allowPlayerDisableMusic,
            boolean allowPlayerDisableAmbient
    ) {
        public boolean getAllowPlayerDisableMusic() { return allowPlayerDisableMusic; }
        public boolean getAllowPlayerDisableAmbient() { return allowPlayerDisableAmbient; }
    }
}