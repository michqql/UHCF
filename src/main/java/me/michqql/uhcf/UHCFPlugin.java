package me.michqql.uhcf;

import me.michqql.core.gui.GuiHandler;
import me.michqql.core.io.CommentFile;
import me.michqql.core.util.MessageHandler;
import me.michqql.uhcf.claim.ClaimsManager;
import me.michqql.uhcf.commands.admin.AdminCommandManager;
import me.michqql.uhcf.commands.faction.FactionCommandManager;
import me.michqql.uhcf.faction.FactionsManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class UHCFPlugin extends JavaPlugin {

    public final static String ADMIN_PERMISSION = "uhcf.admin";

    @Override
    public void onEnable() {
        final CommentFile langFile = new CommentFile(this, "", "lang");
        final CommentFile factionsConfig = new CommentFile(this, "configs", "factions_config");

        final GuiHandler guiHandler = new GuiHandler(this);
        final MessageHandler messageHandler = new MessageHandler(langFile.getConfig());

        final FactionsManager factionsManager = new FactionsManager(factionsConfig);
        final ClaimsManager claimsManager = new ClaimsManager(factionsConfig);

        // Register Commands & Listeners
        Objects.requireNonNull(getCommand("faction"))
                .setExecutor(new FactionCommandManager(this, messageHandler, guiHandler, factionsManager));
        Objects.requireNonNull(getCommand("admin"))
                .setExecutor(new AdminCommandManager(this, messageHandler, factionsManager, claimsManager));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
