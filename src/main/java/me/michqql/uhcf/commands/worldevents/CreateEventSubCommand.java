package me.michqql.uhcf.commands.worldevents;

import me.michqql.core.command.SubCommand;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.worldevents.WorldEventManager;
import me.michqql.uhcf.worldevents.type.KOTH;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Locale;

public class CreateEventSubCommand extends SubCommand {

    private final static List<String> EVENT_TYPES = List.of("koth");

    private final WorldEventManager worldEventManager;

    public CreateEventSubCommand(Plugin bukkitPlugin, MessageHandler messageHandler, WorldEventManager worldEventManager) {
        super(bukkitPlugin, messageHandler);
        this.worldEventManager = worldEventManager;
    }

    // /event create <type> <name>

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        if(args.length < 2) {
            messageHandler.sendList(sender, "command-usage",
                    Placeholder.of("command", "worldevent create <type> <name>"));
            return;
        }

        String type = args[0].toLowerCase(Locale.ROOT);
        if(!EVENT_TYPES.contains(type)) {
            messageHandler.sendList(sender, "world-event-command.invalid-type",
                    Placeholder.of("type", type, "types", EVENT_TYPES.toString()));
            return;
        }

        StringBuilder builder = new StringBuilder();
        for(int i = 1; i < args.length; i++) {
            builder.append(args[i]).append(' ');
        }

        String name = builder.toString().trim();

//        if("koth".equalsIgnoreCase(type)) {
//            KOTH koth = new KOTH(bukkitPlugin, )
//        }
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
        return EVENT_TYPES;
    }

    @Override
    protected boolean requiresPlayer() {
        return false;
    }
}
