package fr.omijica.atmo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
            String zoneName = title.replace("Atmo - ", "").replace(" Settings", "");
            ZoneClass zone = plugin.getZoneChecker().getZoneByName(zoneName);

            if (zone == null) {
                player.sendMessage(Component.text("Zone not found.", NamedTextColor.RED));
                return;
            }

            // Slot 11: Teleport
            if (slot == 11) {
                Location target = zone.getCenterLocation();
                if (target != null) {
                    player.teleport(target);
                } else {
                    player.sendMessage(Component.text("Teleport point not found.", NamedTextColor.RED));
                }
                player.closeInventory();

            }
            // Slot 13: Redefine
            else if (slot == 13) {
                plugin.getZoneListener().enableCreationMode(player, zoneName);
                player.closeInventory();
            }
            // Slot 15: Delete
            else if (slot == 15) {
                plugin.getZoneChecker().deleteZone(plugin.getDataFolder(), zoneName);
                player.sendMessage(Component.text("Zone " + zoneName + " deleted!", NamedTextColor.RED));

                int activePage = plugin.getAtmoMenu().getPlayerPage().getOrDefault(player.getUniqueId(), 0);
                plugin.getAtmoMenu().openPaperMenu(player, activePage);
            }
            // Slot 18: Back
            else if (slot == 18) {
                int activePage = plugin.getAtmoMenu().getPlayerPage().getOrDefault(player.getUniqueId(), 0);
                plugin.getAtmoMenu().openPaperMenu(player, activePage);
            }
        }
    }
}
