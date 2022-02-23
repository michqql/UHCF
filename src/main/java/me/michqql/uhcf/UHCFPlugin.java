package me.michqql.uhcf;

import me.michqql.core.gui.GuiHandler;
import me.michqql.core.io.CommentFile;
import me.michqql.core.util.AbstractListener;
import me.michqql.core.util.MessageHandler;

import me.michqql.uhcf.claim.ClaimsManager;
import me.michqql.uhcf.claim.outline.ClaimOutlineManager;
import me.michqql.uhcf.commands.admin.AdminCommandManager;
import me.michqql.uhcf.commands.faction.FactionCommandManager;
import me.michqql.uhcf.faction.FactionsManager;

import me.michqql.uhcf.listeners.BlockListener;
import me.michqql.uhcf.listeners.DamageListener;
import me.michqql.uhcf.listeners.MovementListener;
import me.michqql.uhcf.listeners.custom.CustomEventListener;
import me.michqql.uhcf.player.PlayerManager;
import me.michqql.uhcf.util.scoreboard.ScoreboardHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class UHCFPlugin extends JavaPlugin {

    public final static String ADMIN_PERMISSION = "uhcf.admin";

    private static UHCFPlugin instance;

    public static UHCFPlugin getInstance() {
        return instance;
    }

    private FactionsManager factionsManager;
    private ClaimsManager claimsManager;
    private PlayerManager playerManager;

    private ClaimOutlineManager claimOutlineManager;

    @Override
    public void onEnable() {
        instance = this;

        final CommentFile langFile = new CommentFile(this, "", "lang");
        final CommentFile configFile = new CommentFile(this, "", "config");

        final GuiHandler guiHandler = new GuiHandler(this);
        final MessageHandler messageHandler = new MessageHandler(langFile.getConfig());

        // High important managers
        this.factionsManager = new FactionsManager(this, configFile);
        this.claimsManager = new ClaimsManager(configFile);
        this.playerManager = new PlayerManager(this, configFile); // registers as event listener in constructor

        // Load saved data
        factionsManager.load();
        playerManager.onEnable();

        // Other, less important managers
        this.claimOutlineManager = new ClaimOutlineManager(this);
        new ScoreboardHandler(this, configFile, factionsManager, claimsManager, playerManager);

        // Register Commands & Listeners
        Objects.requireNonNull(getCommand("faction"))
                .setExecutor(new FactionCommandManager(
                        this, messageHandler, guiHandler, factionsManager,
                        claimsManager, claimOutlineManager, configFile.getConfig()
                ));

        Objects.requireNonNull(getCommand("admin"))
                .setExecutor(new AdminCommandManager(this, messageHandler, factionsManager, claimsManager));

        // Listeners
        new CustomEventListener(this);
        new MovementListener(this, configFile, factionsManager, claimsManager);
        new BlockListener(this, configFile, messageHandler, factionsManager, claimsManager, claimOutlineManager);
        new DamageListener(this, factionsManager, playerManager);
    }

    @Override
    public void onDisable() {
        AbstractListener.unregister(this);

        // Save data
        factionsManager.save();
        playerManager.onDisable();

        claimOutlineManager.onDisable();
    }

    public FactionsManager getFactionsManager() {
        return factionsManager;
    }

    public ClaimsManager getClaimsManager() {
        return claimsManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ClaimOutlineManager getClaimOutlineManager() {
        return claimOutlineManager;
    }
}
