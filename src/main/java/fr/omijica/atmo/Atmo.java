package fr.omijica.atmo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

public final class Atmo extends JavaPlugin {

    private ZoneListener zoneListener;
    private ZoneChecker zoneChecker;

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
        File file3 = new File(getDataFolder(), "blockSound.yml");
        if (!file3.exists()) {
            try {
                file3.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Unable to create the ambient.yml file !");
                e.printStackTrace();
            }
        }

        this.zoneListener = new ZoneListener(this);
        this.zoneChecker = new ZoneChecker();

        if (getCommand("atmo") != null) {
            getCommand("atmo").setExecutor(new AtmoCommand(this, this.zoneChecker));
        }

        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(this.zoneListener, this);

        zoneChecker.loadZones(getDataFolder());
        getLogger().info("AtmoPlugin enabled !");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ZoneClass zone = zoneChecker.getPlayerZone(player);
                    if (zone == null) {
                        continue;
                    }
                }
            }
        }.runTaskTimer(this, 0L, 40L);
    }

    @Override
    public void onDisable() {
        getLogger().info("AtmoPlugin disabled !");
    }

    public ZoneListener getZoneListener() {
        return this.zoneListener;
    }
}
