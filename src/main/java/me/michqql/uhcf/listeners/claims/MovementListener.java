package me.michqql.uhcf.listeners.claims;

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
        // Displays claims to admins that have /admin viewclaims toggled on
        ClaimViewingHandler.update(e.getPlayer().getUniqueId());

        // Send title message
    }
}
