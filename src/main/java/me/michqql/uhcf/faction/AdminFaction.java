package me.michqql.uhcf.faction;

import me.michqql.uhcf.claim.AdminClaim;

public class AdminFaction extends Faction {

    private final AdminClaim claim;

    public AdminFaction(String uniqueIdentifier) {
        super(uniqueIdentifier, null);
        this.claim = new AdminClaim(this);
    }

    public AdminClaim getClaim() {
        return claim;
    }
}
