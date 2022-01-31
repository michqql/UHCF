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
    private final List<World> claimableWorlds;

    private final HashMap<Chunk, Claim> chunkToClaimMap = new HashMap<>();

    public ClaimsManager(CommentFile factionsConfigFile) {
        this.factionsConfigFile = factionsConfigFile;
        this.claimableWorlds = new ArrayList<>();
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration f = factionsConfigFile.getConfig();

        List<String> claimableWorldNames = f.getStringList("claimable-worlds");
        if(claimableWorldNames.isEmpty()) {
            Bukkit.getLogger().warning("[factions_config.yml] No claimable worlds in 'claimable-worlds'");
        }

        for(String worldName : claimableWorldNames) {
            World world = Bukkit.getWorld(worldName);
            if(world == null) {
                Bukkit.getLogger().warning("[factions_config.yml] Invalid world in 'claimable-worlds': " + worldName);
            } else {
                claimableWorlds.add(world);
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

    /**
     * Checks a 5x5 area around the center chunk to see if
     * the location is valid to claim
     *
     * @param playerFaction the claiming faction
     * @param center the center chunk
     * @return the claim that blocks this action from taking place,
     *         or null if it claimed successfully
     *         (Can also return null if the world is invalid)
     */
    public Claim claimPlayer3x3(PlayerFaction playerFaction, Chunk center) {
        // Check world is claimable
        final PlayerClaim playerClaim = playerFaction.getClaim();
        final World world = center.getWorld();
        if(!isWorldClaimable(world))
            return null;

        // Check 5x5 area of chunks to check for any adjacent claims
        Set<Claim> claims = getClaimsAroundCenter(center, 5);
        for(Claim claim : claims) {
            // The area contains an admin claim, cannot claim
            if(claim instanceof AdminClaim)
                return claim;

            // The area contains another factions claim, cannot claim
            if(claim instanceof PlayerClaim otherClaim && !otherClaim.getOwningFaction().equals(playerFaction))
                return claim;
        }

        // There were no other claims blocking the area
        // Now we get a 3x3 area of chunks and add to claim
        for(int x = (center.getX() - 1); x <= (center.getX() + 1); x++) {
            for(int z = (center.getZ() - 1); z <= (center.getZ() + 1); z++) {
                Chunk chunk = world.getChunkAt(x, z);
                playerClaim.getClaimedChunks().add(chunk);

                chunkToClaimMap.put(chunk, playerClaim);
            }
        }
        return null;
    }

    public boolean isWorldClaimable(World world) {
        return claimableWorlds.contains(world);
    }

    public boolean isChunkClaimed(Chunk chunk) {
        return chunkToClaimMap.containsKey(chunk);
    }

    public Claim getClaimByChunk(Chunk chunk) {
        return chunkToClaimMap.get(chunk);
    }

    public Set<Claim> getClaimsAroundCenter(Chunk center, int squareRadius) {
        Set<Claim> claims = new HashSet<>();

        World world = center.getWorld();
        int halfRadius = squareRadius / 2;
        int x = center.getX() - halfRadius;
        int z = center.getZ() - halfRadius;

        for(int i = x; i < x + squareRadius; i++) {
            for(int j = z; j < z + squareRadius; j++) {
                claims.add(getClaimByChunk(world.getChunkAt(i, j)));
            }
        }

        return claims;
    }
}
