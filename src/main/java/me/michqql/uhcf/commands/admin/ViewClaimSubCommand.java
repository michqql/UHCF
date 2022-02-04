package me.michqql.uhcf.commands.admin;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.claim.ClaimsManager;
import me.michqql.uhcf.claim.view.ClaimViewingHandler;
import me.michqql.uhcf.faction.FactionsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;

public class ViewClaimSubCommand extends SubCommand {

    private final FactionsManager factionsManager;
    private final ClaimsManager claimsManager;

    public ViewClaimSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler,
                               FactionsManager factionsManager, ClaimsManager claimsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
        this.claimsManager = claimsManager;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] strings) {
        Player player = (Player) sender;

        ClaimViewingHandler.toggle((UHCFPlugin) bukkitPlugin, factionsManager, claimsManager, player);
        boolean enabled = ClaimViewingHandler.isViewing(player.getUniqueId());

        messageHandler.sendList(player, "admin-command.viewclaims.toggled", new HashMap<>(){{
            put("colour", enabled ? "&a" : "&c");
            put("color", enabled ? "&a" : "&c");
            put("enabled", enabled ? "enabled" : "disabled");
        }});
    }

    @Override
    protected String getName() {
        return "viewclaims";
    }

    @Override
    protected List<String> getAliases() {
        return List.of("view", "vc");
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
