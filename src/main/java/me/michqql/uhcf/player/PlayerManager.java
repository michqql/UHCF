package me.michqql.uhcf.player;

import me.michqql.core.io.CommentFile;
import me.michqql.core.util.AbstractListener;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.player.load.JsonPlayerLoader;
import me.michqql.uhcf.player.load.PlayerLoader;
import me.michqql.uhcf.player.load.YamlPlayerLoader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager extends AbstractListener {

    private final PlayerLoader loader;
    private final HashMap<UUID, PlayerData> playerData = new HashMap<>();

    public PlayerManager(UHCFPlugin plugin, CommentFile configFile) {
        super(plugin);

        String saveMethod = configFile.getConfig().getString("save-method", "json");
        if("json".equalsIgnoreCase(saveMethod)) {
            this.loader = new JsonPlayerLoader(plugin, this);
        } else if("yaml".equalsIgnoreCase(saveMethod) || "yml".equalsIgnoreCase(saveMethod)) {
            this.loader = new YamlPlayerLoader(plugin, this);
        } else {
            Bukkit.getLogger().severe("[UHCF] Unsupported player data loader: " + saveMethod);
            this.loader = null;
        }
    }

    public PlayerData get(UUID uuid) {
        return playerData.get(uuid);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        loadPlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        unloadPlayer(e.getPlayer());
    }

    public void onEnable() {
        Bukkit.getOnlinePlayers().forEach(this::loadPlayer);
    }

    public void onDisable() {
        playerData.forEach((uuid, data) -> {
            loader.savePlayer(data);
        });
    }

    private void loadPlayer(Player player) {
        PlayerData data = new PlayerData(player);
        // Load data
        loader.loadPlayer(data);
        playerData.put(data.uuid, data);
    }

    private void unloadPlayer(Player player) {
        PlayerData data = playerData.remove(player.getUniqueId());
        if(data == null)
            return;

        // Save data
        loader.savePlayer(data);
    }
}
