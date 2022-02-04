package me.michqql.uhcf.commands.admin;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.claim.Claim;
import me.michqql.uhcf.claim.ClaimsManager;
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

public class UnclaimLandSubCommand extends SubCommand {

    private final FactionsManager factionsManager;
    private final ClaimsManager claimsManager;

    public UnclaimLandSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler,
                                 FactionsManager factionsManager, ClaimsManager claimsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
        this.claimsManager = claimsManager;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        // Check player is temporary faction member
        Faction faction = factionsManager.getTemporaryFactionByPlayer(player.getUniqueId());
        if(faction == null) {
            messageHandler.sendList(player, "admin-command.no-temporary-faction");
            return;
        }

        Chunk location = player.getLocation().getChunk();

        // Check whether land is claimed or not
        Claim claim = claimsManager.getClaimByChunk(location);
        if(claim == null) {
            messageHandler.sendList(player, "admin-command.unclaim.chunk-not-claimed");
            return;
        }

        // Unclaim land
        if(faction instanceof AdminFaction af) {
            claimsManager.unclaim(location);

            messageHandler.sendList(player, "admin-command.unclaim.chunk-unclaimed", new HashMap<>(){{
                put("faction.type", "admin");
                put("faction", af.getDisplayName());
                put("faction.name", af.getDisplayName());
                put("faction.id", af.getUniqueIdentifier());
            }});
        }
        else if(faction instanceof PlayerFaction pf) {
            // Check if this chunk will split claim into two areas
            // if so, do not unclaim
            boolean canUnclaim = pf.getClaim().canUnclaim(location);
            if(!canUnclaim) {
                messageHandler.sendList(player, "admin-command.unclaim.cannot-unclaim");
                return;
            }

            // Unclaim the land
            claimsManager.unclaim(location);

            messageHandler.sendList(player, "admin-command.unclaim.chunk-unclaimed", new HashMap<>(){{
                put("faction.type", "player");
                put("faction", pf.getDisplayName());
                put("faction.name", pf.getDisplayName());
                put("faction.id", pf.getUniqueIdentifier());
            }});
        }
    }

    @Override
    protected String getName() {
        return "unclaim";
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
