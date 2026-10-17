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

/** Relie les clics dans les inventaires Atmo aux actions de création et de gestion des zones. */
public class MenuListener implements Listener {

    private final Atmo plugin;

    public MenuListener(Atmo plugin) {
        this.plugin = plugin;
    }

    /** Identifie le menu par son titre et réserve les clics à ses boutons. */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Retire les styles Adventure pour comparer le texte aux titres définis dans AtmoMenu.
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (title.equals("Atmo Menu")) {
            event.setCancelled(true);

            if (event.getSlot() == 12) {
                AtmoMenu.openAnvilMenu(plugin, player);
                MessageUtils.playClick(player);
            }

            else if (event.getSlot() == 14) {
                plugin.getAtmoMenu().openPaperMenu(player, 0);
                MessageUtils.playClick(player);
            }
        } else if (title.equals("Atmo Menu (AREAS)")) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();

            if (item != null && (item.getType() == Material.MAP)) {
                ItemMeta meta = item.getItemMeta();

                if (meta != null && meta.hasDisplayName()) {
                    Component displayNameComponent = meta.displayName();
                    // Le nom affiché sur la carte correspond au nom de la zone à éditer.
                    String name = PlainTextComponentSerializer.plainText().serialize(displayNameComponent);

                    plugin.getAtmoMenu().openZoneEditMenu(player, name);
                    MessageUtils.playClick(player);
                }
            }

            // L'index brut distingue les boutons du menu des cases de l'inventaire du joueur.
            int activeSlot = event.getRawSlot();
            int activePage = plugin.getAtmoMenu().getPlayerPage().getOrDefault(player.getUniqueId(), 0);

            if (activeSlot == 52) {
                plugin.getAtmoMenu().openPaperMenu(player, activePage - 1);
                MessageUtils.playClick(player);
            } else if (activeSlot == 53) {
                plugin.getAtmoMenu().openPaperMenu(player, activePage + 1);
                MessageUtils.playClick(player);
            }
        } else if (title.startsWith("Atmo - ") && title.endsWith(" Settings")) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();

            int slot = event.getRawSlot();
            String zoneName = title.replace("Atmo - ", "").replace(" Settings", "");
            ZoneClass zone = plugin.getZoneChecker().getZoneByName(zoneName);

            if (zone == null) {
                MessageUtils.sendError(player, "Zone not found.");
                return;
            }

            // Case 11 : téléportation au centre calculé de la zone.
            if (slot == 11) {
                Location target = zone.getCenterLocation();
                if (target != null) {
                    player.teleport(target);
                    MessageUtils.playClick(player);
                } else {
                    MessageUtils.sendError(player, "Teleport point not found.");
                }
                player.closeInventory();

            }
            // Case 13 : nouvelle sélection des deux coins pour la zone existante.
            else if (slot == 13) {
                plugin.getZoneListener().enableCreationMode(player, zoneName);
                MessageUtils.playClick(player);
                player.closeInventory();
            }
            // Case 15 : suppression puis retour à la page consultée.
            else if (slot == 15) {
                plugin.getZoneChecker().deleteZone(plugin.getDataFolder(), zoneName);
                MessageUtils.sendSuccess(player, "Zone " + zoneName + " deleted!");

                int activePage = plugin.getAtmoMenu().getPlayerPage().getOrDefault(player.getUniqueId(), 0);
                plugin.getAtmoMenu().openPaperMenu(player, activePage);
            }
            // Case 18 : retour à la liste des zones.
            else if (slot == 18) {
                int activePage = plugin.getAtmoMenu().getPlayerPage().getOrDefault(player.getUniqueId(), 0);
                plugin.getAtmoMenu().openPaperMenu(player, activePage);
                MessageUtils.playClick(player);
            }
        }
    }
}
