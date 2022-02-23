package me.michqql.uhcf.listeners.events;

import me.michqql.uhcf.worldevents.WorldEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WorldEventEndEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final WorldEvent event;

    public WorldEventEndEvent(WorldEvent event) {
        super();
        this.event = event;
    }

    public WorldEvent getEvent() {
        return event;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
