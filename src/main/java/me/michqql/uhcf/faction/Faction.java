package me.michqql.uhcf.faction;

import me.michqql.core.data.IReadWrite;

import java.util.UUID;

public abstract class Faction implements IReadWrite {

    private final String uniqueIdentifier;
    private UUID creator;

    private boolean active;
    private String displayName;

    Faction(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    Faction(String uniqueIdentifier, UUID creator) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.creator = creator;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public UUID getCreator() {
        return creator;
    }

    void setCreator(UUID creator) {
        this.creator = creator;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
