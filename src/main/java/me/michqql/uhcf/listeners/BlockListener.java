package me.michqql.uhcf.listeners;

import me.michqql.core.io.CommentFile;
import me.michqql.core.util.AbstractListener;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;

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

import me.michqql.uhcf.raiding.Raid;
import me.michqql.uhcf.raiding.RaidList;
import me.michqql.uhcf.raiding.RaidManager;
import org.bukkit.Location;
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
    private final RaidManager raidManager;

    private final ClaimOutlineManager claimOutlineManager;

    // Config
    private final int borderlandsDist;

    public BlockListener(Plugin plugin, CommentFile config, MessageHandler messageHandler,
                         FactionsManager factionsManager, ClaimsManager claimsManager,
                         RaidManager raidManager, ClaimOutlineManager claimOutlineManager) {
        super(plugin);
        this.messageHandler = messageHandler;
        this.factionsManager = factionsManager;
        this.claimsManager = claimsManager;
        this.raidManager = raidManager;
        this.claimOutlineManager = claimOutlineManager;

        // Load config
        this.borderlandsDist = config.getConfig().getInt("borderlands-distance", 1000);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        final Player player = e.getPlayer();
        final UUID uuid = player.getUniqueId();
        final Block block = e.getBlock();

        final Claim claim = claimsManager.getClaimByChunk(block.getChunk());

        // Check if player is in borderlands or wilderness
        if(claim == null) {
            Location loc = player.getLocation();
            double x = Math.abs(loc.getX());
            double z = Math.abs(loc.getZ());

            if(x >= borderlandsDist || z >= borderlandsDist) {
                e.setCancelled(true);
                messageHandler.sendList(player, "blocked-interactions.borderlands");
                return;
            }

            // Player is in wilderness and can break blocks
            return;
        }

        // Check if block is in admin claim (shops, spawn, etc...)
        // If player is temp admin, allow interaction
        if(claim instanceof AdminClaim adminClaim) {
            AdminFaction adminFaction = adminClaim.getAdminFactionOwner();
            Faction temp = factionsManager.getTemporaryFactionByPlayer(uuid);
            if(adminFaction.equals(temp))
                return;

            e.setCancelled(true);
            messageHandler.sendList(player, "blocked-interactions.admin-claim", new HashMap<>(){{
                put("faction.type", "admin");
                put("faction", adminFaction.getDisplayName());
                put("faction.name", adminFaction.getDisplayName());
                put("faction.id", adminFaction.getUniqueIdentifier());
            }});
            return;
        }

        PlayerClaim playerClaim = (PlayerClaim) claim;
        PlayerFaction owner = playerClaim.getOwningFaction();

        // Check if player is not a member of owning faction
        if(!owner.getMembers().isInFaction(uuid)) {
            e.setCancelled(true);
            messageHandler.sendList(player, "blocked-interactions.player-claim", new HashMap<>(){{
                put("faction.type", "admin");
                put("faction", owner.getDisplayName());
                put("faction.name", owner.getDisplayName());
                put("faction.id", owner.getUniqueIdentifier());
            }});
            return;
        }

        // Check if the block being broken is an outline block
        if(claimOutlineManager.onBlockBreak(block)) {
            e.setCancelled(true);
            return;
        }

        FactionRole role = owner.getMembers().getFactionRole(uuid);
        if(!role.hasPermission(FactionPermission.BLOCK_ACCESS)) {
            e.setCancelled(true);
            messageHandler.sendList(player, "blocked-interactions.no-block-access",
                    Placeholder.of("role", FactionPermission.BLOCK_ACCESS.getDefaultRole().toString()));
        }

        // Player is allowed to interact
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        final Player player = e.getPlayer();
        final UUID uuid = player.getUniqueId();
        final Block block = e.getBlockPlaced();

        final Claim claim = claimsManager.getClaimByChunk(block.getChunk());

        // Check if player is in borderlands or wilderness
        if(claim == null) {
            Location loc = player.getLocation();
            double x = Math.abs(loc.getX());
            double z = Math.abs(loc.getZ());

            if(x >= borderlandsDist || z >= borderlandsDist) {
                e.setCancelled(true);
                messageHandler.sendList(player, "blocked-interactions.borderlands");
                return;
            }

            // Player is in wilderness and can break blocks
            return;
        }

        // Check if block is in admin claim (shops, spawn, etc...)
        // If player is temp admin, allow interaction
        if(claim instanceof AdminClaim adminClaim) {
            AdminFaction adminFaction = adminClaim.getAdminFactionOwner();
            Faction temp = factionsManager.getTemporaryFactionByPlayer(uuid);
            if(adminFaction.equals(temp))
                return;

            e.setCancelled(true);
            messageHandler.sendList(player, "blocked-interactions.admin-claim", new HashMap<>(){{
                put("faction.type", "admin");
                put("faction", adminFaction.getDisplayName());
                put("faction.name", adminFaction.getDisplayName());
                put("faction.id", adminFaction.getUniqueIdentifier());
            }});
            return;
        }

        PlayerClaim playerClaim = (PlayerClaim) claim;
        PlayerFaction owner = playerClaim.getOwningFaction();

        // Check if player is not a member of owning faction
        if(!owner.getMembers().isInFaction(uuid)) {
            e.setCancelled(true);
            messageHandler.sendList(player, "blocked-interactions.player-claim", new HashMap<>(){{
                put("faction.type", "admin");
                put("faction", owner.getDisplayName());
                put("faction.name", owner.getDisplayName());
                put("faction.id", owner.getUniqueIdentifier());
            }});
            return;
        }

        FactionRole role = owner.getMembers().getFactionRole(uuid);
        if(!role.hasPermission(FactionPermission.BLOCK_ACCESS)) {
            e.setCancelled(true);
            messageHandler.sendList(player, "blocked-interactions.no-block-access",
                    Placeholder.of("role", FactionPermission.BLOCK_ACCESS.getDefaultRole().toString()));
            return;
        }

        // Check if owner faction is being raided
        RaidList raids = raidManager.getRaidsByFaction(owner);
        if(raids.isDefending()) {
            // If they are, stop faction members from placing blocks
            e.setCancelled(true);
            messageHandler.sendList(player, "blocked-interactions.raided");
        }

        // Player is allowed to place blocks
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        final Player player = e.getPlayer();
        final UUID uuid = player.getUniqueId();
        if(!e.hasBlock())
            return;

        final Block block = e.getClickedBlock();
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        assert block != null; // Checked by e.hasBlock
        final Claim claim = claimsManager.getClaimByChunk(block.getChunk());

        final boolean isChest = block.getState() instanceof Container;
        final boolean isDoor = block.getBlockData() instanceof Openable;

        // Check if player is in borderlands or wilderness
        if(claim == null) {
            Location loc = player.getLocation();
            double x = Math.abs(loc.getX());
            double z = Math.abs(loc.getZ());

            if((x >= borderlandsDist || z >= borderlandsDist) && !isChest) {
                e.setCancelled(true);
                messageHandler.sendList(player, "blocked-interactions.borderlands");
                return;
            }

            // Player is in wilderness and can interact
            return;
        }

        // Check if block is in admin claim (shops, spawn, etc...)
        // If player is temp admin, allow interaction
        if(claim instanceof AdminClaim adminClaim) {
            AdminFaction adminFaction = adminClaim.getAdminFactionOwner();
            Faction temp = factionsManager.getTemporaryFactionByPlayer(uuid);
            if(adminFaction.equals(temp))
                return;

            e.setCancelled(true);
            messageHandler.sendList(player, "blocked-interactions.admin-claim", new HashMap<>(){{
                put("faction.type", "admin");
                put("faction", adminFaction.getDisplayName());
                put("faction.name", adminFaction.getDisplayName());
                put("faction.id", adminFaction.getUniqueIdentifier());
            }});
            return;
        }

        PlayerClaim playerClaim = (PlayerClaim) claim;
        PlayerFaction owner = playerClaim.getOwningFaction();

        // Check if owner faction is being raided
        PlayerFaction playerFaction = factionsManager.getPlayerFactionByPlayer(player.getUniqueId());
        Raid raid = raidManager.getRaidByFactionsExact(playerFaction, owner);
        if(raid != null) {
            // Allow player to open containers but not doors
            if(isChest)
                return;
        }

        // Check if player is not a member of owning faction
        if(!owner.getMembers().isInFaction(uuid)) {
            e.setCancelled(true);
            messageHandler.sendList(player, "blocked-interactions.player-claim", new HashMap<>(){{
                put("faction.type", "admin");
                put("faction", owner.getDisplayName());
                put("faction.name", owner.getDisplayName());
                put("faction.id", owner.getUniqueIdentifier());
            }});
            return;
        }

        FactionRole role = owner.getMembers().getFactionRole(uuid);
        if(isChest && !role.hasPermission(FactionPermission.CHEST_ACCESS)) {
            e.setCancelled(true);
            messageHandler.sendList(player, "blocked-interactions.no-chest-access",
                    Placeholder.of("role", FactionPermission.CHEST_ACCESS.getDefaultRole().toString()));
            return;
        }

        if(isDoor && !role.hasPermission(FactionPermission.INTERACTIONS)) {
            e.setCancelled(true);
            messageHandler.sendList(player, "blocked-interactions.no-access",
                    Placeholder.of("role", FactionPermission.INTERACTIONS.getDefaultRole().toString()));
        }

        // Player is allowed to interact
    }
}
