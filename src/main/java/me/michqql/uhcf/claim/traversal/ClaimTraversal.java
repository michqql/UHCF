package me.michqql.uhcf.claim.traversal;

import me.michqql.uhcf.claim.Claim;
import org.bukkit.Chunk;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClaimTraversal {

    private final Claim claim;

    public ClaimTraversal(Claim claim) {
        this.claim = claim;
    }

    /**
     * O(n^3) time complexity!
     *
     * @param chunk the chunk to unclaim
     * @return {@code true} if the chunk can be unclaimed
     */
    public boolean canUnclaim(Chunk chunk) {
        List<Chunk> copy = claim.getClaimedChunksCopy();
        copy.remove(chunk);

        Chunk destination = copy.remove(0);
        for(Chunk remaining : copy) {
            Set<Chunk> visited = new HashSet<>();
            visited.add(chunk);

            if(!hasPath(remaining, destination, visited))
                return false;
        }
        return true;
    }

    public boolean hasPath(Chunk start, Chunk destination) {
        return hasPath(start, destination, new HashSet<>());
    }

    private boolean hasPath(Chunk current, Chunk destination, Set<Chunk> visited) {
        if(current.equals(destination))
            return true;

        visited.add(current);

        Set<Chunk> adjacentChunks = claim.getAdjacent(current);
        adjacentChunks.removeAll(visited);

        for(Chunk c : adjacentChunks) {
            if(hasPath(c, destination, visited))
                return true;
        }
        return false;
    }
}
