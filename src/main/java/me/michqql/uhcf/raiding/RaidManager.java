package me.michqql.uhcf.raiding;

import me.michqql.core.io.CommentFile;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.PlayerFaction;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RaidManager {

    private final UHCFPlugin plugin;
    private final Set<Raid> ongoingRaids = new HashSet<>();

    // Config
    private final long raidDuration;
    private final int warpointThreshold;
    private final boolean automaticRaiding;

    public RaidManager(UHCFPlugin plugin, CommentFile config) {
        this.plugin = plugin;

        // Load config
        FileConfiguration f = config.getConfig();
        this.raidDuration = f.getLong("raid-duration");
        this.warpointThreshold = f.getInt("warpoints-required-to-start-raid");
        this.automaticRaiding = f.getBoolean("automatic-raiding");
    }

    public boolean startRaid(PlayerFaction raiders, PlayerFaction defenders) {
        // Check raiders meet the warpoint threshold
        if(raiders.getWarpoints().getWarpoints(defenders) < warpointThreshold)
            return false;

        Raid raid = getRaidByFactions(raiders, defenders);
        if(raid != null)
            return false;

        raid = new Raid(plugin, raiders, defenders, raidDuration);
        ongoingRaids.add(raid);
        return true;
    }

    public void stopRaid(Raid raid) {
        ongoingRaids.remove(raid);
        raid.forceEnd();
    }

    public Raid getRaidByFactions(PlayerFaction f0, PlayerFaction f1) {
        if(f0.equals(f1))
            return null;

        for(Raid raid : ongoingRaids)
            if(raid.isInvolved(f0) && raid.isInvolved(f1))
                return raid;

        return null;
    }

    public Raid getRaidByFactionsExact(PlayerFaction raiders, PlayerFaction defenders) {
        if(raiders.equals(defenders))
            return null;

        for(Raid raid : ongoingRaids)
            if(raid.isRaider(raiders) && raid.isDefender(defenders))
                return raid;

        return null;
    }

    @NotNull
    public RaidList getRaidsByFaction(PlayerFaction faction) {
        List<Raid> raids = new ArrayList<>();
        for(Raid raid : ongoingRaids)
            if(raid.isInvolved(faction))
                raids.add(raid);

        return new RaidList(faction, raids);
    }

    public Set<PlayerFaction> getFactionsInRaid() {
        Set<PlayerFaction> factions = new HashSet<>();
        ongoingRaids.forEach(raid -> {
            factions.add(raid.getRaiders());
            factions.add(raid.getDefenders());
        });

        return factions;
    }

    public int getWarpointThreshold() {
        return warpointThreshold;
    }

    public boolean isAutomaticRaiding() {
        return automaticRaiding;
    }
}
