package me.michqql.uhcf.commands.faction;

import me.michqql.core.command.SubCommand;
import me.michqql.core.gui.GuiHandler;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.OfflineUUID;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Members;
import me.michqql.uhcf.gui.faction.ViewFactionInfoGui;
import me.michqql.uhcf.player.PlayerData;
import me.michqql.uhcf.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class WhoSubCommand extends SubCommand {

    private final GuiHandler guiHandler;
    private final FactionsManager factionsManager;
    private final PlayerManager playerManager;

    public WhoSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler, GuiHandler guiHandler,
                         FactionsManager factionsManager, PlayerManager playerManager) {
        super(bukkitPlugin, messageHandler);
        this.guiHandler = guiHandler;
        this.factionsManager = factionsManager;
        this.playerManager = playerManager;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if(args.length == 0) {
            messageHandler.sendList(sender, "command-usage",
                    Placeholder.of("command", "faction who <player>"));
            return;
        }

        String playerName = args[0];
        Bukkit.getScheduler().runTaskAsynchronously(bukkitPlugin, () -> {
            UUID uuid = OfflineUUID.getUUID(playerName); // playerName = args[0]
            if(uuid == null) {
                messageHandler.sendList(sender, "faction-command.info.could-not-find-player",
                        Placeholder.of("player", playerName));
                return;
            }

            String realName = OfflineUUID.getName(uuid);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if(!offlinePlayer.isOnline() && !offlinePlayer.hasPlayedBefore()) {
                messageHandler.sendList(sender, "faction-command.info.could-not-find-player",
                        Placeholder.of("player", realName));
                return;
            }

            PlayerFaction faction = factionsManager.getPlayerFactionByPlayer(uuid);
            if(faction == null) {
                messageHandler.sendList(sender, "faction-command.info.no-faction", new HashMap<>(){{
                    put("player", offlinePlayer.getName());
                }});
                return;
            }

            // Check players setting
            if(sender instanceof Player player) {
                PlayerData data = playerManager.get(player.getUniqueId());
                if((boolean) data.getSetting("gui", true)) {
                    new ViewFactionInfoGui(guiHandler, player, factionsManager, faction).openGui();
                    return;
                }
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
        });
    }

    private String getMemberNames(Members members) {
        StringBuilder builder = new StringBuilder();
        builder.append("&e[");

        List<OfflinePlayer> players = members.getPlayers();
        for(int i = 0; i < players.size(); i++) {
            OfflinePlayer player = players.get(i);

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
        return "who";
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
        List<String> arguments = new ArrayList<>();
        for(Player online : Bukkit.getOnlinePlayers()) {
            arguments.add(online.getName());
        }

        return arguments;
    }

    @Override
    protected boolean requiresPlayer() {
        return false;
    }
}
