package me.michqql.uhcf.claim;

import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.Faction;

public class AdminClaim extends Claim {

    private final AdminFaction owner;

    public AdminClaim(AdminFaction owner) {
        this.owner = owner;
    }

    public AdminFaction getAdminFactionOwner() {
        return owner;
    }

    @Override
    public Faction getOwningFaction() {
        return owner;
    }
}
