package me.michqql.uhcf.commands.admin;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Pair;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Warpoints;
import me.michqql.uhcf.raiding.Raid;
import me.michqql.uhcf.raiding.RaidManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WarpointSubCommand extends SubCommand {

    private final FactionsManager factionsManager;
    private final RaidManager raidManager;

    public WarpointSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler,
                              FactionsManager factionsManager, RaidManager raidManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
        this.raidManager = raidManager;
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if(args.length == 0) {
            messageHandler.sendList(sender, "command-usage",
                    Placeholder.of("command", "admin warpoint <set|view> (...)"));
            return;
        }

        String subCommand = args[0];
        if("view".equalsIgnoreCase(subCommand)) {
            if(args.length < 2) {
                messageHandler.sendList(sender, "command-usage",
                        Placeholder.of("command", "admin warpoint view <faction>"));
                return;
            }

            String factionId = args[1];
            PlayerFaction faction = factionsManager.getPlayerFactionById(factionId);
            if(faction == null) {
                messageHandler.sendList(sender, "admin-command.could-not-find-faction", new HashMap<>(){{
                    put("name", factionId);
                    put("id", factionId);
                    put("faction", factionId);
                    put("faction.name", factionId);
                    put("faction.id", factionId);
                }});
                return;
            }

            messageHandler.sendMessage(sender, "admin-command.warpoint.view.title", new HashMap<>(){{
                put("faction", faction.getDisplayName());
                put("faction.name", faction.getDisplayName());
                put("faction.id", faction.getUniqueIdentifier());
            }});

            Warpoints warpoints = faction.getWarpoints();
            AtomicInteger count = new AtomicInteger(1);
            for (Pair<PlayerFaction, Integer> pair : warpoints.getOrderedWarpoints(false)) {
                int warpoint = pair.getValue();
                messageHandler.sendMessage(sender, "admin-command.warpoint.view.line", new HashMap<>(){{
                    put("count", String.valueOf(count.getAndIncrement()));
                    put("positive.colour", (warpoint > 0 ? "&a" : (warpoint < 0 ? "&c" : "&f")));
                    put("positive.color", (warpoint > 0 ? "&a" : (warpoint < 0 ? "&c" : "&f")));
                    put("positive.sign", (warpoint > 0 ? "+" : ""));
                    put("faction", pair.getKey().getDisplayName());
                    put("faction.name", pair.getKey().getDisplayName());
                    put("faction.id", pair.getKey().getUniqueIdentifier());
                    put("warpoints", String.valueOf(warpoint));
                }});
            }
        }
        else if("set".equalsIgnoreCase(subCommand)) {
            if(args.length < 4) {
                messageHandler.sendList(sender, "command-usage",
                        Placeholder.of("command", "admin warpoint set <faction> <other> <amount>"));
                return;
            }

            String factionId = args[1];
            PlayerFaction faction = factionsManager.getPlayerFactionById(factionId);
            if(faction == null) {
                messageHandler.sendList(sender, "admin-command.could-not-find-faction", new HashMap<>() {{
                    put("name", factionId);
                    put("id", factionId);
                    put("faction", factionId);
                    put("faction.name", factionId);
                    put("faction.id", factionId);
                }});
                return;
            }

            String otherId = args[2];
            PlayerFaction other = factionsManager.getPlayerFactionById(otherId);
            if(other == null) {
                messageHandler.sendList(sender, "admin-command.could-not-find-faction", new HashMap<>() {{
                    put("name", otherId);
                    put("id", otherId);
                    put("faction", otherId);
                    put("faction.name", otherId);
                    put("faction.id", otherId);
                }});
                return;
            }

            String amount = args[3];
            int warpoints;
            try {
                warpoints = Integer.parseInt(amount);
            } catch (NumberFormatException e) {
                messageHandler.sendList(sender, "admin-command.warpoint.set.not-a-number",
                        Placeholder.of("value", amount));
                return;
            }

            Raid raid = raidManager.getRaidByFactions(faction, other);
            if(raid != null) {
                messageHandler.sendList(sender, "admin-command.warpoint.set.in-raid", new HashMap<>(){{
                    put("faction", faction.getDisplayName());
                    put("faction.name", faction.getDisplayName());
                    put("faction.id", faction.getUniqueIdentifier());
                    put("other.faction", other.getDisplayName());
                    put("other.faction.name", other.getDisplayName());
                    put("other.faction.id", other.getUniqueIdentifier());
                    put("time", getTimeRemaining(raid.getTimeRemaining()));
                }});
                return;
            }

            int max = raidManager.getWarpointThreshold();
            if(Math.abs(warpoints) >= max) {
                messageHandler.sendList(sender, "admin-command.warpoint.set.warpoint-bound", new HashMap<>(){{
                    put("min", String.valueOf(-max + 1));
                    put("max", String.valueOf(max - 1));
                }});
                return;
            }

            faction.getWarpoints().setWarpoints(other, warpoints);
            other.getWarpoints().setWarpoints(faction, -warpoints);

            messageHandler.sendList(sender, "admin-command.warpoint.set.set", new HashMap<>(){{
                put("faction", faction.getDisplayName());
                put("faction.name", faction.getDisplayName());
                put("faction.id", faction.getUniqueIdentifier());
                put("other.faction", other.getDisplayName());
                put("other.faction.name", other.getDisplayName());
                put("other.faction.id", other.getUniqueIdentifier());
                put("positive.colour", (warpoints > 0 ? "&a" : (warpoints < 0 ? "&c" : "&f")));
                put("positive.color", (warpoints > 0 ? "&a" : (warpoints < 0 ? "&c" : "&f")));
                put("positive.sign", (warpoints > 0 ? "+" : (warpoints < 0 ? "-" : "")));
                put("warpoints", String.valueOf(warpoints));
            }});

            if(raidManager.isAutomaticRaiding() && Math.abs(warpoints) >= raidManager.getWarpointThreshold()) {
                if(warpoints > 0) {
                    raidManager.startRaid(faction, other);
                } else {
                    raidManager.startRaid(other, faction);
                }

                messageHandler.sendList(sender, "admin-command.warpoint.set.started-raid", new HashMap<>(){{
                    put("faction", faction.getDisplayName());
                    put("faction.name", faction.getDisplayName());
                    put("faction.id", faction.getUniqueIdentifier());
                    put("other.faction", other.getDisplayName());
                    put("other.faction.name", other.getDisplayName());
                    put("other.faction.id", other.getUniqueIdentifier());
                }});
            }
        }
        else {
            messageHandler.sendList(sender, "invalid-command",
                    Placeholder.of("command", "admin"));
        }
    }

    @Override
    protected String getName() {
        return "warpoint";
    }

    @Override
    protected List<String> getAliases() {
        return List.of("wp");
    }

    @Override
    protected String getPermission() {
        return UHCFPlugin.ADMIN_PERMISSION;
    }

    @Override
    protected List<String> getArguments(CommandSender commandSender) {
        return Arrays.asList("set", "view");
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
