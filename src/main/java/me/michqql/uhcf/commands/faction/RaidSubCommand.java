package me.michqql.uhcf.commands.faction;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Pair;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Members;
import me.michqql.uhcf.faction.attributes.Relations;
import me.michqql.uhcf.faction.roles.FactionPermission;
import me.michqql.uhcf.raiding.RaidList;
import me.michqql.uhcf.raiding.RaidManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RaidSubCommand extends SubCommand {

    private final FactionsManager factionsManager;
    private final RaidManager raidManager;

    public RaidSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler,
                          FactionsManager factionsManager, RaidManager raidManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
        this.raidManager = raidManager;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerFaction faction = factionsManager.getPlayerFactionByPlayer(player.getUniqueId());
        if(faction == null) {
            messageHandler.sendList(player, "faction-command.not-in-faction");
            return;
        }

        Members members = faction.getMembers();
        if(!members.getFactionRole(player.getUniqueId())
                .hasPermission(FactionPermission.START_RAIDS)) {
            messageHandler.sendList(player, "faction-command.no-permission",
                    Placeholder.of("role", FactionPermission.START_RAIDS.getDefaultRole().toString()));
            return;
        }

        String factionId = args[0];
        PlayerFaction other = factionsManager.getPlayerFactionById(factionId);
        if(other == null) {
            messageHandler.sendList(player, "faction-command.raid.could-not-find-faction", new HashMap<>(){{
                put("faction", factionId);
                put("faction.id", factionId);
                put("faction.name", factionId);
            }});
            return;
        }

        Relations relations = faction.getRelations();
        if(relations.isFriendly(other)) {
            messageHandler.sendList(player, "faction-command.raid.faction-friendly", new HashMap<>(){{
                put("faction", other.getDisplayName());
                put("faction.id", other.getUniqueIdentifier());
                put("faction.name", other.getDisplayName());
            }});
            return;
        }

        int warpoints = faction.getWarpoints().getWarpoints(other);
        if(warpoints < raidManager.getWarpointThreshold()) {
            messageHandler.sendList(player, "faction-command.raid.not-enough-warpoints", new HashMap<>(){{
                put("faction", other.getDisplayName());
                put("faction.id", other.getUniqueIdentifier());
                put("faction.name", other.getDisplayName());
                put("warpoints", String.valueOf(warpoints));
                put("required", String.valueOf(raidManager.getWarpointThreshold()));
            }});
            return;
        }

        RaidList raids = raidManager.getRaidsByFaction(faction);
        if(raids.isInvolved(other)) {
            messageHandler.sendList(player, "faction-command.raid.already-raiding", new HashMap<>(){{
                put("faction", other.getDisplayName());
                put("faction.id", other.getUniqueIdentifier());
                put("faction.name", other.getDisplayName());
            }});
            return;
        }

        raidManager.startRaid(faction, other);

        // Send messages to all online faction members
        // Attackers
        HashMap<String, String> placeholdersAttackers = new HashMap<>(){{
            put("faction", other.getDisplayName());
            put("faction.name", other.getDisplayName());
        }};
        faction.getMembers().getOnlinePlayers().forEach(online ->
                messageHandler.sendList(online, "raid.started.raiders", placeholdersAttackers));

        // Defenders
        HashMap<String, String> placeholdersDefenders = new HashMap<>(){{
            put("faction", faction.getDisplayName());
            put("faction.name", faction.getDisplayName());
        }};
        other.getMembers().getOnlinePlayers().forEach(online ->
                messageHandler.sendList(online, "raid.started.defenders", placeholdersDefenders));
    }

    @Override
    protected String getName() {
        return "raid";
    }

    @Override
    protected List<String> getAliases() {
        return null;
    }

    @Override
    protected String getPermission() {
        return null;
    }

    @Override
    protected List<String> getArguments(CommandSender sender) {
        Player player = (Player) sender;
        PlayerFaction faction = factionsManager.getPlayerFactionByPlayer(player.getUniqueId());
        if(faction == null)
            return null;

        List<String> result = new ArrayList<>();
        for(Pair<PlayerFaction, Integer> pair : faction.getWarpoints().getOrderedWarpoints(false)) {
            if(pair.getValue() >= raidManager.getWarpointThreshold())
                result.add(pair.getKey().getDisplayName());
        }
        return result;
    }

    @Override
    protected boolean requiresPlayer() {
        return true;
    }
}
