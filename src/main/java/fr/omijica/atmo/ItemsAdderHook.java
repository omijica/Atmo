package fr.omijica.atmo;
import dev.lone.itemsadder.api.CustomFurniture;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

/** Regroupe les appels à l'API ItemsAdder, effectués lorsque cette intégration est active. */
public class ItemsAdderHook {
    /** Renvoie l'identifiant « namespace:id » du meuble reconnu, sinon null. */
    public static String getFurnitureId(Entity entity) {
        // Limite les appels à l'API aux types d'entités susceptibles de représenter un meuble.
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
            // Une entité non reconnue ou une erreur de l'API ne doit pas bloquer la boucle sonore.
            return null;
        }
    }
}
