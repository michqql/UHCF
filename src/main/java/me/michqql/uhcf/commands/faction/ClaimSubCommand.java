package me.michqql.uhcf.commands.faction;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.claim.Claim;
import me.michqql.uhcf.claim.ClaimsManager;
import me.michqql.uhcf.claim.PlayerClaim;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.roles.FactionPermission;
import me.michqql.uhcf.faction.roles.FactionRole;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;

public class ClaimSubCommand extends SubCommand {

    private final FactionsManager factionsManager;
    private final ClaimsManager claimsManager;

    private final FileConfiguration config;

    public ClaimSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler,
                           FactionsManager factionsManager, ClaimsManager claimsManager,
                           FileConfiguration config) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
        this.claimsManager = claimsManager;
        this.config = config;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerFaction faction = factionsManager.getPlayerFactionByPlayer(player.getUniqueId());
        if(faction == null) {
            messageHandler.sendList(player, "faction-command.not-in-faction");
            return;
        }

        FactionRole role = faction.getMembers().getFactionRole(player.getUniqueId());
        if(!role.hasPermission(FactionPermission.MANAGE_CLAIMS)) {
            messageHandler.sendList(player, "faction-command.no-permission",
                    Placeholder.of("role", FactionPermission.MANAGE_CLAIMS.getDefaultRole().toString()));
            return;
        }

        Chunk chunk = player.getLocation().getChunk();
        PlayerClaim claim = faction.getClaim();
        int size = claim.getNumberOfChunks();
        int max = config.getInt("player-claims.maximum");
        if(size >= max) {
            messageHandler.sendList(player, "faction-command.claim.claimed-max",
                    Placeholder.of("chunks", String.valueOf(max)));
            return;
        }

        int maxForFaction = getMaximumClaims(faction, max);
        if(size >= maxForFaction) {
            messageHandler.sendList(player, "faction-command.claim.claimed-limit",
                    Placeholder.of("chunks", String.valueOf(maxForFaction)));
            return;
        }

        if(!claimsManager.isWorldClaimable(chunk.getWorld())) {
            messageHandler.sendList(player, "faction-command.claim.cannot-claim-world");
            return;
        }

        if(!claim.isAdjacent(chunk)) {
            messageHandler.sendList(player, "faction-command.claim.not-adjacent");
            return;
        }

        // Claim
        Claim previous = claimsManager.claimPlayerChunk(faction, player.getLocation().getChunk());

        // Another claim was obstructing
        if(previous != null) {
            messageHandler.sendList(player, "faction-command.claim.obstructed");
            return;
        }

        // Successfully claimed
        messageHandler.sendList(player, "faction-command.claim.claimed", new HashMap<>(){{
            put("player", player.getName());
            put("member", player.getName());
            put("faction.claims.size", String.valueOf(claim.getNumberOfChunks()));
            put("faction.claims.max", String.valueOf(maxForFaction));
        }});
    }

    private int getMaximumClaims(PlayerFaction faction, int max) {
        int def = config.getInt("player-claims.default");
        int increase = config.getInt("player-claims.increase_per_member");

        return Math.min(max, def + (increase * (faction.getMembers().getSize() - 1)));
    }

    @Override
    protected String getName() {
        return "claim";
    }

    @Override
    protected List<String> getAliases() {
        return null;
    }

    @Override
    protected String getPermission() {
        return "";
    }

    @Override
    protected List<String> getArguments(CommandSender commandSender) {
        return null;
    }

    @Override
    protected boolean requiresPlayer() {
        return true;
    }
}
