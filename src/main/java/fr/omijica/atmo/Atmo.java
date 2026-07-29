package fr.omijica.atmo;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Atmo extends JavaPlugin {

    private ZoneListener zoneListener;

    @Override
    public void onEnable() {

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        //A CHANGE AVEC DES FICHIERS AVEC LIGNES DE COMMENTAIRES ET COPIER DEPUIS LE SRV
        File file = new File(getDataFolder(), "area.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Unable to create the area.yml file !");
                e.printStackTrace();
            }
        }
        File file2 = new File(getDataFolder(), "ambient.yml");
        if (!file2.exists()) {
            try {
                file2.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Unable to create the ambient.yml file !");
                e.printStackTrace();
            }
        }


        this.zoneListener = new ZoneListener(this);

        if (getCommand("atmo") != null) {
            getCommand("atmo").setExecutor(new AtmoCommand(this));
        }

        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(this.zoneListener, this);

        getLogger().info("AtmoPlugin enabled !");
    }

    @Override
    public void onDisable() {
        getLogger().info("AtmoPlugin disabled !");
    }

    public ZoneListener getZoneListener() {
        return this.zoneListener;
    }
}
