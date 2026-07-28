package fr.omijica.atmo;

import org.bukkit.event.Listener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;


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
        if (!title.equals("Atmo Menu")) {
            return;
        }

        event.setCancelled(true);

        if (event.getSlot() == 12) {
            AtmoMenu.openAnvilMenu(plugin, player);
        }
    }
}
