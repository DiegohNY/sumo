package dev.diegoh.sumo.arena;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.World;

public final class ArenaBounds {
    private final World world;
    private final double centerX;
    private final double centerZ;
    private final double radius;
    private final double radiusSquared;

    private ArenaBounds(World world, double centerX, double centerZ, double radius) {
        this.world = world;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = radius;
        this.radiusSquared = radius * radius;
    }

    public static ArenaBounds cylinder(Location center, double radius) {
        Objects.requireNonNull(center.getWorld(), "bounds center must have a world");
        return new ArenaBounds(center.getWorld(), center.getX(), center.getZ(), radius);
    }

    public boolean contains(Location location) {
        if (location == null || !world.equals(location.getWorld())) return false;
        double dx = location.getX() - centerX;
        double dz = location.getZ() - centerZ;
        return dx * dx + dz * dz <= radiusSquared;
    }

    public World world() {
        return world;
    }

    public double centerX() {
        return centerX;
    }

    public double centerZ() {
        return centerZ;
    }

    public double radius() {
        return radius;
    }
}
