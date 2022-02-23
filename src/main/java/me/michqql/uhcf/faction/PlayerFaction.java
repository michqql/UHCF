package me.michqql.uhcf.faction;

import me.michqql.core.data.IData;
import me.michqql.uhcf.claim.PlayerClaim;
import me.michqql.uhcf.faction.attributes.Members;
import me.michqql.uhcf.faction.attributes.Relations;
import org.bukkit.Bukkit;

import java.util.UUID;

public class PlayerFaction extends Faction {

    private final Members members = new Members();
    private final Relations relations = new Relations();
    private final PlayerClaim playerClaim = new PlayerClaim(this);

    public PlayerFaction(String uniqueIdentifier) {
        super(uniqueIdentifier);
    }

    public PlayerFaction(String uniqueIdentifier, UUID creator) {
        super(uniqueIdentifier, creator);
    }

    @Override
    public void read(IData data) {
        String creatorString = data.getString("creator");
        try {
            setCreator(UUID.fromString(creatorString));
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("[UHCF] Error while loading player faction: malformed creator UUID");
        }

        setDisplayName(data.getString("display-name"));

        members.read(data.getSection("members"));
        relations.read(data.getSection("relations"));
        playerClaim.read(data.getSection("claim"));
    }

    @Override
    public void write(IData data) {
        data.set("creator", getCreator().toString());
        data.set("display-name", getDisplayName());

        members.write(data.createSection("members"));
        relations.write(data.createSection("relations"));
        playerClaim.write(data.createSection("claim"));
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
