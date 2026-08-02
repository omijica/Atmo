package fr.omijica.atmo;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class ZoneClass {

    private final String name;
    private final String world;
    private final double minX, minY, minZ;
    private final double maxX, maxY, maxZ;
    private final int priority;

    public ZoneClass(String name, String world, double x1, double y1, double z1, double x2, double y2, double z2, int priority) {
        this.name = name;
        this.world = world;
        this.minX = Math.min(x1, x2);
        this.maxX = Math.max(x1, x2);
        this.minY = Math.min(y1, y2);
        this.maxY = Math.max(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxZ = Math.max(z1, z2);
        this.priority = priority;
    }

    public boolean contains(org.bukkit.Location loc) {

        if (!loc.getWorld().getName().equals(world)) {
            return false;
        }
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        return x >= minX && x <= maxX +1 && y >= minY && y <= maxY +1 && z >= minZ && z <= maxZ +1;
    }

    public String getName() { return name; }
    public int getPriority() { return priority; }
    public String getWorld() { return world; }
    public double getX1() { return minX; }
    public double getY1() { return minY; }
    public double getZ1() { return minZ; }
    public double getX2() { return maxX; }
    public double getY2() { return maxY; }
    public double getZ2() { return maxZ; }

    public Location getCenterLocation() {
        World world = Bukkit.getWorld(this.world);
        if (world == null) return null;

        double centerX = (this.minX + this.maxX ) / 2.0;
        double centerY = (this.minY + this.maxY) / 2.0;
        double centerZ = (this.minZ + this.maxZ) / 2.0;

        return new Location(world, centerX + 0.5, centerY, centerZ + 0.5);
    }
}



