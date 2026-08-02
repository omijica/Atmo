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
import java.util.*;

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

    /* MENU LIST AREAS */

    private final ZoneChecker zoneChecker;
    private static final int PAGE_SLOTS = 45; // slots 0 à 44
    private static final int PREVIOUS = 52; // "Left"
    private static final int NEXT   = 53; // "Right"
    private final Map<UUID, Integer> playerPage = new HashMap<>();

    AtmoMenu(ZoneChecker zoneChecker) {
        this.zoneChecker = zoneChecker;
    }

    public Map<UUID, Integer> getPlayerPage() {
        return playerPage;
    }

    public void openPaperMenu(Player player, int page) {
        Component title = Component.text("Atmo Menu (AREAS)", NamedTextColor.GOLD, TextDecoration.BOLD);
        Inventory gui = Bukkit.createInventory(null, 54, title);

        /* RIGHT ITEM */
        String base64Texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGU0MDNjYzdiYmFjNzM2NzBiZDU0M2Y2YjA5NTViYWU3YjhlOTEyM2Q4M2JkNzYwZjYyMDRjNWFmZDhiZTdlMSJ9fX0=";
        ItemStack rightItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta rightMeta = (SkullMeta) rightItem.getItemMeta();

        ItemStack glassFiller = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassFiller.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(Component.empty());
            glassFiller.setItemMeta(glassMeta);
        }

        for (int i=45; i < gui.getSize(); i++) {
            gui.setItem(i,glassFiller);
        }

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

        /* LEFT ITEM */
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

        /* BARRIER ITEM */

        ItemStack barrierItem = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrierItem.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.displayName(Component.text(""));
            barrierItem.setItemMeta(barrierMeta);
        }

        /* CODE MENU */
        List<ZoneClass> zones = zoneChecker.getZones();
        int totalPages = Math.max(1, (int) Math.ceil((double) zones.size() / PAGE_SLOTS));
        page = Math.max(0, Math.min(page, totalPages - 1));

        int debut = page * PAGE_SLOTS;
        int fin = Math.min(debut + PAGE_SLOTS, zones.size());

        int slot = 0;
        for (int i = debut; i < fin; i++) {
            gui.setItem(slot, createZoneItem(zones.get(i)));
            slot++;
        }

        if (page > 0) {
            gui.setItem(PREVIOUS, leftItem);
        } else {
            gui.setItem(PREVIOUS, barrierItem);
        }

        if (page < totalPages - 1) {
            gui.setItem(NEXT, rightItem);
        } else {
            gui.setItem(NEXT, barrierItem);
        }

        player.openInventory(gui);

        playerPage.put(player.getUniqueId(), page);
    }

    private ItemStack createZoneItem(ZoneClass zone) {
        ItemStack item = new ItemStack(Material.MAP);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(zone.getName(), NamedTextColor.GOLD, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("World: " + zone.getWorld(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Pos1: " + fmt(zone.getX1(), zone.getY1(), zone.getZ1()), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Pos2: " + fmt(zone.getX2(), zone.getY2(), zone.getZ2()), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Priority: " + zone.getPriority(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private String fmt(double x, double y, double z) {
        return String.format("%.1f, %.1f, %.1f", x, y, z);
    }

    public void openZoneEditMenu(Player player, String name) {
        Component title = Component.text("Atmo - " + name + " Settings", NamedTextColor.GOLD, TextDecoration.BOLD);
        Inventory gui = Bukkit.createInventory(null, 27, title);

        ItemStack glassFiller = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassFiller.getItemMeta();
        if (glassMeta != null) {
            glassMeta.displayName(Component.empty());
            glassFiller.setItemMeta(glassMeta);
        }

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, glassFiller);
        }

        // Bouton 1: Teleport to area (Ender Pearl - Slot 11)
        ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta teleportMeta = teleportItem.getItemMeta();
        if (teleportMeta != null) {
            teleportMeta.displayName(Component.text("Teleport to area", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
            teleportMeta.lore(List.of(Component.text("Click to teleport", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)));
            teleportItem.setItemMeta(teleportMeta);
        }
        gui.setItem(11, teleportItem);

        // Bouton 2: Redefine area (Compass - Slot 13)
        ItemStack redefineItem = new ItemStack(Material.COMPASS);
        ItemMeta redefineMeta = redefineItem.getItemMeta();
        if (redefineMeta != null) {
            redefineMeta.displayName(Component.text("Redefine area", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
            redefineMeta.lore(List.of(Component.text("Click to redefine boundaries", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)));
            redefineItem.setItemMeta(redefineMeta);
        }
        gui.setItem(13, redefineItem);

        // Bouton 3: Delete area (TNT - Slot 15)
        ItemStack deleteItem = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = deleteItem.getItemMeta();
        if (deleteMeta != null) {
            deleteMeta.displayName(Component.text("Delete area", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
            deleteMeta.lore(List.of(Component.text("Click to delete", NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false)));
            deleteItem.setItemMeta(deleteMeta);
        }
        gui.setItem(15, deleteItem);

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(Component.text("Back", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
            backMeta.lore(List.of(Component.text("Click to return", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
            backItem.setItemMeta(backMeta);
        }
        gui.setItem(18, backItem);

        player.openInventory(gui);
    }
}
