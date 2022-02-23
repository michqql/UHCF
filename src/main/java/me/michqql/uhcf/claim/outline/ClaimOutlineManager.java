package me.michqql.uhcf.claim.outline;

import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.claim.AdminClaim;
import me.michqql.uhcf.claim.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ClaimOutlineManager {

    private final static long HIGHLIGHT_TIME_TICKS = 20 * 60 * 5;
    private final static Material HIGHLIGHT_BLOCK_MATERIAL = Material.GLOWSTONE;

    private final UHCFPlugin plugin;
    private final HashMap<Chunk, Set<HighlightedBlockWrapper>> highlightedChunks = new HashMap<>();

    public ClaimOutlineManager(UHCFPlugin plugin) {
        this.plugin = plugin;
    }

    public void onDisable() {
        highlightedChunks.forEach((chunk, set) -> set.forEach(HighlightedBlockWrapper::revert));
    }

    public boolean onBlockBreak(Block block) {
        Chunk chunk = block.getChunk();

        Set<HighlightedBlockWrapper> set = highlightedChunks.get(chunk);
        if(set == null)
            return false;

        for(HighlightedBlockWrapper wrapper : set) {
            if(block.equals(wrapper.block)) {
                wrapper.revert();
                set.remove(wrapper);
                highlightedChunks.put(chunk, set);
                return true;
            }
        }
        return false;
    }

    public void onUnclaim(Claim claim, Chunk chunk) {
        if(claim instanceof AdminClaim)
            return;

        Set<HighlightedBlockWrapper> set = highlightedChunks.remove(chunk);
        if(set == null)
            return;

        for(HighlightedBlockWrapper wrapper : set) {
            wrapper.revert();
        }
    }

    public void onClaim(Claim claim, Chunk chunk) {
        if(claim instanceof AdminClaim)
            return;

        World world = chunk.getWorld();
        final int worldXCoordinate = chunk.getX() * 16;
        final int worldZCoordinate = chunk.getZ() * 16;

        Set<HighlightedBlockWrapper> affected = new HashSet<>();
        // Loop through X: 0 -> 15
        for(int x = 0; x < 16; x++) {
            Block block0 = getHighestSolidBlock(world, worldXCoordinate + x, worldZCoordinate);
            if(block0 != null)
                affected.add(new HighlightedBlockWrapper(block0));

            Block block1 = getHighestSolidBlock(world, worldXCoordinate + x, worldZCoordinate + 15);
            if(block1 != null)
                affected.add(new HighlightedBlockWrapper(block1));
        }

        // Loop through Z: 0 -> 15
        for(int z = 0; z < 16; z++) {
            Block block0 = getHighestSolidBlock(world, worldXCoordinate, worldZCoordinate + z);
            if(block0 != null)
                affected.add(new HighlightedBlockWrapper(block0));

            Block block1 = getHighestSolidBlock(world, worldXCoordinate + 15, worldZCoordinate + z);
            if(block1 != null)
                affected.add(new HighlightedBlockWrapper(block1));
        }

        // Add all affected blocks to highlightedChunks
        // Run task later to revert these changes
        highlightedChunks.put(chunk, affected);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Set<HighlightedBlockWrapper> set = highlightedChunks.remove(chunk);
            if(set != null) {
                set.forEach(HighlightedBlockWrapper::revert);
            }
        }, HIGHLIGHT_TIME_TICKS);
    }

    private Block getHighestSolidBlock(World world, int x, int z) {
        for(int y = world.getMaxHeight(); y >= 0; y--) {
            Block block = world.getBlockAt(x, y, z);
            if(block.getType().isOccluding())
                return block;
        }
        return null;
    }

    static class HighlightedBlockWrapper {
        private final Block block;
        private final Material previousMaterial;
        private final BlockData previousData;

        HighlightedBlockWrapper(Block block) {
            this.block = block;
            this.previousMaterial = block.getType();
            this.previousData = block.getBlockData().clone();

            block.setType(HIGHLIGHT_BLOCK_MATERIAL);
        }

        void revert() {
            block.setType(previousMaterial);
            block.setBlockData(previousData);
        }
    }
}
