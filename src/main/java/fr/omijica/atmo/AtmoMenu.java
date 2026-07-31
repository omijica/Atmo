package fr.omijica.atmo;



import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
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
import org.bukkit.profile.PlayerTextures;

import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    public static void openPaperMenu(Player player) {
        Component title = Component.text("Atmo Menu", NamedTextColor.GOLD, TextDecoration.BOLD);
        Inventory gui = Bukkit.createInventory(null, 54, title);

        String base64Texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGU0MDNjYzdiYmFjNzM2NzBiZDU0M2Y2YjA5NTViYWU3YjhlOTEyM2Q4M2JkNzYwZjYyMDRjNWFmZDhiZTdlMSJ9fX0=";
        ItemStack rightItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta rightMeta = (SkullMeta) rightItem.getItemMeta();

        if (rightMeta != null) {
            try {
                String decodedJson = new String(Base64.getDecoder().decode(base64Texture));
                String url = decodedJson.split("\"url\":\"")[1].split("\"")[0];
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                PlayerTextures textures = profile.getTextures();
                textures.setSkin(URI.create(url).toURL());
                profile.setTextures(textures);
                rightMeta.setOwnerProfile(profile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            rightMeta.displayName(Component.text("Right", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
            rightMeta.lore(List.of(Component.text("Click here", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)));

            rightItem.setItemMeta(rightMeta);

        }

        gui.setItem(50, rightItem); //45

        base64Texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTMzYWQ1YzIyZGIxNjQzNWRhYWQ2MTU5MGFiYTUxZDkzNzkxNDJkZDU1NmQ2YzQyMmE3MTEwY2EzYWJlYTUwIn19fQ==";
        ItemStack leftItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta leftMeta = (SkullMeta) leftItem.getItemMeta();

        if (leftMeta != null) {
            try {
                String decodedJson = new String(Base64.getDecoder().decode(base64Texture));
                String url = decodedJson.split("\"url\":\"")[1].split("\"")[0];
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                PlayerTextures textures = profile.getTextures();
                textures.setSkin(URI.create(url).toURL());
                profile.setTextures(textures);
                leftMeta.setOwnerProfile(profile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            leftMeta.displayName(Component.text("Left", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
            leftMeta.lore(List.of(Component.text("Click here", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)));

            leftItem.setItemMeta(leftMeta);

        }

        gui.setItem(48, leftItem); //45

        ItemStack barrierItem = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrierItem.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.displayName(Component.text(" "));
            barrierItem.setItemMeta(barrierMeta);
        }
        gui.setItem(48, barrierItem);

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
                ZoneListener zListener = plugin.getZoneListener();
                zListener.enableCreationMode(player, zoneName);
                return List.of(AnvilGUI.ResponseAction.close());
        })
        .title("Enter area's name.")
        .text("Area Name")
        .itemLeft(new ItemStack(Material.NAME_TAG))
        .plugin(plugin)
        .open(player);
    }
}
