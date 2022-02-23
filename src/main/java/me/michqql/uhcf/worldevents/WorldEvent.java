package me.michqql.uhcf.worldevents;

import me.michqql.core.data.IReadWrite;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.listeners.events.WorldEventEndEvent;
import me.michqql.uhcf.listeners.events.WorldEventStartEvent;
import org.bukkit.Bukkit;

public abstract class WorldEvent implements IReadWrite {

    protected final UHCFPlugin plugin;
    protected final String id;
    protected final long tickDelta;

    boolean active;
    long delta;

    public WorldEvent(UHCFPlugin plugin, String id, long tickDelta) {
        this.plugin = plugin;
        this.id = id;
        this.tickDelta = tickDelta;
    }

    void internalStart() {
        this.active = true;
        start();

        Bukkit.getPluginManager().callEvent(new WorldEventStartEvent(this));
    }

    void internalStop() {
        this.active = false;
        end();

        Bukkit.getPluginManager().callEvent(new WorldEventEndEvent(this));
    }

    protected abstract void start();
    protected abstract void tick();
    protected abstract void end();

    protected boolean isActive() {
        return active;
    }
}
