package me.michqql.uhcf.commands.admin;

import me.michqql.core.command.CommandManager;
import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.uhcf.claim.ClaimsManager;
import me.michqql.uhcf.faction.FactionsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;

public class AdminCommandManager extends CommandManager {

    private final FactionsManager factionsManager;
    private final ClaimsManager claimsManager;

    public AdminCommandManager(Plugin bukkitPlugin, MessageHandler messageHandler,
                               FactionsManager factionsManager, ClaimsManager claimsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
        this.claimsManager = claimsManager;
    }

    @Override
    protected void registerSubCommands() {
        subCommands.addAll(Arrays.asList(
                new CreateAdminFactionSubCommand(bukkitPlugin, messageHandler, factionsManager),
                new ClaimLandSubCommand(bukkitPlugin, messageHandler, factionsManager, claimsManager),
                new TempJoinSubCommand(bukkitPlugin, messageHandler, factionsManager)
        ));
    }

    @Override
    protected String getName() {
        return "admin";
    }

    @Override
    protected void sendInvalidSubCommandMessage(CommandSender commandSender) {
        messageHandler.sendList(commandSender, "invalid-command", new HashMap<>(){{
            put("command", getName());
        }});
    }

    @Override
    protected void sendNoPermissionMessage(CommandSender commandSender, SubCommand subCommand) {
        messageHandler.sendList(commandSender, "no-permission");
    }

    @Override
    protected void sendRequiresPlayerMessage(CommandSender commandSender, SubCommand subCommand) {
        messageHandler.sendList(commandSender, "requires-player");
    }
}
