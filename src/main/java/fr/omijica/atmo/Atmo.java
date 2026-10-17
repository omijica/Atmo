package fr.omijica.atmo;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Point d'entrée du plugin Paper : prépare les fichiers, les services et les événements,
 * puis lance la mise à jour périodique des sons pour les joueurs connectés.
 */
public final class Atmo extends JavaPlugin {

    private ZoneListener zoneListener;
    private ZoneChecker zoneChecker;
    private AtmoMenu atmoMenu;
    private SoundManager soundManager;
    private boolean itemsAdderEnabled = false;

    @Override
    public void onEnable() {

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Copie les configurations fournies dans le JAR uniquement si elles sont absentes.
        saveDefaultFiles();

        // L'intégration aux meubles personnalisés reste facultative.
        itemsAdderEnabled = Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");
        if (itemsAdderEnabled) {
            String gradientName = ansiGradient("ItemsAdder", 255, 105, 180, 148, 0, 211); // rose -> violet
            String aqua = "\u001B[38;2;85;255;255m";
            String reset = "\u001B[0m";

            getLogger().info(gradientName + aqua + " dependency found and initialized." + reset);
        }

        // Crée d'abord les services dont dépendent le moteur sonore, les menus et les commandes.
        this.zoneChecker = new ZoneChecker();
        this.soundManager = new SoundManager(this);
        this.zoneListener = new ZoneListener(this, this.zoneChecker);
        this.atmoMenu = new AtmoMenu(this.zoneChecker);
        // Les clés de stockage doivent exister avant la première lecture des préférences.
        AtmoPlayerData.init(this);

        if (getCommand("atmo") != null) {
            AtmoCommand atmoCommand = new AtmoCommand(this, this.zoneChecker);
            getCommand("atmo").setExecutor(atmoCommand);
            getCommand("atmo").setTabCompleter(atmoCommand);
        }

        getServer().getPluginManager().registerEvents(this.soundManager, this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(this.zoneListener, this);

        // Charge les zones avant de lancer la boucle sonore, qui les consultera à chaque passage.
        zoneChecker.setZones(ZoneConfig.load(getDataFolder()));
        getLogger().info("AtmoPlugin enabled !");

        this.soundManager.start();
    }

    @Override
    public void onDisable() {
        // Demande l'arrêt des sons suivis par le moteur lors de la désactivation du plugin.
        this.soundManager.stop();
        getLogger().info("AtmoPlugin disabled !");
    }

    public boolean getItemsAdderEnabled() {return itemsAdderEnabled;}

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

    /** Préserve les réglages existants en ne créant que les fichiers manquants. */
    private void saveDefaultFiles() {
        String[] files = {"area.yml", "ambient.yml", "blockSound.yml", "config.yml"};
        for (String fileName : files) {
            File file = new File(getDataFolder(), fileName);
            if (!file.exists()) {
                saveResource(fileName, false);
            }
        }
    }

    /** Applique un dégradé RVB au texte destiné à la console, caractère par caractère. */
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
