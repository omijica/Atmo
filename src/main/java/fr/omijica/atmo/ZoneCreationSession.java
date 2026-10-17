package fr.omijica.atmo;

import org.bukkit.Location;

/**
 * Sélection temporaire d'une zone : son nom et les deux coins choisis par un joueur.
 * La vérification du monde et l'enregistrement sont assurés par ZoneListener.
 */
public class ZoneCreationSession {
    private final String zoneName;
    private Location pos1;
    private Location pos2;

    public ZoneCreationSession(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getZoneName() { return zoneName; }
    public Location getPos1() {
        return pos1;
    }
    public Location getPos2() {
        return pos2;
    }
    public void setPos1(Location pos1) { this.pos1 = pos1; }
    public void setPos2(Location pos2) { this.pos2 = pos2; }

    /** Indique uniquement si les deux coins ont été sélectionnés. */
    public boolean isComplete() {
        return pos1 != null && pos2 != null;
    }
}
