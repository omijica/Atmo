package fr.omijica.atmo;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class MenuListener implements Listener {

    private final Atmo plugin;

    public MenuListener(Atmo plugin) {
        this.plugin = plugin;
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

            ItemStack item = event.getCurrentItem();

            if (item != null && (item.getType() == Material.MAP)) {
                ItemMeta meta = item.getItemMeta();

                if (meta != null && meta.hasDisplayName()) {
                    Component displayNameComponent = meta.displayName();
                    String name = PlainTextComponentSerializer.plainText().serialize(displayNameComponent);

                    plugin.getAtmoMenu().openZoneEditMenu(player, name);
                }
            }

            int activeSlot = event.getRawSlot();
            int activePage = plugin.getAtmoMenu().getPlayerPage().getOrDefault(player.getUniqueId(), 0);

            if (activeSlot == 52) {
                plugin.getAtmoMenu().openPaperMenu(player, activePage - 1);
            } else if (activeSlot == 53) {
                plugin.getAtmoMenu().openPaperMenu(player, activePage + 1);
            }
        } else if (title.startsWith("Atmo - ") && title.endsWith(" Settings")) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();

            int slot = event.getRawSlot();

            // Slot 11: Teleport
            if (slot == 11) {
            }
            // Slot 13: Redefine
            else if (slot == 13) {
            }
            // Slot 15: Delete
            else if (slot == 15) {
            }
            // Slot 18: Back
            else if (slot == 18) {
                int activePage = plugin.getAtmoMenu().getPlayerPage().getOrDefault(player.getUniqueId(), 0);
                plugin.getAtmoMenu().openPaperMenu(player, activePage);
            }
        }
    }
}
