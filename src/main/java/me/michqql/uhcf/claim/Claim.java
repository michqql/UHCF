package me.michqql.uhcf.claim;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.*;

public abstract class Claim {

    protected final Set<Chunk> chunks = new HashSet<>();

    void claim(Chunk chunk) {
        chunks.add(chunk);
    }

    void unclaim(Chunk chunk) {
        chunks.remove(chunk);
    }

    protected Set<Chunk> getClaimedChunks() {
        return chunks;
    }

    public List<Chunk> getClaimedChunksCopy() {
        return new ArrayList<>(chunks);
    }

    public boolean isClaimed(Chunk chunk) {
        return chunks.contains(chunk);
    }

    public int getNumberOfChunks() {
        return chunks.size();
    }

    /**
     * O(n) time complexity
     *
     * @param chunk the chunk to check
     * @return {@code true} if there is a path from the chunk to the claim
     */
    public boolean isAdjacent(Chunk chunk) {
        if(chunks.isEmpty())
            return true;

        // Basic check - check whether chunk is adjacent to a claimed chunk
        for(Chunk claimed : chunks) {
            if(isAdjacent(claimed, chunk))
                return true;
        }
        return false;
    }

    public Set<Chunk> getAdjacent(Chunk center) {
        Set<Chunk> result = new HashSet<>();

        World world = center.getWorld();
        int x = center.getX();
        int z = center.getZ();

        Chunk north = world.getChunkAt(x, z + 1);
        if(chunks.contains(north))
            result.add(north);

        Chunk south = world.getChunkAt(x, z - 1);
        if(chunks.contains(south))
            result.add(south);

        Chunk east = world.getChunkAt(x + 1, z);
        if(chunks.contains(east))
            result.add(east);

        Chunk west = world.getChunkAt(x - 1, z);
        if(chunks.contains(west))
            result.add(west);

        return result;
    }

    private boolean isAdjacent(Chunk c1, Chunk c2) {
        if(!c1.getWorld().equals(c2.getWorld()))
            return false;

        int x1 = c1.getX();
        int z1 = c1.getZ();

        int x2 = c2.getX();
        int z2 = c2.getZ();

        int dist = Math.abs(x1 - x2 + z1 - z2);
        return dist == 1;
    }
}
