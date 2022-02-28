package me.michqql.uhcf.util.scoreboard;

import dev.jcsoftware.jscoreboards.JPerPlayerScoreboard;
import me.michqql.core.io.CommentFile;

import me.michqql.core.util.AbstractListener;
import me.michqql.core.util.MessageHandler;
import me.michqql.uhcf.UHCFPlugin;

import me.michqql.uhcf.claim.Claim;
import me.michqql.uhcf.claim.ClaimsManager;
import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.Faction;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Relations;
import me.michqql.uhcf.faction.roles.FactionRole;
import me.michqql.uhcf.player.PlayerData;
import me.michqql.uhcf.player.PlayerManager;
import me.michqql.uhcf.raiding.Raid;
import me.michqql.uhcf.raiding.RaidList;
import me.michqql.uhcf.raiding.RaidManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScoreboardHandler extends AbstractListener {

    private final UHCFPlugin plugin;
    private final FactionsManager factionsManager;
    private final ClaimsManager claimsManager;
    private final PlayerManager playerManager;
    private final RaidManager raidManager;

    // Config
    private String wildernessName;
    private String borderlandsName;
    private long period;
    private boolean dynamic = false;
    private int counter = 0;
    private List<String> titleLines;
    private String factionTitle;
    private String factionText;
    private String adminFactionTitle;
    private String adminFactionText;
    private String territoryTitle;
    private String territoryText;
    private String balanceTitle;
    private String balanceText;
    private String statsTitle;
    private String statsText;
    private String raidTitle;
    private String raidText;
    private String footer;

    private JPerPlayerScoreboard scoreboard;

    public ScoreboardHandler(UHCFPlugin plugin, CommentFile config,
                             FactionsManager factionsManager, ClaimsManager claimsManager,
                             PlayerManager playerManager, RaidManager raidManager) {
        super(plugin);
        this.plugin = plugin;
        this.factionsManager = factionsManager;
        this.claimsManager = claimsManager;
        this.playerManager = playerManager;
        this.raidManager = raidManager;

        ScoreboardManager sbManager = Bukkit.getScoreboardManager();
        if(sbManager == null) {
            Bukkit.getLogger().severe("[UHCF] Could not create scoreboard (loaded before worlds)");
            return;
        }

        // Load config
        FileConfiguration mainF = config.getConfig();
        this.wildernessName = mainF.getString("wilderness-name");
        this.borderlandsName = mainF.getString("borderlands-name");

        CommentFile layoutFile = new CommentFile(plugin, "", "scoreboard_layout");
        FileConfiguration f = layoutFile.getConfig();

        String type = f.getString("title.type");
        this.period = f.getLong("title.dynamic.interval", 2L);

        // Body
        this.factionTitle = f.getString("body.faction.title");
        this.factionText = f.getString("body.faction.text");
        this.adminFactionTitle = f.getString("body.admin-faction.title");
        this.adminFactionText = f.getString("body.admin-faction.text");
        this.territoryTitle = f.getString("body.territory.title");
        this.territoryText = f.getString("body.territory.text");
        this.balanceTitle = f.getString("body.balance.title");
        this.balanceText = f.getString("body.balance.text");
        this.statsTitle = f.getString("body.stats.title");
        this.statsText = f.getString("body.stats.text");
        this.raidTitle = f.getString("body.raid.title");
        this.raidText = f.getString("body.raid.text");
        this.footer = f.getString("body.footer");

        // Title
        if("dynamic".equalsIgnoreCase(type)) {
            this.dynamic = true;
            this.titleLines = f.getStringList("title.dynamic.lines");
        } else {
            String line = f.getString("title.static", "");
            titleLines = new ArrayList<>(List.of(line));
        }

        setupScoreboard();
        run();
    }

    private void setupScoreboard() {
        this.scoreboard = new JPerPlayerScoreboard(
                // Title
                player -> {
                    if(dynamic) {
                        int index = counter % titleLines.size();
                        return titleLines.get(index);
                    } else {
                        return titleLines.get(0);
                    }
                },
                // Body
                player -> {
                    List<String> body = new ArrayList<>();
                    HashMap<String, String> placeholders;

                    // Faction
                    body.add(" ");
                    body.add(factionTitle);
                    PlayerFaction faction = factionsManager.getPlayerFactionByPlayer(player.getUniqueId());
                    String relColour = (faction == null) ? FactionRole.NONE.getPrefix() : FactionRole.MEMBER.getPrefix();
                    String facName = (faction == null) ? "None" : faction.getDisplayName();
                    placeholders = new HashMap<>(){{
                        put("relation.colour", relColour);
                        put("relation.color", relColour);
                        put("relation.col", relColour);
                        put("rel.colour", relColour);
                        put("rel.color", relColour);
                        put("rel.col", relColour);
                        put("faction", facName);
                        put("faction.name", facName);
                    }};
                    body.add(MessageHandler.replacePlaceholdersStatic(factionText, placeholders));

                    // Admin faction
                    Faction tempFaction = factionsManager.getTemporaryFactionByPlayer(player.getUniqueId());
                    if(tempFaction != null) {
                        body.add("");
                        body.add(adminFactionTitle);

                        String type = tempFaction instanceof AdminFaction ? "admin" : "player";
                        String typeColour = tempFaction instanceof AdminFaction ? "&c" : "&e";
                        placeholders = new HashMap<>(){{
                            put("relation.colour", typeColour);
                            put("relation.color", typeColour);
                            put("relation.col", typeColour);
                            put("rel.colour", typeColour);
                            put("rel.color", typeColour);
                            put("rel.col", typeColour);
                            put("faction.type.colour", typeColour);
                            put("faction.type.color", typeColour);
                            put("faction.type.col", typeColour);
                            put("faction.type", type);
                            put("faction", tempFaction.getDisplayName());
                            put("faction.name", tempFaction.getDisplayName());
                            put("faction.id", tempFaction.getUniqueIdentifier());
                        }};
                        body.add(MessageHandler.replacePlaceholdersStatic(adminFactionText, placeholders));
                    }

                    // Territory
                    body.add("  ");
                    body.add(territoryTitle);
                    Chunk chunk = player.getLocation().getChunk();
                    Claim claim = claimsManager.getClaimByChunk(chunk);
                    String territoryColour = (claim == null) ? FactionRole.NONE.getPrefix() :
                            Relations.getRelation(player, faction, claim.getOwningFaction()).getPrefix();
                    String territoryName = (claim == null ?
                            (claimsManager.isInBorderlands(chunk) ? borderlandsName : wildernessName) :
                            claim.getOwningFaction().getDisplayName());
                    placeholders = new HashMap<>(){{
                        put("relation.colour", territoryColour);
                        put("relation.color", territoryColour);
                        put("relation.col", territoryColour);
                        put("rel.colour", territoryColour);
                        put("rel.color", territoryColour);
                        put("rel.col", territoryColour);
                        put("faction", territoryName);
                        put("faction.name", territoryName);
                    }};
                    body.add(MessageHandler.replacePlaceholdersStatic(territoryText, placeholders));

                    // Balance
                    body.add("   ");
                    body.add(balanceTitle);
                    placeholders = new HashMap<>(){{
                        put("balance", "");
                    }};
                    body.add(MessageHandler.replacePlaceholdersStatic(balanceText, placeholders));

                    // Stats
                    RaidList raids = raidManager.getRaidsByFaction(faction);
                    raid: if(raids.getRaids() > 0) {
                        int index = (counter / 100) % raids.raids().size();
                        Raid raid = raids.raids().get(index);
                        PlayerFaction other = raid.getOther(faction);
                        if(other == null)
                            break raid;

                        long timeRemaining = raid.getTimeRemaining();
                        long minutes = (timeRemaining / 1000) / 60;
                        long seconds = (timeRemaining / 1000) % 60;

                        String time = minutes + "m " + seconds + "s";

                        placeholders = new HashMap<>() {{
                            put("faction", other.getDisplayName());
                            put("faction.name", other.getDisplayName());
                            put("raid.type", raid.isRaider(faction) ? "Raiding" : "Defending");
                            put("time", time);
                        }};

                        body.add("    ");
                        body.add(MessageHandler.replacePlaceholdersStatic(raidTitle, placeholders));
                        body.add(MessageHandler.replacePlaceholdersStatic(raidText, placeholders));
                    } else {
                        body.add("     ");
                        body.add(statsTitle);
                        PlayerData data = playerManager.get(player.getUniqueId());
                        double kd = data.getKDRatio();
                        placeholders = new HashMap<>() {{
                            put("kills", String.valueOf(data.getKills()));
                            put("deaths", String.valueOf(data.getDeaths()));
                            put("kd", String.valueOf(kd));
                            put("kd.colour", kd >= 1 ? "&a" : "&c");
                            put("kd.color", kd >= 1 ? "&a" : "&c");
                            put("kd.col", kd >= 1 ? "&a" : "&c");
                        }};
                        body.add(MessageHandler.replacePlaceholdersStatic(statsText, placeholders));
                    }

                    // Footer
                    body.add("      ");
                    if(!footer.isEmpty())
                        body.add(footer);

                    return body;
                });
    }

    private void run() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            scoreboard.updateScoreboard();
            counter++;

            if(counter > 128)
                counter = 0;
        }, 20L, period);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        scoreboard.addPlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        scoreboard.removePlayer(e.getPlayer());
    }
}
