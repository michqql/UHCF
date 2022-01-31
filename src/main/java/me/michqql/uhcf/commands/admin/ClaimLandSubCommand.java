package me.michqql.uhcf.commands.admin;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.claim.AdminClaim;
import me.michqql.uhcf.claim.Claim;
import me.michqql.uhcf.claim.ClaimsManager;
import me.michqql.uhcf.claim.PlayerClaim;
import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.Faction;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;

public class ClaimLandSubCommand extends SubCommand {

    private final FactionsManager factionsManager;
    private final ClaimsManager claimsManager;

    public ClaimLandSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler,
                               FactionsManager factionsManager, ClaimsManager claimsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
        this.claimsManager = claimsManager;
    }

    @Override
    protected void registerSubCommands() {

    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        // Check player is temporary faction member
        Faction faction = factionsManager.getTemporaryFactionByPlayer(player.getUniqueId());
        if(faction == null) {
            messageHandler.sendList(player, "admin-command.claim.no-temp-faction");
            return;
        }

        Chunk location = player.getLocation().getChunk();

        // Check whether land is claimed or not
        Claim claim = claimsManager.getClaimFromChunk(location);
        if(claim != null) {
            String displayName = "unknown";
            if(claim instanceof AdminClaim ac)
                displayName = ac.getAdminFactionOwner().getDisplayName();
            else if(claim instanceof PlayerClaim pc)
                displayName = pc.getOwningFaction().getDisplayName();

            messageHandler.sendList(player, "admin-command.claim.chunk-already-claimed",
                    Placeholder.of("faction", displayName));
            return;
        }

        // Claim land
        if(faction instanceof AdminFaction af) {
            Claim current = claimsManager.claimAdminChunk(af, location);

            if(current == null) {
                messageHandler.sendList(player, "admin-command.claim.chunk-claimed", new HashMap<>() {{
                    put("type", "admin");
                    put("faction", faction.getDisplayName());
                }});
            } else {
                sendAlreadyClaimedMessage(player, current);
            }
        }
        else if(faction instanceof PlayerFaction pf) {
            Claim current = claimsManager.claimPlayer3x3(pf, location);

            if(current == null) {
                messageHandler.sendList(player, "admin-command.claim.chunk-claimed", new HashMap<>() {{
                    put("type", "player");
                    put("faction", faction.getDisplayName());
                }});
            } else {
                sendAlreadyClaimedMessage(player, current);
            }
        }
    }

    private void sendAlreadyClaimedMessage(Player player, Claim claim) {
        String displayName = "unknown";
        if(claim instanceof AdminClaim ac)
            displayName = ac.getAdminFactionOwner().getDisplayName();
        else if(claim instanceof PlayerClaim pc)
            displayName = pc.getOwningFaction().getDisplayName();

        messageHandler.sendList(player, "admin-command.claim.chunk-already-claimed",
                Placeholder.of("faction", displayName));
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
        return UHCFPlugin.ADMIN_PERMISSION;
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
