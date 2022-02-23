package me.michqql.uhcf.listeners.events;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerChunkChangeEvent extends PlayerEvent {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final Chunk from, to;

    public PlayerChunkChangeEvent(Player who, Chunk from, Chunk to) {
        super(who);
        this.from = from;
        this.to = to;
    }

    public Chunk getFrom() {
        return from;
    }

    public Chunk getTo() {
        return to;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
