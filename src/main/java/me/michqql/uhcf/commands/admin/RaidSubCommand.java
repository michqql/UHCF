package me.michqql.uhcf.commands.admin;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.raiding.Raid;
import me.michqql.uhcf.raiding.RaidManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
        if(args.length < 3) {
            messageHandler.sendList(sender, "command-usage",
                    Placeholder.of("command", "admin raid <start|stop> <raider faction> <defender faction>"));
            return;
        }

        boolean stopRaid = "stop".equalsIgnoreCase(args[0]);

        String factionId = args[1];
        PlayerFaction raiders = factionsManager.getPlayerFactionById(factionId);
        if(raiders == null) {
            messageHandler.sendList(sender, "admin-command.could-not-find-faction", new HashMap<>(){{
                put("name", factionId);
                put("id", factionId);
                put("faction", factionId);
                put("faction.name", factionId);
                put("faction.id", factionId);
            }});
            return;
        }

        String otherId = args[2];
        PlayerFaction defenders = factionsManager.getPlayerFactionById(otherId);
        if(defenders == null) {
            messageHandler.sendList(sender, "admin-command.could-not-find-faction", new HashMap<>(){{
                put("name", otherId);
                put("id", otherId);
                put("faction", otherId);
                put("faction.name", otherId);
                put("faction.id", otherId);
            }});
            return;
        }

        Raid raid = raidManager.getRaidByFactions(raiders, defenders);
        if(stopRaid) {
            if(raid == null) {
                messageHandler.sendList(sender, "admin-command.raid.stop.not-in-raid", new HashMap<>(){{
                    put("faction", raiders.getDisplayName());
                    put("faction.name", raiders.getDisplayName());
                    put("faction.id", raiders.getUniqueIdentifier());
                    put("other.faction", defenders.getDisplayName());
                    put("other.faction.name", defenders.getDisplayName());
                    put("other.faction.id", defenders.getUniqueIdentifier());
                }});
                return;
            }

            raidManager.stopRaid(raid);
            messageHandler.sendList(sender, "admin-command.raid.stop.stopped", new HashMap<>(){{
                put("faction", raiders.getDisplayName());
                put("faction.name", raiders.getDisplayName());
                put("faction.id", raiders.getUniqueIdentifier());
                put("other.faction", defenders.getDisplayName());
                put("other.faction.name", defenders.getDisplayName());
                put("other.faction.id", defenders.getUniqueIdentifier());
                put("time", getTimeRemaining(raid.getTimeRemaining()));
            }});
        }
        else {
            if(raid != null) {
                messageHandler.sendList(sender, "admin-command.raid.start.in-raid", new HashMap<>(){{
                    put("faction", raiders.getDisplayName());
                    put("faction.name", raiders.getDisplayName());
                    put("faction.id", raiders.getUniqueIdentifier());
                    put("other.faction", defenders.getDisplayName());
                    put("other.faction.name", defenders.getDisplayName());
                    put("other.faction.id", defenders.getUniqueIdentifier());
                    put("time", getTimeRemaining(raid.getTimeRemaining()));
                }});
                return;
            }

            int warpoints = raidManager.getWarpointThreshold();

            raiders.getWarpoints().setWarpoints(defenders, warpoints);
            defenders.getWarpoints().setWarpoints(raiders, -warpoints);

            boolean started = raidManager.startRaid(raiders, defenders);
            if(started) {
                messageHandler.sendList(sender, "admin-command.raid.start.started", new HashMap<>() {{
                    put("faction", raiders.getDisplayName());
                    put("faction.name", raiders.getDisplayName());
                    put("faction.id", raiders.getUniqueIdentifier());
                    put("other.faction", defenders.getDisplayName());
                    put("other.faction.name", defenders.getDisplayName());
                    put("other.faction.id", defenders.getUniqueIdentifier());
                }});
            } else {
                messageHandler.sendList(sender, "admin-command.raid.start.unexpected-error");
            }
        }
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
        return UHCFPlugin.ADMIN_PERMISSION;
    }

    @Override
    protected List<String> getArguments(CommandSender sender) {
        Set<PlayerFaction> factions = raidManager.getFactionsInRaid();
        List<String> result = new ArrayList<>();
        for(PlayerFaction faction : factions)
            result.add(faction.getDisplayName());

        return result;
    }

    @Override
    protected boolean requiresPlayer() {
        return false;
    }

    private String getTimeRemaining(long timeRemaining) {
        long minutes = (timeRemaining / 1000) / 60;
        long seconds = (timeRemaining / 1000) % 60;

        return minutes + " minutes and " + seconds + " seconds";
    }
}
