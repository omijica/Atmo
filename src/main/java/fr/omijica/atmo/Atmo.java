package fr.omijica.atmo;

import org.bukkit.plugin.java.JavaPlugin;

public final class Atmo extends JavaPlugin {

    @Override
    public void onEnable() {
        if (getCommand("atmo") != null) {
            getCommand("atmo").setExecutor(new AtmoCommand());
        }

        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        getLogger().info("AtmoPlugin enabled !");
    }

    @Override
    public void onDisable() {
        getLogger().info("AtmoPlugin disabled !");
    }
}
