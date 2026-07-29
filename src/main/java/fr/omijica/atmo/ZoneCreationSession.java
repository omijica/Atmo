package fr.omijica.atmo;

import org.bukkit.Location;

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

    public boolean isComplete() {
        return pos1 != null && pos2 != null;
    }
}
