package me.michqql.uhcf.listeners.building;

import me.michqql.core.util.AbstractListener;

import me.michqql.core.util.MessageHandler;
import me.michqql.uhcf.claim.AdminClaim;
import me.michqql.uhcf.claim.Claim;
import me.michqql.uhcf.claim.ClaimsManager;
import me.michqql.uhcf.claim.PlayerClaim;
import me.michqql.uhcf.claim.outline.ClaimOutlineManager;
import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.Faction;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.roles.FactionPermission;
import me.michqql.uhcf.faction.roles.FactionRole;

import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

public class BlockListener extends AbstractListener {

    private final MessageHandler messageHandler;

    private final FactionsManager factionsManager;
    private final ClaimsManager claimsManager;

    private final ClaimOutlineManager claimOutlineManager;

    public BlockListener(Plugin plugin, MessageHandler messageHandler,
                         FactionsManager factionsManager, ClaimsManager claimsManager,
                         ClaimOutlineManager claimOutlineManager) {
        super(plugin);
        this.messageHandler = messageHandler;
        this.factionsManager = factionsManager;
        this.claimsManager = claimsManager;
        this.claimOutlineManager = claimOutlineManager;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        // Method handles messages too
        if(isInteractionBlocked(player, block)) {
            e.setCancelled(true);

            Claim claim = claimsManager.getClaimByChunk(block.getChunk());
            Faction fac = claim.getOwningFaction();
            messageHandler.sendList(player, "interactions", new HashMap<>(){{
                put("interaction", "break blocks");
                put("faction", fac.getDisplayName());
                put("faction.name", fac.getDisplayName());
                put("faction.id", fac.getUniqueIdentifier());
            }});
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        if(isInteractionBlocked(player, block)) {
            e.setCancelled(true);

            Claim claim = claimsManager.getClaimByChunk(block.getChunk());
            Faction fac = claim.getOwningFaction();
            messageHandler.sendList(player, "interactions", new HashMap<>(){{
                put("interaction", "place blocks");
                put("faction", fac.getDisplayName());
                put("faction.name", fac.getDisplayName());
                put("faction.id", fac.getUniqueIdentifier());
            }});
        }
    }

    @EventHandler
    public void onOpen(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if(!e.hasBlock())
            return;

        Block block = e.getClickedBlock();
        Action action = e.getAction();
        if(action != Action.RIGHT_CLICK_BLOCK)
            return;

        assert block != null; // Checked by e.hasBlock

        if(isInteractionBlocked(player, block)) {
            e.setCancelled(true);

            Claim claim = claimsManager.getClaimByChunk(block.getChunk());
            Faction fac = claim.getOwningFaction();
            messageHandler.sendList(player, "interactions", new HashMap<>(){{
                put("interaction", "interact");
                put("faction", fac.getDisplayName());
                put("faction.name", fac.getDisplayName());
                put("faction.id", fac.getUniqueIdentifier());
            }});
        }
    }

    private boolean isInteractionBlocked(Player player, Block block) {
        UUID uuid = player.getUniqueId();

        Claim claim = claimsManager.getClaimByChunk(block.getChunk());
        if(claim == null) // Check borders? TODO: implement world border feature/borderlands
            return false;

        // Check if player is temporarily in admin faction
        if(claim instanceof AdminClaim adminClaim) {
            AdminFaction adminFaction = adminClaim.getAdminFactionOwner();
            Faction temp = factionsManager.getTemporaryFactionByPlayer(uuid);
            return !adminFaction.equals(temp);
        }

        PlayerClaim playerClaim = (PlayerClaim) claim;
        PlayerFaction owner = playerClaim.getOwningFaction();

        if(!owner.getMembers().isInFaction(uuid))
            return true;

        if(claimOutlineManager.onBlockBreak(block))
            return true;

        FactionRole role = owner.getMembers().getFactionRole(uuid);
        if(!role.hasPermission(FactionPermission.BLOCK_ACCESS))
            return true;

        if(block instanceof Container && !role.hasPermission(FactionPermission.CHEST_ACCESS))
            return true;

        if(block.getBlockData() instanceof Openable && !role.hasPermission(FactionPermission.INTERACTIONS))
            return true;

        return false;
    }
}
