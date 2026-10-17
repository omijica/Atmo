package fr.omijica.atmo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Maintient les zones en mémoire et détermine laquelle s'applique à la position d'un joueur. */
public class ZoneChecker {

    private final List<ZoneClass> zones = new ArrayList<>();

    /** Remplace les zones en mémoire par celles fournies par le lecteur de configuration. */
    public void setZones(List<ZoneClass> loadedZones) {
        List<ZoneClass> snapshot = List.copyOf(loadedZones);
        zones.clear();
        zones.addAll(snapshot);
    }

    public List<ZoneClass> getZones() { return Collections.unmodifiableList(zones); }

    /** Recherche un nom sans distinguer les majuscules ; renvoie null s'il est introuvable. */
    public ZoneClass getZoneByName(String name) {
        return zones.stream().filter(zone -> zone.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /** Retire une zone de la mémoire après sa suppression dans le fichier. */
    public void removeZone(String name) {
        zones.removeIf(zone -> zone.getName().equalsIgnoreCase(name));
    }

    /**
     * Retient la zone contenant le joueur avec la plus grande priorité numérique.
     * À priorité égale, la première zone chargée reste sélectionnée ; hors zone, renvoie null.
     */
    public ZoneClass getPlayerZone(org.bukkit.entity.Player player) {
        ZoneClass bestZone = null;

        for (ZoneClass zone : zones) {
            if (zone.contains(player.getLocation())) {
                if (bestZone == null || zone.getPriority() > bestZone.getPriority()) {
                    bestZone = zone;
                }
            }
        }
        return bestZone;
    }

}


