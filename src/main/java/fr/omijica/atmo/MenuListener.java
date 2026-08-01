package fr.omijica.atmo;

import org.bukkit.event.Listener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class MenuListener implements Listener {

    private final Atmo plugin;
    private final AtmoMenu atmoMenu;

    public MenuListener(Atmo plugin, AtmoMenu atmoMenu) {
        this.plugin = plugin;
        this.atmoMenu = atmoMenu;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (title.equals("Atmo Menu")) {
            event.setCancelled(true);

            if (event.getSlot() == 12) {
                AtmoMenu.openAnvilMenu(plugin, player);
            }

            else if (event.getSlot() == 14) {
                plugin.getAtmoMenu().openPaperMenu(player, 0);
            }
        } else if (title.equals("Atmo Menu (AREAS)")) {
            event.setCancelled(true);

            int activeSlot = event.getRawSlot();
            int activePage = atmoMenu.getPlayerPage().getOrDefault(player.getUniqueId(), 0);

            if (activeSlot == 48) {
                plugin.getAtmoMenu().openPaperMenu(player, activePage - 1);
            } else if (activeSlot == 50) {
                plugin.getAtmoMenu().openPaperMenu(player, activePage + 1);
            }
        }
    }
}
