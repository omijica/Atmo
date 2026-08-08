package fr.omijica.atmo;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Atmo extends JavaPlugin {

    private ZoneListener zoneListener;
    private ZoneChecker zoneChecker;
    private AtmoMenu atmoMenu;
    private Ambient ambient;
    private AmbientManager ambientManager;
    private ConfigClass configClass;

    @Override
    public void onEnable() {

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Save default configuration files if they don't exist
        saveDefaultFiles();

        this.zoneChecker = new ZoneChecker();
        this.ambient = new Ambient();
        this.ambientManager = new AmbientManager(this);
        this.zoneListener = new ZoneListener(this, this.zoneChecker);
        this.atmoMenu = new AtmoMenu(this.zoneChecker);
        this.configClass = new ConfigClass();

        if (getCommand("atmo") != null) {
            AtmoCommand atmoCommand = new AtmoCommand(this, this.zoneChecker);
            getCommand("atmo").setExecutor(atmoCommand);
            getCommand("atmo").setTabCompleter(atmoCommand);
        }

        getServer().getPluginManager().registerEvents(this.ambientManager, this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(this.zoneListener, this);

        zoneChecker.loadZones(getDataFolder());
        getLogger().info("AtmoPlugin enabled !");

        this.ambientManager.start();
    }

    @Override
    public void onDisable() {
        getLogger().info("AtmoPlugin disabled !");
    }

    public ConfigClass getConfigClass() {return this.configClass;}

    public Ambient getAmbient() {
        return this.ambient;
    }

    public AmbientManager getAmbientManager() {return this.ambientManager;}

    public ZoneListener getZoneListener() {
        return this.zoneListener;
    }

    public AtmoMenu getAtmoMenu() {
        return this.atmoMenu;
    }

    public ZoneChecker getZoneChecker() {
        return this.zoneChecker;
    }

    private void saveDefaultFiles() {
        String[] files = {"area.yml", "ambient.yml", "blockSound.yml", "config.yml"};
        for (String fileName : files) {
            File file = new File(getDataFolder(), fileName);
            if (!file.exists()) {
                saveResource(fileName, false);
            }
        }
    }

}
