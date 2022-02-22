package me.michqql.uhcf.player;

import me.michqql.core.data.IData;
import me.michqql.core.data.IReadWrite;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerData implements IReadWrite {

    final UUID uuid;
    final Player player;

    // Statistics
    private int kills, deaths;

    PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.player = player;
    }

    @Override
    public void read(IData data) {
        this.kills = data.getInteger("kills");
        this.deaths = data.getInteger("deaths");
    }

    @Override
    public void write(IData data) {
        data.set("kills", kills);
        data.set("deaths", deaths);
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
}
