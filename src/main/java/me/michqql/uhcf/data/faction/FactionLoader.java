package me.michqql.uhcf.data.faction;

import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.Faction;
import me.michqql.uhcf.faction.PlayerFaction;

public abstract class FactionLoader {

    protected final UHCFPlugin plugin;

    public FactionLoader(UHCFPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract PlayerFaction loadPlayerFaction(String identifier);
    public abstract AdminFaction loadAdminFaction(String identifier);
    public abstract void save(Faction faction);
}
