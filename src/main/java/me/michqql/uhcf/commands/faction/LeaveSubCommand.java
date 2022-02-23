package me.michqql.uhcf.commands.faction;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Members;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LeaveSubCommand extends SubCommand {

    private final FactionsManager factionsManager;

    public LeaveSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler, FactionsManager factionsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
    }

    @Override
    protected void registerSubCommands() {

    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        // Get faction
        PlayerFaction faction = factionsManager.getPlayerFactionByPlayer(uuid);

        // Check if player is in faction
        if(faction == null) {
            messageHandler.sendList(player, "faction-command.leave.not-in-faction");
            return;
        }

        // Check if player owns faction
        // (if so, they must promote another player first)
        Members members = faction.getMembers();
        if(members.isLeader(uuid)) {
            messageHandler.sendList(player, "faction-command.leave.player-is-leader");
            return;
        }

        // Leave faction
        factionsManager.setPlayerFaction(uuid, null);
        members.removeMember(uuid);

        // Send message to player
        messageHandler.sendList(player, "faction-command.leave.left-player",
                Placeholder.of("name", faction.getDisplayName()));

        // Send message to online members
        List<Player> online = members.getOnlinePlayers();
        HashMap<String, String> placeholders = Placeholder.of("player", player.getName());
        online.forEach(member -> messageHandler.sendList(member, "faction-command.leave.left-member", placeholders));
    }

    @Override
    protected String getName() {
        return "leave";
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
        return null;
    }

    @Override
    protected boolean requiresPlayer() {
        return true;
    }
}
