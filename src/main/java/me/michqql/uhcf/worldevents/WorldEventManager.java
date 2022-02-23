package me.michqql.uhcf.worldevents;

import me.michqql.core.io.CommentFile;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class WorldEventManager {

    private final static Random RANDOM = new Random();

    private final Queue<WorldEvent> worldEventQueue = new LinkedList<>();
    private final List<WorldEvent> events = new ArrayList<>();

    private final List<WorldEvent> activeEvents = new ArrayList<>();

    private final UHCFPlugin plugin;

    private boolean disabled;

    // Config
    private long timeBetweenEventsInMillis;

    public WorldEventManager(UHCFPlugin plugin, CommentFile config) {
        this.plugin = plugin;

        // Load config
        FileConfiguration f = config.getConfig();
        String timeString = f.getString("time-between-events");
        if(timeString == null || timeString.isEmpty()) {
            Bukkit.getLogger().warning("[UHCF] Error while loading world events config, invalid time: " + timeString);
            Bukkit.getLogger().warning("[UHCF] Events will not start automatically");
            return;
        }

        this.timeBetweenEventsInMillis = TimeUtil.toMillis(TimeUtil.parseString(timeString));

        startTimer();
    }

    private void startTimer() {
        // Queue timer
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                startNextEvent();
            }
        }, 1000L, timeBetweenEventsInMillis);

        // Tick timer (code is called every tick)
        new BukkitRunnable() {
            @Override
            public void run() {
                for(WorldEvent active : activeEvents) {
                    active.delta++;

                    if(active.delta >= active.tickDelta) {
                        active.delta = 0;
                        active.tick();
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 1L);
    }

    private void startNextEvent() {
        if(disabled)
            return;

        if(worldEventQueue.isEmpty()) {
            WorldEvent event = getRandomEvent();
            if(event == null) {
                Bukkit.getLogger().warning("[UHCF] Failed to queue/start world event, no inactive events");
                return;
            }

            worldEventQueue.add(event);
        }

        // Get next event in queue
        WorldEvent event = worldEventQueue.poll();
        if(event == null)
            return;

        // Start event
        event.internalStart();
        activeEvents.add(event);

        // Queue another event
        WorldEvent nextEvent = getRandomEvent();
        if(nextEvent == null) {
            Bukkit.getLogger().warning("[UHCF] Failed to queue world event, no inactive events");
            return;
        }

        worldEventQueue.add(nextEvent);
    }

    private WorldEvent getRandomEvent() {
        List<Integer> indexes = new ArrayList<>();
        for(int i = 0; i < events.size(); i++)
            indexes.add(i);

        while(indexes.size() > 0) {
            int randomIndex = RANDOM.nextInt(indexes.size());
            WorldEvent event = events.get(randomIndex);
            if(!event.active)
                return event;

            indexes.remove(randomIndex);
        }
        return null;
    }

    public void registerEvent(WorldEvent event) {
        events.add(event);
    }

    public Set<WorldEvent> getEventsByType(Class<? extends WorldEvent> type) {
        Set<WorldEvent> eventsOfType = new HashSet<>();
        for(WorldEvent event : events) {
            if(event.getClass().equals(type))
                eventsOfType.add(event);
        }
        return eventsOfType;
    }

    public List<WorldEvent> getEvents() {
        return events;
    }

    public Queue<WorldEvent> getWorldEventQueue() {
        return worldEventQueue;
    }

    public List<WorldEvent> getActiveEvents() {
        return activeEvents;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
