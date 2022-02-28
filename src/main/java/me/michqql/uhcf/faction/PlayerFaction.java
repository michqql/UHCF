package me.michqql.uhcf.faction;

import me.michqql.core.data.IData;
import me.michqql.uhcf.claim.PlayerClaim;
import me.michqql.uhcf.faction.attributes.Members;
import me.michqql.uhcf.faction.attributes.Relations;
import me.michqql.uhcf.faction.attributes.Warpoints;
import org.bukkit.Bukkit;

import java.util.UUID;

public class PlayerFaction extends Faction {

    private final Members members = new Members();
    private final Relations relations = new Relations();
    private final Warpoints warpoints = new Warpoints();
    private final PlayerClaim playerClaim = new PlayerClaim(this);

    private long createdAt;

    public PlayerFaction(String uniqueIdentifier) {
        super(uniqueIdentifier);
        this.createdAt = System.currentTimeMillis();
    }

    public PlayerFaction(String uniqueIdentifier, UUID creator) {
        super(uniqueIdentifier, creator);
        this.createdAt = System.currentTimeMillis();
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

        this.createdAt = data.getLong("created-at");

        members.read(data.getSection("members"));
        relations.read(data.getSection("relations"));
        warpoints.read(data.getSection("warpoints"));
        playerClaim.read(data.getSection("claim"));
    }

    @Override
    public void write(IData data) {
        data.set("creator", getCreator().toString());
        data.set("display-name", getDisplayName());
        data.set("created-at", createdAt);

        members.write(data.createSection("members"));
        relations.write(data.createSection("relations"));
        warpoints.write(data.createSection("warpoints"));
        playerClaim.write(data.createSection("claim"));
    }

    public long getCreatedAtTimestamp() {
        return createdAt;
    }

    public Members getMembers() {
        return members;
    }

    public Relations getRelations() {
        return relations;
    }

    public Warpoints getWarpoints() {
        return warpoints;
    }

    public PlayerClaim getClaim() {
        return playerClaim;
    }
}
