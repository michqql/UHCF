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
import me.michqql.uhcf.listeners.events.infoupdate.FactionMemberUpdateEvent;
import me.michqql.uhcf.util.EventUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DemoteSubCommand extends SubCommand {

    private final FactionsManager factionsManager;

    public DemoteSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler, FactionsManager factionsManager) {
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
                    Placeholder.of("command", "faction demote <player>"));
            return;
        }

        String playerName = args[0];
        Bukkit.getScheduler().runTaskAsynchronously(bukkitPlugin, () -> {
            UUID uuid = OfflineUUID.getUUID(playerName); // playerName = args[0]
            if(uuid == null) {
                messageHandler.sendList(player, "faction-command.demote.not-in-your-faction",
                        Placeholder.of("player", playerName));
                return;
            }

            String realName = OfflineUUID.getName(uuid);
            if(!members.isInFaction(uuid)) {
                messageHandler.sendList(player, "faction-command.demote.not-in-your-faction",
                        Placeholder.of("player", realName));
                return;
            }

            if(player.getUniqueId().equals(uuid)) {
                messageHandler.sendList(player, "faction-command.demote.cannot-demote-self");
                return;
            }

            FactionRole role = members.getFactionRole(uuid);
            if(role.isHigherOrEqualRanking(members.getFactionRole(player.getUniqueId()))) {
                messageHandler.sendList(player, "faction-command.demote.do-not-outrank",
                        Placeholder.of("player", realName, "role", role.toString()));
                return;
            }

            FactionRole demotion = role.getDemotion();
            if(demotion == FactionRole.NONE) {
                messageHandler.sendList(player, "faction-command.demote.cannot-demote-further",
                        Placeholder.of("player", realName, "role", role.toString()));
                return;
            }

            members.setFactionRole(uuid, demotion);

            // Call event
            EventUtil.call(new FactionMemberUpdateEvent(
                    playerFaction,
                    player,
                    uuid,
                    role,
                    demotion
            ));

            messageHandler.sendList(player, "faction-command.demote.demoted.you",
                    Placeholder.of("player", realName, "role", demotion.toString()));

            Player demoted = Bukkit.getPlayer(uuid);
            if(demoted != null) {
                messageHandler.sendList(demoted, "faction-command.demote.demoted.player",
                        Placeholder.of("player", player.getName(), "role", demotion.toString()));
            }
        });
    }

    @Override
    protected String getName() {
        return "demote";
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
