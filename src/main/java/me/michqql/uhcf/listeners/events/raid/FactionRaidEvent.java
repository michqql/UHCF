package me.michqql.uhcf.listeners.events.raid;

import me.michqql.uhcf.raiding.Raid;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FactionRaidEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public enum Type {
        START, END
    }

    private final Raid raid;
    private final Type type;
    private final boolean force;

    public FactionRaidEvent(Raid raid, Type type) {
        this.raid = raid;
        this.type = type;
        this.force = false;
    }

    public FactionRaidEvent(Raid raid, Type type, boolean force) {
        this.raid = raid;
        this.type = type;
        this.force = force;
    }

    public Raid getRaid() {
        return raid;
    }

    public Type getEventType() {
        return type;
    }

    /**
     * E.g. if the event was ended by an admin
     * @return is forceful event call
     */
    public boolean isForceful() {
        return force;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
