package me.michqql.uhcf.listeners.events.infoupdate;

import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.roles.FactionRole;
import me.michqql.uhcf.listeners.events.FactionInfoUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class FactionMemberUpdateEvent extends FactionInfoUpdateEvent {

    private final UUID memberUUID;
    private final FactionRole previous, current;

    public FactionMemberUpdateEvent(PlayerFaction playerFaction, CommandSender sender,
                                    UUID uuid, FactionRole from, FactionRole to) {
        super(playerFaction, sender);
        this.memberUUID = uuid;
        this.previous = from;
        this.current = to;
    }

    public PlayerFaction getPlayerFaction() {
        return (PlayerFaction) faction;
    }

    public UUID getMemberUUID() {
        return memberUUID;
    }

    public OfflinePlayer getMemberAsOfflinePlayer() {
        return Bukkit.getOfflinePlayer(memberUUID);
    }

    /**
     * Gets the last role of this member
     * If this is null, or {@link FactionRole}.NONE then the player has just joined the faction
     * and their current role <italic>should</italic> be {@link FactionRole}.RECRUIT
     * @return the current role of the player
     */
    public FactionRole getPreviousRole() {
        return previous;
    }

    /**
     * Gets the new role of this member
     * If this is null, or {@link FactionRole}.NONE then the player is no longer in the faction (left or kicked)
     * @return the current role of the player
     */
    public FactionRole getCurrentRole() {
        return current;
    }
}
