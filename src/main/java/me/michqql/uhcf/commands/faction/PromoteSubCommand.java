package me.michqql.uhcf.commands.faction;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.OfflineUUID;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PromoteSubCommand extends SubCommand {

    private final FactionsManager factionsManager;

    public PromoteSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler, FactionsManager factionsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerFaction playerFaction = factionsManager.getPlayerFactionByPlayer(player.getUniqueId());
        if(playerFaction == null) {
            messageHandler.sendList(player, "faction-command.not-in-faction");
            return;
        }

        Members members = playerFaction.getMembers();
        if(!members.getFactionRole(player.getUniqueId())
                .hasPermission(FactionPermission.MANAGE_ROLES)) {
            messageHandler.sendList(player, "faction-command.no-permission",
                    Placeholder.of("role", FactionPermission.MANAGE_ROLES.getDefaultRole().toString()));
            return;
        }

        if(args.length == 0) {
            messageHandler.sendList(player, "command-usage",
                    Placeholder.of("command", "faction promote <player>"));
            return;
        }

        String playerName = args[0];
        Bukkit.getScheduler().runTaskAsynchronously(bukkitPlugin, () -> {
            UUID uuid = OfflineUUID.getUUID(playerName);
            if(uuid == null) {
                messageHandler.sendList(player, "faction-command.promote.not-in-your-faction",
                        Placeholder.of("player", playerName));
                return;
            }

            String realName = OfflineUUID.getName(uuid);
            if(!members.isInFaction(uuid)) {
                messageHandler.sendList(player, "faction-command.promote.not-in-your-faction",
                        Placeholder.of("player", realName));
                return;
            }

            if(player.getUniqueId().equals(uuid)) {
                messageHandler.sendList(player, "faction-command.promote.cannot-promote-self");
                return;
            }

            FactionRole role = members.getFactionRole(uuid);
            FactionRole previous = members.getFactionRole(player.getUniqueId());
            if(role.isHigherOrEqualRanking(previous)) {
                messageHandler.sendList(player, "faction-command.promote.do-not-outrank",
                        Placeholder.of("player", realName, "role", role.toString()));
                return;
            }

            FactionRole promotion = role.getPromotion();
            members.setFactionRole(uuid, promotion);

            messageHandler.sendList(player, "faction-command.promote.promoted.you",
                    Placeholder.of("player", realName, "role", promotion.toString()));

            FactionRole current = members.getFactionRole(player.getUniqueId());
            if(previous != current && previous == FactionRole.LEADER) {
                messageHandler.sendList(player, "faction-command.promote.promoted.demoted-from-leader",
                        Placeholder.of("role.leader", FactionRole.LEADER.toString(),
                                "role", current.toString()));
            }

            Player promoted = Bukkit.getPlayer(uuid);
            if(promoted != null) {
                messageHandler.sendList(promoted, "faction-command.promote.promoted.player",
                        Placeholder.of("player", player.getName(), "role", promotion.toString()));
            }
        });
    }

    @Override
    protected String getName() {
        return "promote";
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
        if(!playerRole.hasPermission(FactionPermission.MANAGE_ROLES))
            return null;

        List<String> arguments = new ArrayList<>();
        for(UUID uuid : members.getMembers()) {
            FactionRole role = members.getFactionRole(uuid);
            if(role.isHigherOrEqualRanking(playerRole))
                continue;

            OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);
            if(member.hasPlayedBefore())
                arguments.add(member.getName());
        }

        return arguments;
    }

    @Override
    protected boolean requiresPlayer() {
        return true;
    }
}
