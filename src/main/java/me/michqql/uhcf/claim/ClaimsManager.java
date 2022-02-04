package me.michqql.uhcf.claim;

import me.michqql.core.io.CommentFile;
import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.PlayerFaction;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ClaimsManager {

    // Config
    private final CommentFile factionsConfigFile;
    private World claimableWorld;

    private final HashMap<Chunk, Claim> chunkToClaimMap = new HashMap<>();

    public ClaimsManager(CommentFile factionsConfigFile) {
        this.factionsConfigFile = factionsConfigFile;
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration f = factionsConfigFile.getConfig();

        String claimableWorldName = f.getString("claimable-world");
        if(claimableWorldName == null || claimableWorldName.isEmpty()) {
            Bukkit.getLogger().warning("[factions_config.yml] No claimable world specified 'claimable-world'");
        } else {
            World world = Bukkit.getWorld(claimableWorldName);
            if (world == null) {
                Bukkit.getLogger().warning("[factions_config.yml] Invalid world in 'claimable-world': " + claimableWorldName);
            } else {
                this.claimableWorld = world;
            }
        }
    }

    // Admin claims bypass world restrictions
    public Claim claimAdminChunk(AdminFaction adminFaction, Chunk chunk) {
        Claim claim = getClaimByChunk(chunk);
        if(claim != null)
            return claim;

        AdminClaim adminClaim = adminFaction.getClaim();
        adminClaim.claim(chunk);

        chunkToClaimMap.put(chunk, adminClaim);
        return null;
    }

    public Claim claimPlayerChunk(PlayerFaction playerFaction, Chunk chunk) {
        // Check world is claimable
        final PlayerClaim playerClaim = playerFaction.getClaim();
        final World world = chunk.getWorld();
        if(!isWorldClaimable(world))
            return null;

        Claim claim = getClaimByChunk(chunk);
        if(claim != null)
            return claim;

        // Check there are no other claims nearby
        Set<Claim> nearby = getClaimsAroundCenter(chunk, 3);
        for(Claim near : nearby) {
            // The area contains an admin claim, cannot claim
            if(near instanceof AdminClaim)
                return near;

            // The area contains another factions claim, cannot claim
            if(near instanceof PlayerClaim otherClaim && !otherClaim.getOwningFaction().equals(playerFaction))
                return near;
        }

        // There were no other claims blocking the area
        // Check there is a connection between chunk and rest of claim
        if(!playerClaim.isAdjacent(chunk))
            return null;

        playerClaim.getClaimedChunks().add(chunk);
        chunkToClaimMap.put(chunk, playerClaim);
        return null;
    }

    public void unclaim(Chunk chunk) {
        Claim claim = chunkToClaimMap.remove(chunk);
        if(claim != null)
            claim.unclaim(chunk);
    }

    public boolean isWorldClaimable(World world) {
        return claimableWorld != null && claimableWorld.equals(world);
    }

    public boolean isChunkClaimed(Chunk chunk) {
        return chunkToClaimMap.containsKey(chunk);
    }

    public Claim getClaimByChunk(Chunk chunk) {
        return chunkToClaimMap.get(chunk);
    }

    public Set<Claim> getClaimsAroundCenter(Chunk center, int squareLength) {
        Set<Claim> claims = new HashSet<>();

        World world = center.getWorld();
        int radius = squareLength / 2;
        int x = center.getX() - radius;
        int z = center.getZ() - radius;

        for(int i = x; i < x + squareLength; i++) {
            for(int j = z; j < z + squareLength; j++) {
                Claim chunk = getClaimByChunk(world.getChunkAt(i, j));
                if(chunk != null)
                    claims.add(chunk);
            }
        }

        return claims;
    }
}
