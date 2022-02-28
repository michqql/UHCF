package me.michqql.uhcf.listeners.events.infoupdate;

import me.michqql.uhcf.faction.Faction;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.listeners.events.FactionInfoUpdateEvent;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class FactionClaimUpdateEvent extends FactionInfoUpdateEvent {

    private final Chunk chunk;
    private final boolean unclaim;

    public FactionClaimUpdateEvent(Faction faction, Player player, Chunk chunk, boolean unclaim) {
        super(faction, player);
        this.chunk = chunk;
        this.unclaim = unclaim;
    }

    public PlayerFaction getPlayerFaction() {
        return (PlayerFaction) faction;
    }

    public Player getPlayer() {
        return (Player) who;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public boolean isUnclaim() {
        return unclaim;
    }
}
