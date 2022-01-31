package me.michqql.uhcf.faction;

import java.util.UUID;

public abstract class Faction {

    private final String uniqueIdentifier;
    private final UUID creator;

    private boolean active;
    private String displayName;

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
