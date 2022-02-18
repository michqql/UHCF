package me.michqql.uhcf.listeners.building;

import me.michqql.core.util.AbstractListener;

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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class BlockListener extends AbstractListener {

    private final FactionsManager factionsManager;
    private final ClaimsManager claimsManager;

    private final ClaimOutlineManager claimOutlineManager;

    public BlockListener(Plugin plugin, FactionsManager factionsManager, ClaimsManager claimsManager,
                         ClaimOutlineManager claimOutlineManager) {
        super(plugin);
        this.factionsManager = factionsManager;
        this.claimsManager = claimsManager;
        this.claimOutlineManager = claimOutlineManager;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        e.setCancelled(!canPlayerInteractInChunk(player, e.getBlock()));
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        e.setCancelled(!canPlayerInteractInChunk(player, e.getBlock()));
    }

    private boolean canPlayerInteractInChunk(Player player, Block block) {
        UUID uuid = player.getUniqueId();

        Claim claim = claimsManager.getClaimByChunk(player.getLocation().getChunk());
        if(claim == null) // Check borders? TODO: implement world border feature/borderlands
            return true;

        // Check if player is temporarily in admin faction
        if(claim instanceof AdminClaim adminClaim) {
            AdminFaction adminFaction = adminClaim.getAdminFactionOwner();
            Faction temp = factionsManager.getTemporaryFactionByPlayer(uuid);
            return adminFaction.equals(temp);
        }

        PlayerClaim playerClaim = (PlayerClaim) claim;
        PlayerFaction owner = playerClaim.getOwningFaction();

        if(!owner.getMembers().isInFaction(uuid))
            return false;

        if(claimOutlineManager.onBlockBreak(block))
            return false;

        FactionRole role = owner.getMembers().getFactionRole(uuid);
        if(!role.hasPermission(FactionPermission.BLOCK_ACCESS))
            return false;

        if(block instanceof Container && !role.hasPermission(FactionPermission.CHEST_ACCESS))
            return false;

        if(block.getBlockData() instanceof Openable && !role.hasPermission(FactionPermission.INTERACTIONS))
            return false;

        return true;
    }
}
