package me.michqql.uhcf.listeners.events.infoupdate;

import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Relations;
import me.michqql.uhcf.listeners.events.FactionInfoUpdateEvent;
import org.bukkit.command.CommandSender;

public class FactionRelationUpdateEvent extends FactionInfoUpdateEvent {

    private final Relations.Relation previous, current;
    private final PlayerFaction other;

    public FactionRelationUpdateEvent(PlayerFaction playerFaction, CommandSender sender,
                                      PlayerFaction other, Relations.Relation from, Relations.Relation to) {
        super(playerFaction, sender);
        this.previous = from;
        this.current = to;
        this.other = other;
    }

    public PlayerFaction getPlayerFaction() {
        return (PlayerFaction) faction;
    }

    public PlayerFaction getOtherFaction() {
        return other;
    }

    public Relations.Relation getPreviousRelationStatus() {
        return previous;
    }

    public Relations.Relation getCurrentRelationStatus() {
        return current;
    }
}
