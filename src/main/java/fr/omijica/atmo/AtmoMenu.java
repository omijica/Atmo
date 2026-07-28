package fr.omijica.atmo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.wesjd.anvilgui.AnvilGUI;

import java.util.Collections;
import java.util.List;

public class AtmoMenu {

    public static void openMenu(Player player) {
        Component title = Component.text("Atmo Menu", NamedTextColor.GOLD, TextDecoration.BOLD);
        Inventory gui = Bukkit.createInventory(null, 27, title);

        ItemStack glassFiller = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassFiller.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(Component.empty());
            glassFiller.setItemMeta(glassMeta);
        }

        for (int i=0; i < gui.getSize(); i++) {
            gui.setItem(i,glassFiller);
        }

        //TRIPWIRE
        ItemStack tripwireItem = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta tripwireMeta = tripwireItem.getItemMeta();
        if (tripwireMeta != null) {
            tripwireMeta.displayName(Component.text("Create an area.", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
            tripwireMeta.lore(List.of(Component.text("Click here", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)));

            tripwireItem.setItemMeta(tripwireMeta);
        }
        gui.setItem(12, tripwireItem);

        //PAPER
        ItemStack paperItem = new ItemStack(Material.PAPER);
        ItemMeta paperMeta = paperItem.getItemMeta();
        if (paperMeta != null) {
            paperMeta.displayName(Component.text("Edit an area.", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
            paperMeta.lore(List.of(Component.text("Click here", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)));

            paperItem.setItemMeta(paperMeta);
        }
        gui.setItem(14, paperItem);

        player.openInventory(gui);
    }

    public static void openAnvilMenu(Atmo plugin, Player player) {
        new AnvilGUI.Builder().onClick((slot, stateSnapshot) -> {

                if (slot != AnvilGUI.Slot.OUTPUT) {
                    return Collections.emptyList();
                }

                String zoneName = stateSnapshot.getText();

                if (zoneName == null || zoneName.isBlank() || zoneName.equals("Area Name")) {
                    player.sendMessage(Component.text("Please enter a valid zone name.", NamedTextColor.RED));
                    return List.of(AnvilGUI.ResponseAction.replaceInputText("Area Name"));
                }

                player.sendMessage(Component.text("Zone \"" + zoneName + "\" successfully created!", NamedTextColor.GREEN));
                // saveZone(player, zoneName);
                return List.of(AnvilGUI.ResponseAction.close());
        })
        .title("Enter area's name.")
        .text("Area Name")
        .itemLeft(new ItemStack(Material.NAME_TAG))
        .plugin(plugin)
        .open(player);
    }
}
