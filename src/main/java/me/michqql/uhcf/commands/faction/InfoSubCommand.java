package me.michqql.uhcf.commands.faction;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Members;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class InfoSubCommand extends SubCommand {

    private final FactionsManager factionsManager;

    public InfoSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler, FactionsManager factionsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if(args.length == 0) {
            messageHandler.sendList(sender, "command-usage",
                    Placeholder.of("command", "faction info <faction>"));
            return;
        }

        String factionId = args[0];
        PlayerFaction faction = factionsManager.getPlayerFactionById(factionId);
        if(faction == null) {
            messageHandler.sendList(sender, "faction-command.info.could-not-find-faction", new HashMap<>(){{
                put("faction", factionId);
                put("faction.id", factionId);
                put("faction.name", factionId);
                put("faction.type", "player");
            }});
            return;
        }

        Members members = faction.getMembers();
        OfflinePlayer creator = Bukkit.getOfflinePlayer(faction.getCreator());
        OfflinePlayer leader = Bukkit.getOfflinePlayer(members.getLeader());
        messageHandler.sendList(sender, "faction-command.info.information", new HashMap<>(){{
            put("faction", faction.getDisplayName());
            put("faction.id", faction.getUniqueIdentifier());
            put("faction.name", faction.getDisplayName());
            put("faction.creator", creator.getName());
            put("faction.owner", leader.getName());
            put("faction.leader", leader.getName());
            put("faction.members.names", getMemberNames(members));
            put("faction.members.online", String.valueOf(members.getOnlinePlayers().size()));
            put("faction.members.size", String.valueOf(members.getSize()));
            put("faction.claims", String.valueOf(faction.getClaim().getNumberOfChunks()));
        }});
    }

    private String getMemberNames(Members members) {
        StringBuilder builder = new StringBuilder();
        builder.append("&e[");

        List<OfflinePlayer> players = members.getPlayers();
        for(int i = 0; i < players.size(); i++) {
            OfflinePlayer player = players.get(i);
            if(!player.hasPlayedBefore())
                continue;

            if(player.isOnline())
                builder.append("&a");
            else
                builder.append("&c");

            builder.append(player.getName());
            builder.append("&e");

            if(i < players.size() - 1)
                builder.append(", ");
        }

        builder.append("&e]");
        return builder.toString();
    }

    @Override
    protected String getName() {
        return "info";
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
        Collection<PlayerFaction> factions = factionsManager.getPlayerFactions();
        if(factions.isEmpty())
            return null;

        List<String> arguments = new ArrayList<>();
        for(PlayerFaction faction : factions) {
            arguments.add(faction.getDisplayName());
        }

        return arguments;
    }

    @Override
    protected boolean requiresPlayer() {
        return false;
    }
}
