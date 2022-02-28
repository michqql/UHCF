package me.michqql.uhcf.player;

import me.michqql.core.data.IData;
import me.michqql.core.data.IReadWrite;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiFunction;

public class PlayerData implements IReadWrite {

    final UUID uuid;
    final Player player;

    // Statistics
    private int kills = 0, deaths = 0;

    // Settings
    private final HashMap<String, Object> settingsMap = new HashMap<>();

    PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.player = player;
    }

    @Override
    public void read(IData data) {
        this.kills = data.getInteger("kills");
        this.deaths = data.getInteger("deaths");

        IData settings = data.getSection("settings");
        for(String key : settings.getKeys()) {
            settingsMap.put(key, settings.get(key));
        }
    }

    @Override
    public void write(IData data) {
        data.set("kills", kills);
        data.set("deaths", deaths);

        IData settings = data.createSection("settings");
        settingsMap.forEach(settings::set);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public Player getPlayer() {
        return player;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void incrementKills() {
        this.kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void incrementDeaths() {
        this.deaths++;
    }

    public double getKDRatio() {
        double d = deaths;
        if(d == 0)
            d = 1;

        return kills / d;
    }

    public Object getSetting(String key) {
        return settingsMap.get(key);
    }

    public <T> Object getSetting(String key, T def) {
        Object obj = settingsMap.get(key);
        if(obj == null)
            return def;

        return obj;
    }

    public void setSetting(String key, Object obj) {
        this.settingsMap.put(key, obj);
    }

    public Object computeSetting(String key, BiFunction<String, Object, Object> compute) {
        return this.settingsMap.compute(key, compute);
    }
}
