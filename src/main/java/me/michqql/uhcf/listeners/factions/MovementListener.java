package me.michqql.uhcf.listeners.factions;

import me.michqql.core.util.AbstractListener;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.claim.view.ClaimViewingHandler;
import me.michqql.uhcf.events.PlayerChunkChangeEvent;
import org.bukkit.event.EventHandler;

public class MovementListener extends AbstractListener {

    public MovementListener(UHCFPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onChunkChange(PlayerChunkChangeEvent e) {
        ClaimViewingHandler.update(e.getPlayer().getUniqueId());
    }
}
