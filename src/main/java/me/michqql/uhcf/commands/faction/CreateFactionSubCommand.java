package me.michqql.uhcf.commands.faction;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CreateFactionSubCommand extends SubCommand {

    private final FactionsManager factionsManager;

    public CreateFactionSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler, FactionsManager factionsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
    }

    // /faction create <name>

    @Override
    protected void registerSubCommands() {

    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if(args.length == 0) {
            messageHandler.sendList(sender, "command-usage",
                    Placeholder.of("command", "faction create <name>"));
            return;
        }

        if(args.length > 1) {
            messageHandler.sendList(sender, "faction-command.create.name-cannot-contain-spaces");
            return;
        }

        String name = args[0];
        Player player = (Player) sender;

        // Check if player is in faction
        PlayerFaction current = factionsManager.getPlayerFactionByPlayer(player.getUniqueId());
        if(current != null) {
            messageHandler.sendList(player, "faction-command.create.already-in-faction");
        }

        // Check id is valid
        if(!factionsManager.validateId(name)) {
            messageHandler.sendList(player, "faction-command.create.invalid-id");
            return;
        }

        // Check if id is in use
        PlayerFaction faction = factionsManager.getPlayerFactionById(name);
        if(faction != null) {
            messageHandler.sendList(player, "faction-command.create.name-taken",
                    Placeholder.of("name", faction.getDisplayName()));
            return;
        }

        // Create faction
        PlayerFaction created = new PlayerFaction(name.toLowerCase(Locale.ROOT), player.getUniqueId());
        factionsManager.createPlayerFaction(created);

        // Set leader to player
        created.setDisplayName(name);
        created.getMembers().setOwner(player.getUniqueId());

        // Send messages
        messageHandler.sendList(player, "faction-command.create.faction-created",
                Placeholder.of("name", name));
    }

    @Override
    protected String getName() {
        return "create";
    }

    @Override
    protected List<String> getAliases() {
        return null;
    }

    @Override
    protected String getPermission() {
        return "";
    }

    @Override
    protected List<String> getArguments(CommandSender commandSender) {
        return null;
    }

    @Override
    protected boolean requiresPlayer() {
        return true;
    }
}
