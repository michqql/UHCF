package me.michqql.uhcf.commands.faction;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Members;
import net.ricecode.similarity.JaroStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class InfoSubCommand extends SubCommand {

    private final static StringSimilarityService STRING_SIMILARITY_SERVICE =
            new StringSimilarityServiceImpl(new JaroStrategy());

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
        boolean similarityMatch = false;
        PlayerFaction faction = factionsManager.getPlayerFactionById(factionId);
        // First, if the faction is null, try to match by name and id approximations
        approx:
        if(faction == null) {
            List<PlayerFaction> matches = new ArrayList<>();
            Collection<PlayerFaction> factions = factionsManager.getPlayerFactions();
            for(PlayerFaction pf : factions) {
                if(factionId.equalsIgnoreCase(pf.getDisplayName())) {
                    faction = pf;
                    break approx;
                }

                double score = STRING_SIMILARITY_SERVICE.score(factionId, pf.getDisplayName());
                if(score >= 0.8)
                    matches.add(pf);
            }

            if(matches.size() > 1) {
                messageHandler.sendList(sender, "faction-command.info.multiple-factions",
                        Placeholder.of("list", getFactionNames(matches)));
                return;
            } else if(matches.size() == 1) {
                faction = matches.get(0);
                similarityMatch = true;
            } else {
                messageHandler.sendList(sender, "faction-command.info.could-not-find-faction", new HashMap<>(){{
                    put("faction", factionId);
                    put("faction.id", factionId);
                    put("faction.name", factionId);
                    put("faction.type", "player");
                }});
                return;
            }
        }

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

        final PlayerFaction finalFaction = faction;
        if(similarityMatch) {
            messageHandler.sendList(sender, "faction-command.info.similar-match",
                    Placeholder.of("input", factionId, "faction.name", finalFaction.getDisplayName()));
        }

        messageHandler.sendList(sender, "faction-command.info.information", new HashMap<>(){{
            put("faction", finalFaction.getDisplayName());
            put("faction.id", finalFaction.getUniqueIdentifier());
            put("faction.name", finalFaction.getDisplayName());
            put("faction.creator", creator.getName());
            put("faction.owner", leader.getName());
            put("faction.leader", leader.getName());
            put("faction.members.names", getMemberNames(members));
            put("faction.members.online", String.valueOf(members.getOnlinePlayers().size()));
            put("faction.members.size", String.valueOf(members.getSize()));
            put("faction.claims", String.valueOf(finalFaction.getClaim().getNumberOfChunks()));
        }});
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

    private String getFactionNames(List<PlayerFaction> matches) {
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < matches.size(); i++) {
            PlayerFaction faction = matches.get(i);

            builder.append(faction.getDisplayName());
            if(i < matches.size() - 1)
                builder.append(", ");
        }
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
