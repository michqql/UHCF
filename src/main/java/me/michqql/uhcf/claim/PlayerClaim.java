package me.michqql.uhcf.claim;

import me.michqql.uhcf.faction.PlayerFaction;

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
}
