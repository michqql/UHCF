package me.michqql.uhcf.claim;

import me.michqql.uhcf.faction.AdminFaction;

public class AdminClaim extends Claim {

    private final AdminFaction owner;

    public AdminClaim(AdminFaction owner) {
        this.owner = owner;
    }

    public AdminFaction getAdminFactionOwner() {
        return owner;
    }
}
