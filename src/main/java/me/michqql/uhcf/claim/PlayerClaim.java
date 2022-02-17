package me.michqql.uhcf.claim;

import me.michqql.uhcf.claim.traversal.ClaimTraversal;
import me.michqql.uhcf.faction.PlayerFaction;
import org.bukkit.Chunk;

public class PlayerClaim extends Claim {

    private final PlayerFaction owner;

    private double upkeep;
    private long decayTimestamp;

    public PlayerClaim(PlayerFaction owner) {
        this.owner = owner;
    }

    public PlayerFaction getOwningFaction() {
        return owner;
    }

    public double getUpkeep() {
        return upkeep;
    }

    public void addUpkeep(double amount) {
        upkeep += amount;

        if(upkeep > getMaximumUpkeep())
            upkeep = getMaximumUpkeep();
    }

    public double getMaximumUpkeep() {
        // Claimed chunks x Faction size x Building blocks used
        return getNumberOfChunks() * owner.getMembers().getSize();
    }

    public boolean isDecaying() {
        return upkeep <= 0;
        //return upkeep <= 0 && System.currentTimeMillis() - decayTimestamp < TimeUnit.MINUTES.toMillis(30);
    }

    public long getDecayTimestamp() {
        return decayTimestamp;
    }

    /**
     * Checks whether unclaiming this chunk will split
     * the claim into two disconnected areas
     *
     * @param chunk to unclaim
     * @return {@code false} if this action will split claim into two
     */
    public boolean canUnclaim(Chunk chunk) {
        if(chunks.size() <= 2)
            return true;

        ClaimTraversal traversal = new ClaimTraversal(this);
        return traversal.canUnclaim(chunk);
    }

    public void claim(Chunk chunk) {
        chunks.add(chunk);
    }

    public void unclaim(Chunk chunk) {
        chunks.remove(chunk);
    }
}
