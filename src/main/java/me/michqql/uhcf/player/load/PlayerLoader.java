package me.michqql.uhcf.player.load;

import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.player.PlayerData;
import me.michqql.uhcf.player.PlayerManager;

import java.util.UUID;

public abstract class PlayerLoader {

    public final static String PLAYER_DIRECTORY = "players";

    protected final UHCFPlugin plugin;
    protected final PlayerManager playerManager;

    public PlayerLoader(UHCFPlugin plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }
    
    public abstract void loadPlayer(PlayerData data);
    
    public abstract void savePlayer(PlayerData data);
}
