package me.michqql.uhcf.commands.admin;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;

public class TempJoinSubCommand extends SubCommand {

    private final FactionsManager factionsManager;

    public TempJoinSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler, FactionsManager factionsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if(args.length == 0) {
            messageHandler.sendList(sender, "command-usage",
                    Placeholder.of("command", "admin tempjoin <faction> [-p]"));
            return;
        }

        Player player = (Player) sender;

        String factionId = args[0];
        boolean pFlag = hasFlag("-p", args);

        if(pFlag) {
            PlayerFaction playerFaction = factionsManager.getPlayerFactionById(factionId);
            if(playerFaction == null) {
                messageHandler.sendList(player, "admin-command.temp-join.unknown-faction",
                        Placeholder.of("faction", factionId, "faction.type", "player"));
                return;
            }

            factionsManager.setTemporaryFaction(player.getUniqueId(), playerFaction);
            messageHandler.sendList(player, "admin-command.temp-join.joined", new HashMap<>(){{
                put("faction", playerFaction.getDisplayName());
                put("faction.type", "player");
                put("faction.id", playerFaction.getUniqueIdentifier());
            }});
        } else {
            AdminFaction adminFaction = factionsManager.getAdminFactionById(factionId);
            if(adminFaction == null) {
                messageHandler.sendList(player, "admin-command.temp-join.unknown-faction",
                        Placeholder.of("faction", factionId, "faction.type", "admin"));
                return;
            }

            factionsManager.setTemporaryFaction(player.getUniqueId(), adminFaction);
            messageHandler.sendList(player, "admin-command.temp-join.joined", new HashMap<>(){{
                put("faction", adminFaction.getDisplayName());
                put("faction.type", "admin");
                put("faction.id", adminFaction.getUniqueIdentifier());
            }});
        }
    }

    @Override
    protected String getName() {
        return "tempjoin";
    }

    @Override
    protected List<String> getAliases() {
        return List.of("tj");
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
