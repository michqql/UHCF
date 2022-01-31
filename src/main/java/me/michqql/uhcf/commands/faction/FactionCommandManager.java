package me.michqql.uhcf.commands.faction;

import me.michqql.core.command.CommandManager;
import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.uhcf.faction.FactionsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;

public class FactionCommandManager extends CommandManager {

    private final FactionsManager factionsManager;

    public FactionCommandManager(Plugin bukkitPlugin, MessageHandler messageHandler, FactionsManager factionsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
    }

    @Override
    protected void registerSubCommands() {
        subCommands.addAll(List.of(
                new CreateFactionSubCommand(bukkitPlugin, messageHandler, factionsManager),
                new InvitePlayerSubCommand(bukkitPlugin, messageHandler, factionsManager)
        ));
    }

    @Override
    protected String getName() {
        return "faction";
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
