package me.michqql.uhcf.listeners;

import me.michqql.core.util.AbstractListener;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.events.PlayerChunkChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Used to listen to events that will call subsequent custom events
 * Does nothing except call custom events
 */
public class CustomEventListener extends AbstractListener {

    public CustomEventListener(UHCFPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();

        // PlayerChunkChangeEvent
        if(to != null && !from.getChunk().equals(to.getChunk())) {
            Bukkit.getPluginManager().callEvent(new PlayerChunkChangeEvent(
                    e.getPlayer(),
                    from.getChunk(),
                    to.getChunk()
            ));
        }
    }
}
