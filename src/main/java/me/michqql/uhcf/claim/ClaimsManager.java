package me.michqql.uhcf.claim;

import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.PlayerFaction;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ClaimsManager {

    private final HashMap<Claim.XZIdentifier, Claim> coordinateToClaimMap = new HashMap<>();

    public Claim claimAdminChunk(AdminFaction adminFaction, Chunk chunk) {
        Claim claim = getClaimFromChunk(chunk);
        if(claim != null)
            return claim;

        AdminClaim adminClaim = adminFaction.getClaim();
        adminClaim.claim(chunk);

        coordinateToClaimMap.put(new Claim.XZIdentifier(chunk), adminClaim);
        return null;
    }

    public Claim claimPlayer3x3(PlayerFaction playerFaction, Chunk center) {
        HashSet<Chunk> toClaim = new HashSet<>();

        // Check 3x3 area of chunks, and check their 3x3
        // to check for any adjacent claims
        World world = center.getWorld();
        for(int x = (center.getX() - 1); x <= (center.getX() + 1); x++) {
            for(int z = (center.getZ() - 1); z <= (center.getZ() + 1); z++) {
                Chunk chunk = world.getChunkAt(x, z);

                Set<Claim> claims = get3x3ClaimsAroundCoordinate(chunk.getX(), chunk.getZ());

                // Check a 3x3 area to see if it already contains any claims
                for(Claim claim : claims) {
                    // The space contains an admin claim, and so cannot be claimed
                    if(claim instanceof AdminClaim)
                        return claim;

                    // The space contains another factions claim, and so cannot be claimed
                    if(claim instanceof PlayerClaim playerClaim)
                        if(!playerClaim.getOwningFaction().equals(playerFaction))
                            return claim;

                    // There was no claim, or it was owned by this player faction
                }

                // If it has made it to this point, there were no claims
                // in a 3x3 area around this chunk, and so we can add
                // this chunk to be claimed

                // We must check whether this chunk is already claimed by
                // this faction, if so, we do not want to claim it again
                Claim claim = getClaimFromChunk(chunk);
                if(claim == null)
                    toClaim.add(chunk);
            }
        }

        PlayerClaim playerClaim = new PlayerClaim(playerFaction, toClaim);
        for(Chunk chunk : playerClaim.chunks)
            coordinateToClaimMap.put(new Claim.XZIdentifier(chunk), playerClaim);

        playerFaction.getClaims().add(playerClaim);
        return null;
    }

    public boolean isChunkClaimed(Chunk chunk) {
        return coordinateToClaimMap.containsKey(new Claim.XZIdentifier(chunk));
    }

    public boolean isChunkClaimed(int x, int z) {
        return coordinateToClaimMap.containsKey(new Claim.XZIdentifier(x, z));
    }

    public Claim getClaimFromChunk(Chunk chunk) {
        return coordinateToClaimMap.get(new Claim.XZIdentifier(chunk));
    }

    public Claim getClaimFromCoordinates(int x, int z) {
        return coordinateToClaimMap.get(new Claim.XZIdentifier(x, z));
    }

    public Set<Claim> get3x3ClaimsAroundCoordinate(int x, int z) {
        Set<Claim> claims = new HashSet<>();

        for(int i = (x - 1); i <= (x + 1); i++) {
            for(int j = (z - 1); j <= (x + 1); j++) {
                claims.add(getClaimFromCoordinates(i, j));
            }
        }

        return claims;
    }
}
