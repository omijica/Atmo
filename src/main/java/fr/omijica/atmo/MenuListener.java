package fr.omijica.atmo;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.logging.Level;

/** Relie les clics dans les inventaires Atmo aux actions de création et de gestion des zones. */
public class MenuListener implements Listener {

    private final Atmo plugin;

    public MenuListener(Atmo plugin) {
        this.plugin = plugin;
    }

    /** Vérifie l'identité du menu et les permissions avant de traiter ses boutons. */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // ignore tout inventaire qui ne possède pas le détenteur réservé aux menus Atmo.
        Inventory inventory = event.getView().getTopInventory();
        if (!(inventory.getHolder() instanceof AtmoMenu.MenuHolder holder)) {
            return;
        }

        event.setCancelled(true);
        // seuls les clics dans le menu du haut peuvent déclencher ses boutons.
        if (event.getRawSlot() < 0 || event.getRawSlot() >= inventory.getSize()) {
            return;
        }

        // bloque les actions du menu si le joueur n'a plus l'autorisation de le gérer.
        if (!player.hasPermission("atmo.admin") && !player.hasPermission("atmo.menu")) {
            MessageUtils.sendError(player, "You do not have permission to use this command.");
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
            // récupère la zone depuis le détenteur, sans découper un titre pouvant contenir le nom.
            String zoneName = holder.getZoneName();
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
                try {
                    ZoneConfig.delete(plugin.getDataFolder(), zone.getName());
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Unable to delete the zone from area.yml!", e);
                    MessageUtils.sendError(player, "Unable to delete the zone.");
                    return;
                }
                plugin.getZoneChecker().removeZone(zone.getName());
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
