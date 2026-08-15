package fr.omijica.atmo;
import dev.lone.itemsadder.api.CustomFurniture;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class ItemsAdderHook {
    public static String getFurnitureId(Entity entity) {
        EntityType type = entity.getType();
        if (type != EntityType.ITEM_DISPLAY
                && type != EntityType.INTERACTION
                && type != EntityType.ARMOR_STAND) {
            return null;
        }

        try {
            CustomFurniture furniture = CustomFurniture.byAlreadySpawned(entity);
            if (furniture == null) {
                return null;
            }
            String id = furniture.getNamespacedID().toString();
            return id;
        } catch (RuntimeException e) {
            return null;
        }
    }
}