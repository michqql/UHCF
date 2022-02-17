package me.michqql.uhcf.faction.load;

import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;

public abstract class FactionLoader {

    public final static String PLAYER_FACTIONS_DIRECTORY = "factions/player";
    public final static String ADMIN_FACTIONS_DIRECTORY = "factions/admin";

    protected final UHCFPlugin plugin;
    protected final FactionsManager factionsManager;

    public FactionLoader(UHCFPlugin plugin, FactionsManager factionsManager) {
        this.plugin = plugin;
        this.factionsManager = factionsManager;
    }
    
    public abstract void loadPlayer(String id);
    public abstract void loadPlayerSaved();
    
    public abstract void savePlayer(PlayerFaction faction);
    public abstract void savePlayerAll();

    public abstract void loadAdmin(String id);
    public abstract void loadAdminSaved();

    public abstract void saveAdmin(AdminFaction faction);
    public abstract void saveAdminAll();
}
