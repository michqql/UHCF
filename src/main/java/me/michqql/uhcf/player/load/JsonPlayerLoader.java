package me.michqql.uhcf.player.load;

import me.michqql.core.data.JsonData;
import me.michqql.core.io.JsonFile;
import me.michqql.core.util.FileUtils;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Members;
import me.michqql.uhcf.player.PlayerData;
import me.michqql.uhcf.player.PlayerManager;

import java.util.List;
import java.util.UUID;

public class JsonPlayerLoader extends PlayerLoader {

    public JsonPlayerLoader(UHCFPlugin plugin, PlayerManager playerManager) {
        super(plugin, playerManager);
    }

    @Override
    public void loadPlayer(PlayerData player) {
        JsonFile file = new JsonFile(plugin, PLAYER_DIRECTORY, player.getUniqueId().toString());
        JsonData data = new JsonData(file);

        player.read(data);
    }

    @Override
    public void savePlayer(PlayerData player) {
        JsonFile file = new JsonFile(plugin, PLAYER_DIRECTORY, player.getUniqueId().toString());
        JsonData data = new JsonData(file);

        player.write(data);
        data.save();
        data.close();
    }
}
