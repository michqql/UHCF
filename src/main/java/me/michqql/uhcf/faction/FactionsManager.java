package me.michqql.uhcf.faction;

import me.michqql.core.io.CommentFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FactionsManager {

    // Config
    private final CommentFile factionsConfigFile;
    private Pattern identifierRegexPattern;

    private final HashMap<String, PlayerFaction> playerFactions = new HashMap<>();
    private final HashMap<String, AdminFaction> adminFactions = new HashMap<>();
    private final HashMap<UUID, PlayerFaction> playerToFactionMap = new HashMap<>();
    private final HashMap<UUID, Faction> temporaryPlayerToFactionMap = new HashMap<>();

    public FactionsManager(CommentFile factionsConfigFile) {
        this.factionsConfigFile = factionsConfigFile;
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration f = factionsConfigFile.getConfig();

        String regexPattern = f.getString("faction-id-regex-pattern");
        if(regexPattern == null) {
            Bukkit.getLogger().warning("[factions_config.yml] No faction id regex pattern given at 'faction-id-regex-pattern'");
            regexPattern = "[a-zA-Z0-9]{3,10}";
        }
        identifierRegexPattern = Pattern.compile(regexPattern);
    }

    public PlayerFaction getPlayerFactionById(String id) {
        return playerFactions.get(id.toLowerCase(Locale.ROOT));
    }

    public void createPlayerFaction(PlayerFaction faction) {
        playerFactions.putIfAbsent(faction.getUniqueIdentifier(), faction);
    }

    public AdminFaction getAdminFactionById(String id) {
        return adminFactions.get(id.toLowerCase(Locale.ROOT));
    }

    public void createAdminFaction(AdminFaction faction) {
        adminFactions.putIfAbsent(faction.getUniqueIdentifier(), faction);
    }

    public PlayerFaction getPlayerFactionByPlayer(UUID uuid) {
        return playerToFactionMap.get(uuid);
    }

    public void setPlayerFaction(UUID uuid, PlayerFaction playerFaction) {
        if(playerFaction == null)
            playerToFactionMap.remove(uuid);
        else
            playerToFactionMap.put(uuid, playerFaction);
    }

    public Faction getTemporaryFactionByPlayer(UUID uuid) {
        return temporaryPlayerToFactionMap.get(uuid);
    }

    public void setTemporaryFaction(UUID uuid, Faction faction) {
        if(faction == null)
            temporaryPlayerToFactionMap.remove(uuid);
        else
            temporaryPlayerToFactionMap.put(uuid, faction);
    }

    public boolean validateId(String id) {
        id = id.toLowerCase(Locale.ROOT);
        Matcher matcher = identifierRegexPattern.matcher(id);
        return matcher.matches();
    }
}
