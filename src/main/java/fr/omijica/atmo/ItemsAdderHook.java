package fr.omijica.atmo;
import dev.lone.itemsadder.api.CustomFurniture;
import org.bukkit.entity.Entity;

public class ItemsAdderHook {
    public static String getFurnitureId(Entity entity) {
        CustomFurniture furniture = CustomFurniture.byAlreadySpawned(entity);
        if (furniture == null) return null;
        return furniture.getNamespacedID().toString();
    }
}
