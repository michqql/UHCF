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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class KickSubCommand extends SubCommand {

    private final FactionsManager factionsManager;

    public KickSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler, FactionsManager factionsManager) {
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
                .hasPermission(FactionPermission.MANAGE_MEMBERS)) {
            messageHandler.sendList(player, "faction-command.no-permission",
                    Placeholder.of("role", FactionPermission.MANAGE_MEMBERS.getDefaultRole().toString()));
            return;
        }

        if(args.length == 0) {
            messageHandler.sendList(player, "command-usage",
                    Placeholder.of("command", "faction kick <player>"));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(bukkitPlugin, () -> {
            UUID uuid = OfflineUUID.getUUID(args[0]); // playerName = args[0]
            if(uuid == null) {
                return;
            }

            String playerName = OfflineUUID.getName(uuid);
            if(!members.isInFaction(uuid)) {
                messageHandler.sendList(player, "faction-command.kick.not-in-your-faction",
                        Placeholder.of("player", playerName));
                return;
            }

            if(player.getUniqueId().equals(uuid)) {
                messageHandler.sendList(player, "faction-command.kick.cannot-kick-self");
                return;
            }

            FactionRole role = members.getFactionRole(uuid);
            if(role.isHigherOrEqualRanking(members.getFactionRole(player.getUniqueId()))) {
                messageHandler.sendList(player, "faction-command.kick.do-not-outrank",
                        Placeholder.of("player", playerName, "role", role.toString()));
                return;
            }

            FactionRole previousRole = members.removeMember(uuid);
            factionsManager.setPlayerFaction(uuid, null);

            // Call member update event
            EventUtil.call(new FactionMemberUpdateEvent(
                    playerFaction,
                    player,
                    uuid,
                    previousRole,
                    FactionRole.NONE
            ));

            for(Player online : members.getOnlinePlayers()) {
                messageHandler.sendList(online, "faction-command.kick.kicked.faction",
                        Placeholder.of("player", playerName, "member", player.getName()));
            }

            Player kicked = Bukkit.getPlayer(uuid);
            if(kicked != null) {
                messageHandler.sendList(kicked, "faction-command.kick.kicked.player", new HashMap<>(){{
                    put("member", player.getName());
                    put("faction", playerFaction.getDisplayName());
                    put("faction.name", playerFaction.getDisplayName());
                    put("faction.id", playerFaction.getUniqueIdentifier());
                }});
            }
        });
    }

    @Override
    protected String getName() {
        return "kick";
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
        if(!playerRole.hasPermission(FactionPermission.MANAGE_MEMBERS))
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
