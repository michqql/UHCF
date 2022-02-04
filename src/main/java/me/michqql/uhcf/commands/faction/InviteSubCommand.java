package me.michqql.uhcf.commands.faction;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Members;
import me.michqql.uhcf.faction.roles.FactionPermission;
import me.michqql.uhcf.faction.roles.FactionRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class InviteSubCommand extends SubCommand {

    private final static long INVITE_COOLDOWN = TimeUnit.MINUTES.toMillis(5); // 5 minutes in ms

    private final FactionsManager factionsManager;

    public InviteSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler, FactionsManager factionsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerFaction playerFaction = factionsManager.getPlayerFactionByPlayer(player.getUniqueId());
        if(playerFaction == null) {
            messageHandler.sendList(player, "faction-command.invite.not-in-faction");
            return;
        }

        if(!playerFaction.getMembers().getFactionRole(player.getUniqueId())
                .hasPermission(FactionPermission.INVITE_MEMBERS)) {
            messageHandler.sendList(player, "faction-command.no-permission",
                    Placeholder.of("role", FactionPermission.INVITE_MEMBERS.getDefaultRole().toString()));
            return;
        }

        if(args.length == 0) {
            messageHandler.sendList(player, "command-usage",
                    Placeholder.of("command", "faction invite <player>"));
            return;
        }

        String playerName = args[0];
        Player online = Bukkit.getPlayer(playerName);
        if(online == null) {
            messageHandler.sendList(player, "faction-command.invite.player-not-online",
                    Placeholder.of("player", playerName));
            return;
        }

        if(playerFaction.getMembers().isInFaction(online.getUniqueId())) {
            messageHandler.sendList(player, "faction-command.invite.player-already-joined",
                    Placeholder.of("player", online.getName()));
            return;
        }

        // Check if distributed size is less than maximum
        if(!factionsManager.canInvitePlayer(playerFaction)) {
            messageHandler.sendList(player, "faction-command.invite.cannot-invite-size",
                    Placeholder.of("player", online.getName()));
            return;
        }

        HashMap<PlayerFaction, Long> invites = factionsManager.getPlayerInvites(online.getUniqueId());
        long inviteTimestamp = invites.getOrDefault(playerFaction, 0L);
        if(invites.containsKey(playerFaction) &&
                inviteTimestamp + INVITE_COOLDOWN >= System.currentTimeMillis()) {
            double seconds = (inviteTimestamp + INVITE_COOLDOWN - System.currentTimeMillis()) / 1000D;
            messageHandler.sendList(player, "faction-command.invite.invite-on-cooldown",
                    Placeholder.of("player", online.getName(), "time", String.valueOf(seconds)));
            return;
        }

        factionsManager.invitePlayer(online.getUniqueId(), playerFaction);

        List<Player> onlinePlayers = playerFaction.getMembers().getOnlinePlayers();
        HashMap<String, String> placeholders = Placeholder.of("player", online.getName());
        for(Player member : onlinePlayers) {
            messageHandler.sendList(member, "faction-command.invite.invited.faction", placeholders);
        }

        messageHandler.sendList(online, "faction-command.invite.invited.player", new HashMap<>(){{
            put("player", player.getName());
            put("faction", playerFaction.getDisplayName());
            put("faction.name", playerFaction.getDisplayName());
            put("faction.id", playerFaction.getUniqueIdentifier());
        }});
    }

    @Override
    protected String getName() {
        return "invite";
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
        if(!playerRole.hasPermission(FactionPermission.INVITE_MEMBERS))
            return null;

        List<String> arguments = new ArrayList<>();
        for(Player online : Bukkit.getOnlinePlayers()) {
            if(!members.isInFaction(online.getUniqueId()))
                arguments.add(online.getName());
        }

        return arguments;
    }

    @Override
    protected boolean requiresPlayer() {
        return true;
    }
}
