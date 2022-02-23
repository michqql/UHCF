package me.michqql.uhcf.building.build;

import me.michqql.uhcf.building.structures.Structure;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class Building {

    List<Node> children = new ArrayList<>();
    Structure structure;

    Location location; // The location in the world at which the structure's relative location is at 0,0,0

    class Node {
        Node parent;
        List<Node> children;

        Structure structure;
        BlockFace blockFace;
    }
}
