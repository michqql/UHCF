package me.michqql.uhcf.player;

import me.michqql.core.data.IData;
import me.michqql.core.data.IReadWrite;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

public class PlayerData implements IReadWrite {

    final UUID uuid;
    final Player player;

    // Statistics
    private int kills = 0, deaths = 0;
    private final HashMap<String, Object> statistics = new HashMap<>();

    PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.player = player;
    }

    @Override
    public void read(IData data) {
        this.kills = data.getInteger("kills");
        this.deaths = data.getInteger("deaths");

        IData customStats = data.getSection("custom-stats");
        for(String key : customStats.getKeys()) {
            statistics.put(key, customStats.get(key));
        }
    }

    @Override
    public void write(IData data) {
        data.set("kills", kills);
        data.set("deaths", deaths);

        IData customStats = data.createSection("custom-stats");
        statistics.forEach(customStats::set);
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

    public Set<String> getCustomStatisticKeys() {
        return statistics.keySet();
    }

    public Object getCustomStatistic(String key) {
        return statistics.get(key);
    }

    public void setCustomStatistic(String key, Object o) {
        statistics.put(key, o);
    }

    public void computeCustomStatistic(String key, BiFunction<String, Object, Object> computeFunction) {
        statistics.compute(key, computeFunction);
    }
}
