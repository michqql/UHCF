package me.michqql.uhcf.commands.faction;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class JoinSubCommand extends SubCommand {

    private final FactionsManager factionsManager;

    public JoinSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler, FactionsManager factionsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if(args.length == 0) {
            messageHandler.sendList(sender, "command-usage",
                    Placeholder.of("command", "faction join <faction>"));
            return;
        }

        Player player = (Player) sender;
        PlayerFaction current = factionsManager.getPlayerFactionByPlayer(player.getUniqueId());
        if(current != null) {
            messageHandler.sendList(player, "faction-command.join.already-in-faction");
            return;
        }

        HashMap<PlayerFaction, Long> invites = factionsManager.getPlayerInvites(player.getUniqueId());

        String factionName = args[0];
        PlayerFaction playerFaction = factionsManager.getPlayerFactionById(factionName);
        if(playerFaction == null) {
            messageHandler.sendList(player, "faction-command.join.faction-doesnt-exist", new HashMap<>(){{
                put("faction", factionName);
                put("faction.name", factionName);
            }});
            return;
        }

        if(!invites.containsKey(playerFaction)) {
            messageHandler.sendList(player, "faction-command.join.not-invited", new HashMap<>(){{
                put("faction", playerFaction.getDisplayName());
                put("faction.name", playerFaction.getDisplayName());
                put("faction.id", playerFaction.getUniqueIdentifier());
            }});
            return;
        }

        for(Player member : playerFaction.getMembers().getOnlinePlayers()) {
            messageHandler.sendList(member, "faction-command.join.joined.faction",
                    Placeholder.of("player", player.getName()));
        }

        playerFaction.getMembers().addMember(player.getUniqueId());
        factionsManager.setPlayerFaction(player.getUniqueId(), playerFaction);

        messageHandler.sendList(player, "faction-command.join.joined.player", new HashMap<>(){{
            put("faction", playerFaction.getDisplayName());
            put("faction.name", playerFaction.getDisplayName());
            put("faction.id", playerFaction.getUniqueIdentifier());
        }});
    }

    @Override
    protected String getName() {
        return "join";
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
        HashMap<PlayerFaction, Long> invites = factionsManager.getPlayerInvites(player.getUniqueId());
        if(invites == null || invites.isEmpty())
            return null;

        List<String> arguments = new ArrayList<>();
        for(PlayerFaction playerFaction : invites.keySet()) {
            arguments.add(playerFaction.getDisplayName());
        }

        return arguments;
    }

    @Override
    protected boolean requiresPlayer() {
        return true;
    }
}
