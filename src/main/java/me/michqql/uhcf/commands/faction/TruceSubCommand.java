package me.michqql.uhcf.commands.faction;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Members;
import me.michqql.uhcf.faction.attributes.Relations;
import me.michqql.uhcf.faction.roles.FactionPermission;
import me.michqql.uhcf.faction.roles.FactionRole;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class TruceSubCommand extends SubCommand {

    private final FactionsManager factionsManager;

    public TruceSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler, FactionsManager factionsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if(args.length == 0) {
            messageHandler.sendList(sender, "command-usage",
                    Placeholder.of("command", "faction truce <faction>"));
            return;
        }

        Player player = (Player) sender;
        PlayerFaction faction = factionsManager.getPlayerFactionByPlayer(player.getUniqueId());
        if(faction == null) {
            messageHandler.sendList(player, "faction-command.not-in-faction");
            return;
        }

        Members members = faction.getMembers();
        if(!members.getFactionRole(player.getUniqueId())
                .hasPermission(FactionPermission.MANAGE_RELATIONS)) {
            messageHandler.sendList(player, "faction-command.no-permission",
                    Placeholder.of("role", FactionPermission.MANAGE_RELATIONS.getDefaultRole().toString()));
            return;
        }

        String factionId = args[0];
        PlayerFaction other = factionsManager.getPlayerFactionById(factionId);
        if(other == null) {
            messageHandler.sendList(player, "faction-command.truce.could-not-find-faction", new HashMap<>(){{
                put("faction", factionId);
                put("faction.id", factionId);
                put("faction.name", factionId);
            }});
            return;
        }

        Relations relations = faction.getRelations();
        if(relations.isTruce(other)) {
            messageHandler.sendList(player, "faction-command.truce.already-truced", new HashMap<>(){{
                put("faction", other.getDisplayName());
                put("faction.id", other.getUniqueIdentifier());
                put("faction.name", other.getDisplayName());
            }});
            return;
        }

        // Check if distributed size is below maximum
        if(!factionsManager.canHaveRelation(faction, other, Relations.Relation.TRUCE)) {
            messageHandler.sendList(player, "faction-command.truce.cannot-truce-size", new HashMap<>(){{
                put("faction", faction.getDisplayName());
                put("faction.name", faction.getDisplayName());
                put("faction.id", faction.getUniqueIdentifier());
            }});
            return;
        }

        // Check if the specified faction has requested alliance with player's
        if(factionsManager.isRequestingRelation(other, faction, Relations.Relation.TRUCE)) {
            // Accept the request
            factionsManager.acceptRequest(other, faction);

            for(Player online : members.getOnlinePlayers()) {
                messageHandler.sendList(online, "faction-command.truce.accepted.player-faction", new HashMap<>(){{
                    put("faction", other.getDisplayName());
                    put("faction.id", other.getUniqueIdentifier());
                    put("faction.name", other.getDisplayName());
                }});
            }

            for(Player online : other.getMembers().getOnlinePlayers()) {
                messageHandler.sendList(online, "faction-command.truce.accepted.other-faction", new HashMap<>(){{
                    put("faction", faction.getDisplayName());
                    put("faction.id", faction.getUniqueIdentifier());
                    put("faction.name", faction.getDisplayName());
                }});
            }
        } else {
            // Specified faction has not requested, player's faction wants to send the request
            factionsManager.requestRelation(faction, other, Relations.Relation.TRUCE);

            for(Player online : members.getOnlinePlayers()) {
                messageHandler.sendList(online, "faction-command.truce.requested.player-faction", new HashMap<>(){{
                    put("member", player.getName());
                    put("player", player.getName());
                    put("faction", other.getDisplayName());
                    put("faction.id", other.getUniqueIdentifier());
                    put("faction.name", other.getDisplayName());
                }});
            }

            for(Player online : other.getMembers().getOnlinePlayers()) {
                messageHandler.sendList(online, "faction-command.truce.requested.other-faction", new HashMap<>(){{
                    put("player", player.getName());
                    put("faction", faction.getDisplayName());
                    put("faction.id", faction.getUniqueIdentifier());
                    put("faction.name", faction.getDisplayName());
                }});
            }
        }
    }

    @Override
    protected String getName() {
        return "truce";
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
    protected List<String> getArguments(CommandSender sender) {
        Player player = (Player) sender;
        PlayerFaction faction = factionsManager.getPlayerFactionByPlayer(player.getUniqueId());
        if(faction == null)
            return null;

        Members members = faction.getMembers();
        FactionRole playerRole = members.getFactionRole(player.getUniqueId());
        if(!playerRole.hasPermission(FactionPermission.MANAGE_RELATIONS))
            return null;

        List<String> arguments = new ArrayList<>();

        Collection<PlayerFaction> factions = factionsManager.getPlayerFactions();
        Relations relations = faction.getRelations();
        for(PlayerFaction other : factions) {
            if(!relations.isFriendly(other))
                arguments.add(other.getDisplayName());
        }

        return arguments;
    }

    @Override
    protected boolean requiresPlayer() {
        return true;
    }
}
