package me.michqql.uhcf.faction;

import me.michqql.uhcf.claim.PlayerClaim;
import me.michqql.uhcf.faction.attributes.Members;
import me.michqql.uhcf.faction.attributes.Relations;

import java.util.UUID;

public class PlayerFaction extends Faction {

    private final Members members = new Members();
    private final Relations relations = new Relations();
    private final PlayerClaim playerClaim = new PlayerClaim(this);

    public PlayerFaction(String uniqueIdentifier, UUID creator) {
        super(uniqueIdentifier, creator);
    }

    public Members getMembers() {
        return members;
    }

    public Relations getRelations() {
        return relations;
    }

    public PlayerClaim getClaim() {
        return playerClaim;
    }
}
