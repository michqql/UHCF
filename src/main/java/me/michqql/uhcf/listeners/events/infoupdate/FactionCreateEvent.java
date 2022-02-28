package me.michqql.uhcf.listeners.events.infoupdate;

import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.listeners.events.FactionInfoUpdateEvent;
import org.bukkit.entity.Player;

public class FactionCreateEvent extends FactionInfoUpdateEvent {

    public FactionCreateEvent(PlayerFaction faction, Player player) {
        super(faction, player);
    }

    public PlayerFaction getPlayerFaction() {
        return (PlayerFaction) faction;
    }

    public Player getCreator() {
        return (Player) who;
    }

    public Player getPlayer() {
        return getCreator();
    }
}
