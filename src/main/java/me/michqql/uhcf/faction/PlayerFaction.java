package me.michqql.uhcf.faction;

import me.michqql.uhcf.claim.PlayerClaim;
import me.michqql.uhcf.faction.attributes.Members;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerFaction extends Faction {

    private final Members members = new Members();

    // Claims
    private final List<PlayerClaim> claims = new ArrayList<>();

    public PlayerFaction(String uniqueIdentifier, UUID creator) {
        super(uniqueIdentifier, creator);
    }

    public Members getMembers() {
        return members;
    }

    public List<PlayerClaim> getClaims() {
        return claims;
    }
}
