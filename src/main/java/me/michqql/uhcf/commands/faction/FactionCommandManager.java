package me.michqql.uhcf.commands.faction;

import me.michqql.core.command.CommandManager;
import me.michqql.core.command.SubCommand;
import me.michqql.core.gui.GuiHandler;
import me.michqql.core.util.MessageHandler;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.gui.faction.ViewFactionInfoGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class FactionCommandManager extends CommandManager {

    private final GuiHandler guiHandler;
    private final FactionsManager factionsManager;

    public FactionCommandManager(Plugin bukkitPlugin, MessageHandler messageHandler,
                                 GuiHandler guiHandler, FactionsManager factionsManager) {
        super(bukkitPlugin, messageHandler);
        this.guiHandler = guiHandler;
        this.factionsManager = factionsManager;
    }

    @Override
    protected void registerSubCommands() {
        subCommands.addAll(Arrays.asList(
                new InfoSubCommand(bukkitPlugin, messageHandler, factionsManager),
                new WhoSubCommand(bukkitPlugin, messageHandler, factionsManager),
                new CreateFactionSubCommand(bukkitPlugin, messageHandler, factionsManager),
                new LeaveSubCommand(bukkitPlugin, messageHandler, factionsManager),
                new InviteSubCommand(bukkitPlugin, messageHandler, factionsManager),
                new JoinSubCommand(bukkitPlugin, messageHandler, factionsManager),
                new KickSubCommand(bukkitPlugin, messageHandler, factionsManager),
                new PromoteSubCommand(bukkitPlugin, messageHandler, factionsManager),
                new DemoteSubCommand(bukkitPlugin, messageHandler, factionsManager)
        ));
    }

    @Override
    protected String getName() {
        return "faction";
    }

    @Override
    protected void sendInvalidSubCommandMessage(CommandSender sender, String input) {
        if((input == null || input.isEmpty()) && (sender instanceof Player player)) {
            PlayerFaction faction = factionsManager.getPlayerFactionByPlayer(player.getUniqueId());
            if(faction != null) {
                new ViewFactionInfoGui(guiHandler, player, faction).openGui();
            } else {
                // TODO send faction help command
            }
            return;
        }

        SubCommand subCommand = getSubCommand("info");
        if(subCommand instanceof InfoSubCommand infoSubCommand)
            infoSubCommand.onCommand(sender, new String[]{ input });
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