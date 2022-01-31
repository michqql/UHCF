package me.michqql.uhcf.claim;

import org.bukkit.Chunk;

import java.util.HashSet;
import java.util.Set;

public abstract class Claim {

    protected final Set<Chunk> chunks = new HashSet<>();

    public Set<Chunk> getClaimedChunks() {
        return chunks;
    }

    public int getNumberOfChunks() {
        return chunks.size();
    }
}
