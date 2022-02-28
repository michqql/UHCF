package me.michqql.uhcf.util;

import me.michqql.uhcf.UHCFPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

public class EventUtil {

    public static void call(Event event) {
        if(!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(UHCFPlugin.getInstance(), () ->
                    Bukkit.getPluginManager().callEvent(event));
            return;
        }

        Bukkit.getPluginManager().callEvent(event);
    }
}
