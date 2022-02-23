package me.michqql.uhcf.commands.admin;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CreateAdminFactionSubCommand extends SubCommand {

    private final FactionsManager factionsManager;

    public CreateAdminFactionSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler, FactionsManager factionsManager) {
        super(bukkitPlugin, messageHandler);
        this.factionsManager = factionsManager;
    }

    // /x create <name> [-p]

    @Override
    protected void registerSubCommands() {

    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if(args.length == 0) {
            messageHandler.sendList(sender, "admin-command.create.usage");
            return;
        }

        String name = args[0];
        boolean playerFaction = hasFlag("-p", args);

        // Convert name to id
        // Check id is valid
        if(!factionsManager.validateId(name)) {
            messageHandler.sendList(sender, "admin-command.create.invalid-id");
            return;
        }

        if(playerFaction)
            createPlayerFaction(sender, name);
        else
            createAdminFaction(sender, name);
    }

    private void createAdminFaction(CommandSender sender, String name) {
        // Check if id is in use
        AdminFaction faction = factionsManager.getAdminFactionById(name);
        if(faction != null) {
            messageHandler.sendList(sender, "admin-command.create.name-taken", new HashMap<>(){{
                put("name", faction.getDisplayName());
                put("type", "admin");
            }});
            return;
        }

        // Create faction
        AdminFaction created = new AdminFaction(name.toLowerCase(Locale.ROOT));
        factionsManager.createAdminFaction(created);
        created.setDisplayName(name);

        // Send messages
        messageHandler.sendList(sender, "admin-command.create.faction-created", new HashMap<>(){{
            put("name", name);
            put("type", "admin");
        }});
    }

    private void createPlayerFaction(CommandSender sender, String name) {
        // Check if id is in use
        PlayerFaction faction = factionsManager.getPlayerFactionById(name);
        if(faction != null) {
            messageHandler.sendList(sender, "admin-command.create.name-taken", new HashMap<>(){{
                put("name", faction.getDisplayName());
                put("type", "player");
            }});
            return;
        }

        // Create faction
        PlayerFaction created = new PlayerFaction(name.toLowerCase(Locale.ROOT), ((Player) sender).getUniqueId());
        factionsManager.createPlayerFaction(created);
        created.setDisplayName(name);

        // Send messages
        messageHandler.sendList(sender, "admin-command.create.faction-created", new HashMap<>(){{
            put("name", name);
            put("type", "player");
        }});
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
        return UHCFPlugin.ADMIN_PERMISSION;
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
