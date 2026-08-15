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
            String gradientName = ansiGradient("ItemsAdder", 255, 105, 180, 148, 0, 211); // rose -> violet
            String aqua = "\u001B[38;2;85;255;255m";
            String reset = "\u001B[0m";

            getLogger().info(gradientName + aqua + " dependency found and initialized." + reset);
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

    private String ansiGradient(String text, int fromR, int fromG, int fromB, int toR, int toG, int toB) {
        StringBuilder sb = new StringBuilder();
        int length = text.length();
        for (int i = 0; i < length; i++) {
            float ratio = length <= 1 ? 0 : (float) i / (length - 1);
            int r = Math.round(fromR + ratio * (toR - fromR));
            int g = Math.round(fromG + ratio * (toG - fromG));
            int b = Math.round(fromB + ratio * (toB - fromB));
            sb.append("\u001B[38;2;").append(r).append(";").append(g).append(";").append(b).append("m")
                    .append(text.charAt(i));
        }
        return sb.toString();
    }
}
