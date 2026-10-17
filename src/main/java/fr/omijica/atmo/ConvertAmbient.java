package fr.omijica.atmo;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ConvertAmbient {

    public void convert(File dataFolder) {
        File file = new File(dataFolder, "ambient.yml");

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String key : config.getKeys(false)) {

            if (!config.isConfigurationSection(key)) {
                continue;
            }

            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            File destination = new File(dataFolder, "ambient/" + key + ".yml");

            if (destination.exists()) {
                continue;
            }

            try {
                Files.createDirectories(destination.getParentFile().toPath()); //sécurité pour le dossier parent

                YamlConfiguration configDest = new YamlConfiguration();

                configDest.set(key, config.get(key));

                configDest.save(destination);


            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}
