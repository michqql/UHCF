package me.michqql.uhcf.commands.faction;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.claim.ClaimsManager;
import me.michqql.uhcf.claim.PlayerClaim;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.roles.FactionPermission;
import me.michqql.uhcf.faction.roles.FactionRole;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;

public class UnclaimSubCommand extends SubCommand {

    private final FactionsManager factionsManager;
    private final ClaimsManager claimsManager;

    public UnclaimSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler,
                             FactionsManager factionsManager, ClaimsManager claimsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
        this.claimsManager = claimsManager;
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
        if(!claim.isClaimed(chunk)) {
            messageHandler.sendList(player, "faction-command.unclaim.chunk-not-owned");
            return;
        }

        if(!claim.canUnclaim(chunk)) {
            messageHandler.sendList(player, "faction-command.unclaim.cannot-unclaim");
            return;
        }

        claimsManager.unclaim(chunk);

        messageHandler.sendList(player, "faction-command.unclaim.unclaimed", new HashMap<>(){{
            put("player", player.getName());
            put("member", player.getName());
        }});
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
