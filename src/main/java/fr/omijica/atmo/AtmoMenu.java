package fr.omijica.atmo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        gui.setItem(10, tripwireItem);

        //PAPER
        ItemStack paperItem = new ItemStack(Material.PAPER);
        ItemMeta paperMeta = paperItem.getItemMeta();
        if (paperMeta != null) {
            paperMeta.displayName(Component.text("Edit an area.", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
            paperMeta.lore(List.of(Component.text("Click here", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)));

            paperItem.setItemMeta(paperMeta);
        }
        gui.setItem(13, paperItem);

        //DISC
        ItemStack discItem = new ItemStack(Material.MUSIC_DISC_STAL);
        ItemMeta discMeta = discItem.getItemMeta();
        if (discMeta != null) {
            discMeta.displayName(Component.text("List of music.", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
            discMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            discMeta.lore(List.of(Component.text("Click here.", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)));

            discItem.setItemMeta(discMeta);
        }
        gui.setItem(16, discItem);

        player.openInventory(gui);
    }
}
