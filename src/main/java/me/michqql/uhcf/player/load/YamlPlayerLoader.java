package me.michqql.uhcf.player.load;

import me.michqql.core.data.YamlData;
import me.michqql.core.io.YamlFile;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.player.PlayerData;
import me.michqql.uhcf.player.PlayerManager;

public class YamlPlayerLoader extends PlayerLoader {

    public YamlPlayerLoader(UHCFPlugin plugin, PlayerManager playerManager) {
        super(plugin, playerManager);
    }

    @Override
    public void loadPlayer(PlayerData player) {
        YamlFile file = new YamlFile(plugin, PLAYER_DIRECTORY, player.getUniqueId().toString());
        YamlData data = new YamlData(file);

        player.read(data);
    }

    @Override
    public void savePlayer(PlayerData player) {
        YamlFile file = new YamlFile(plugin, PLAYER_DIRECTORY, player.getUniqueId().toString());
        YamlData data = new YamlData(file);

        player.write(data);
        data.save();
        data.close();
    }
}
