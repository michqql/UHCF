package me.michqql.uhcf.raiding;

import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.listeners.events.raid.FactionRaidEvent;
import me.michqql.uhcf.util.EventUtil;
import org.bukkit.Bukkit;

public final class Raid {

    private final UHCFPlugin plugin;

    private final PlayerFaction raiders;
    private final PlayerFaction defenders;

    private final long startTimestamp;
    private final long raidDuration;
    private final long endTimestamp;

    private boolean ended;

    public Raid(UHCFPlugin plugin, PlayerFaction raiders, PlayerFaction defenders, long raidDuration) {
        this.plugin = plugin;

        this.raiders = raiders;
        this.defenders = defenders;

        this.startTimestamp = System.currentTimeMillis();
        this.raidDuration = raidDuration;
        this.endTimestamp = startTimestamp + raidDuration;

        callEvents();
    }

    private void callEvents() {
        // Start event
        onStart();
        EventUtil.call(new FactionRaidEvent(this, FactionRaidEvent.Type.START));

        // Schedule end event
        // Convert raid duration (ms) to delay (ticks) by dividing by 50
        // Only call event if the raid has not already ended (e.g. via force stop)
        long delay = raidDuration / 50;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(!ended) {
                onEnd();
                EventUtil.call(new FactionRaidEvent(this, FactionRaidEvent.Type.END));
            }
        }, delay);
    }

    private void onStart() {

    }

    private void onEnd() {
        this.ended = true;

        raiders.getWarpoints().setWarpoints(defenders, 0);
        defenders.getWarpoints().setWarpoints(raiders, 0);
    }

    void forceEnd() {
        onEnd();
        EventUtil.call(new FactionRaidEvent(this, FactionRaidEvent.Type.END, true));
    }

    public PlayerFaction getRaiders() {
        return raiders;
    }

    public boolean isRaider(PlayerFaction faction) {
        return raiders.equals(faction);
    }

    public PlayerFaction getDefenders() {
        return defenders;
    }

    public PlayerFaction getOther(PlayerFaction faction) {
        if(raiders.equals(faction))
            return defenders;
        else if(defenders.equals(faction))
            return raiders;
        else
            return null;
    }

    public boolean isDefender(PlayerFaction faction) {
        return defenders.equals(faction);
    }

    public boolean isInvolved(PlayerFaction faction) {
        return defenders.equals(faction) || raiders.equals(faction);
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getRaidDuration() {
        return raidDuration;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public long getTimeRemaining() {
        return endTimestamp - System.currentTimeMillis();
    }
}
