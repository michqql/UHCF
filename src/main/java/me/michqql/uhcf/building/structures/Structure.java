package me.michqql.uhcf.building.structures;

public abstract class Structure {

    protected final String id;
    protected final double width, height, depth;
    protected final String displayName;

    protected final double integrity;

    Structure(String id, double width, double height, double depth,
              String displayName, double integrity) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.displayName = displayName;
        this.integrity = integrity;
    }
}
