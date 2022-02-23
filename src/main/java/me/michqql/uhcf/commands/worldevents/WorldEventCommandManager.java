package me.michqql.uhcf.commands.worldevents;

import me.michqql.core.command.CommandManager;
import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.worldevents.WorldEventManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class WorldEventCommandManager extends CommandManager {

    private final WorldEventManager worldEventManager;

    public WorldEventCommandManager(Plugin bukkitPlugin, MessageHandler messageHandler, WorldEventManager worldEventManager) {
        super(bukkitPlugin, messageHandler);
        this.worldEventManager = worldEventManager;
    }

    @Override
    protected void registerSubCommands() {

    }

    @Override
    protected void sendInvalidSubCommandMessage(CommandSender commandSender, String s) {
        messageHandler.sendList(commandSender, "invalid-command",
                Placeholder.of("command", "worldevent"));
    }

    @Override
    protected void sendNoPermissionMessage(CommandSender commandSender, SubCommand subCommand) {
        messageHandler.sendList(commandSender, "no-permission");
    }

    @Override
    protected void sendRequiresPlayerMessage(CommandSender commandSender, SubCommand subCommand) {
        messageHandler.sendList(commandSender, "requires-player");
    }

    @Override
    protected String getName() {
        return "worldevent";
    }
}
