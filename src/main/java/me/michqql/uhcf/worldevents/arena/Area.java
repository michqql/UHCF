package me.michqql.uhcf.worldevents.arena;

import me.michqql.core.data.IData;
import me.michqql.core.data.IReadWrite;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

public class Area implements IReadWrite {

    private final static Vector ZERO = new Vector(0, 0, 0);

    private Vector min, max;

    public Area() {
        this.min = ZERO.clone();
        this.max = ZERO.clone();
    }

    public Area(Vector posA, Vector posB) {
        setPositions(posA, posB);
    }

    @Override
    public void read(IData data) {
        int xMin = data.getInteger("x-min");
        int yMin = data.getInteger("y-min");
        int zMin = data.getInteger("z-min");
        int xMax = data.getInteger("x-max");
        int yMax = data.getInteger("y-max");
        int zMax = data.getInteger("z-max");

        this.min = new Vector(xMin, yMin, zMin);
        this.max = new Vector(xMax, yMax, zMax);
    }

    @Override
    public void write(IData data) {
        data.set("x-min", min.getBlockX());
        data.set("y-min", min.getBlockY());
        data.set("z-min", min.getBlockZ());
        data.set("x-max", max.getBlockX());
        data.set("y-max", max.getBlockY());
        data.set("z-max", max.getBlockZ());
    }

    public void setPositions(Vector a, Vector b) {
        int aX = a.getBlockX();
        int aY = a.getBlockY();
        int aZ = a.getBlockZ();

        int bX = b.getBlockX();
        int bY = b.getBlockY();
        int bZ = b.getBlockZ();

        this.min = new Vector(Math.min(aX, bX), Math.min(aY, bY), Math.min(aZ, bZ));
        this.max = new Vector(Math.max(aX, bX), Math.max(aY, bY), Math.max(aZ, bZ));
    }

    public boolean isInside(Vector vec) {
        return isInside(vec.getX(), vec.getY(), vec.getZ());
    }

    public boolean isInside(Location location) {
        return isInside(location.getX(), location.getY(), location.getZ());
    }

    public boolean isInside(double x, double y, double z) {
        return x >= min.getX() && x <= max.getX() &&
                y >= min.getY() && y <= max.getY() &&
                z >= min.getZ() && z <= max.getZ();
    }

    public List<Player> getPlayersInside(World world) {
        return world.getPlayers().stream()
                .filter(player -> isInside(player.getLocation()))
                .collect(Collectors.toList());
    }

    public Vector getMidpoint() {
        return min.clone().midpoint(max);
    }

    public int getArea() {
        int x = max.getBlockX() - min.getBlockX();
        int y = max.getBlockY() - min.getBlockY();
        int z = max.getBlockZ() - min.getBlockZ();

        return x * y * z;
    }

    public Area copy() {
        return new Area(min, max);
    }

    public Vector getMin() {
        return min;
    }

    public void setMin(Vector min) {
        this.min = min;
    }

    public Vector getMax() {
        return max;
    }

    public void setMax(Vector max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "Area{" +
                "min=(" + min +
                "), max=(" + max +
                ")}";
    }
}
