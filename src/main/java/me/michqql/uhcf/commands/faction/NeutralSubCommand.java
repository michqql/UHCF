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
import me.michqql.uhcf.listeners.events.infoupdate.FactionRelationUpdateEvent;
import me.michqql.uhcf.util.EventUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NeutralSubCommand extends SubCommand {

    private final FactionsManager factionsManager;

    public NeutralSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler, FactionsManager factionsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if(args.length == 0) {
            messageHandler.sendList(sender, "command-usage",
                    Placeholder.of("command", "faction neutral <faction>"));
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
            messageHandler.sendList(player, "faction-command.neutral.could-not-find-faction", new HashMap<>(){{
                put("faction", factionId);
                put("faction.id", factionId);
                put("faction.name", factionId);
            }});
            return;
        }

        Relations relations = faction.getRelations();
        if(!relations.isFriendly(other)) {
            messageHandler.sendList(player, "faction-command.neutral.no-relation", new HashMap<>(){{
                put("faction", other.getDisplayName());
                put("faction.id", other.getUniqueIdentifier());
                put("faction.name", other.getDisplayName());
            }});
            return;
        }

        Relations.Relation previousType = relations.getRelation(other);

        relations.setRelation(other, Relations.Relation.NONE);
        other.getRelations().setRelation(faction, Relations.Relation.NONE);

        // Call relation update event
        EventUtil.call(new FactionRelationUpdateEvent(
                faction, player, other, previousType, Relations.Relation.NONE
        ));

        for(Player online : members.getOnlinePlayers()) {
            messageHandler.sendList(online, "faction-command.neutral.player-faction", new HashMap<>(){{
                put("member", player.getName());
                put("player", player.getName());
                put("relation", previousType == Relations.Relation.ALLY ? "alliance" : "truce");
                put("un-type", previousType == Relations.Relation.ALLY ? "unallied" : "untruced");
                put("faction", other.getDisplayName());
                put("faction.id", other.getUniqueIdentifier());
                put("faction.name", other.getDisplayName());
            }});
        }

        for(Player online : other.getMembers().getOnlinePlayers()) {
            messageHandler.sendList(online, "faction-command.neutral.other-faction", new HashMap<>(){{
                put("player", player.getName());
                put("relation", previousType == Relations.Relation.ALLY ? "alliance" : "truce");
                put("un-type", previousType == Relations.Relation.ALLY ? "unallied" : "untruced");
                put("faction", faction.getDisplayName());
                put("faction.id", faction.getUniqueIdentifier());
                put("faction.name", faction.getDisplayName());
            }});
        }
    }

    @Override
    protected String getName() {
        return "neutral";
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
        Player player = (Player) commandSender;
        PlayerFaction faction = factionsManager.getPlayerFactionByPlayer(player.getUniqueId());
        if(faction == null)
            return null;

        Members members = faction.getMembers();
        FactionRole playerRole = members.getFactionRole(player.getUniqueId());
        if(!playerRole.hasPermission(FactionPermission.MANAGE_RELATIONS))
            return null;

        List<String> arguments = new ArrayList<>();
        Relations relations = faction.getRelations();
        for(PlayerFaction other : relations.getAlliances()) {
            arguments.add(other.getDisplayName());
        }

        for(PlayerFaction other : relations.getTruces()) {
            arguments.add(other.getDisplayName());
        }

        return arguments;
    }

    @Override
    protected boolean requiresPlayer() {
        return true;
    }
}
