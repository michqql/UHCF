package me.michqql.uhcf.listeners.events;

import me.michqql.uhcf.faction.Faction;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class FactionInfoUpdateEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    protected final Faction faction;
    protected final CommandSender who;

    public FactionInfoUpdateEvent(Faction faction, CommandSender sender) {
        this.faction = faction;
        this.who = sender;
    }

    public Faction getFaction() {
        return faction;
    }

    public CommandSender getWho() {
        return who;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
