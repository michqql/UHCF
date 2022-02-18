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

import me.michqql.uhcf.listeners.building.BlockListener;
import me.michqql.uhcf.listeners.claims.MovementListener;
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

    private ClaimOutlineManager claimOutlineManager;

    @Override
    public void onEnable() {
        instance = this;

        final CommentFile langFile = new CommentFile(this, "", "lang");
        final CommentFile factionsConfig = new CommentFile(this, "", "config");

        final GuiHandler guiHandler = new GuiHandler(this);
        final MessageHandler messageHandler = new MessageHandler(langFile.getConfig());

        this.factionsManager = new FactionsManager(factionsConfig);
        this.claimsManager = new ClaimsManager(factionsConfig);
        factionsManager.load();

        this.claimOutlineManager = new ClaimOutlineManager(this);

        // Register Commands & Listeners
        Objects.requireNonNull(getCommand("faction"))
                .setExecutor(new FactionCommandManager(
                        this, messageHandler, guiHandler, factionsManager,
                        claimsManager, claimOutlineManager, factionsConfig.getConfig()
                ));

        Objects.requireNonNull(getCommand("admin"))
                .setExecutor(new AdminCommandManager(this, messageHandler, factionsManager, claimsManager));

        // Listeners
        new MovementListener(this);
        new BlockListener(this, factionsManager, claimsManager, claimOutlineManager);
    }

    @Override
    public void onDisable() {
        AbstractListener.unregister(this);

        // Save data
        factionsManager.save();

        claimOutlineManager.onDisable();
    }

    public FactionsManager getFactionsManager() {
        return factionsManager;
    }

    public ClaimsManager getClaimsManager() {
        return claimsManager;
    }
}
