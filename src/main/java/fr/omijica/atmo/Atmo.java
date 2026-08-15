package fr.omijica.atmo;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Atmo extends JavaPlugin {

    private ZoneListener zoneListener;
    private ZoneChecker zoneChecker;
    private AtmoMenu atmoMenu;
    private Ambient ambient;
    private SoundManager soundManager;
    private ConfigClass configClass;
    private BlockSoundClass blockSoundClass;
    private boolean itemsAdderEnabled = false;

    @Override
    public void onEnable() {

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Save default configuration files if they don't exist
        saveDefaultFiles();

        itemsAdderEnabled = Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");
        if (itemsAdderEnabled) {
            getLogger().info("ItemsAdder dependency found and initialized.");
        }

        this.zoneChecker = new ZoneChecker();
        this.ambient = new Ambient();
        this.soundManager = new SoundManager(this);
        this.zoneListener = new ZoneListener(this, this.zoneChecker);
        this.atmoMenu = new AtmoMenu(this.zoneChecker);
        this.configClass = new ConfigClass();
        this.blockSoundClass = new BlockSoundClass();
        AtmoPlayerData.init(this);

        if (getCommand("atmo") != null) {
            AtmoCommand atmoCommand = new AtmoCommand(this, this.zoneChecker);
            getCommand("atmo").setExecutor(atmoCommand);
            getCommand("atmo").setTabCompleter(atmoCommand);
        }

        getServer().getPluginManager().registerEvents(this.soundManager, this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(this.zoneListener, this);

        zoneChecker.loadZones(getDataFolder());
        getLogger().info("AtmoPlugin enabled !");

        this.soundManager.start();
    }

    @Override
    public void onDisable() {
        this.soundManager.stop();
        getLogger().info("AtmoPlugin disabled !");
    }

    public boolean getItemsAdderEnabled() {return itemsAdderEnabled;}

    public ConfigClass getConfigClass() {return this.configClass;}

    public Ambient getAmbient() {
        return this.ambient;
    }

    public SoundManager getSoundManager() {return this.soundManager;}

    public ZoneListener getZoneListener() {
        return this.zoneListener;
    }

    public AtmoMenu getAtmoMenu() {
        return this.atmoMenu;
    }

    public ZoneChecker getZoneChecker() {
        return this.zoneChecker;
    }

    public BlockSoundClass getBlockSoundClass() {return this.blockSoundClass;}

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
