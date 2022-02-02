package me.michqql.uhcf.faction;

import me.michqql.core.io.CommentFile;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FactionsManager {

    // Config
    private final CommentFile factionsConfigFile;
    private Pattern identifierRegexPattern;

    // Factions
    private final HashMap<String, PlayerFaction> playerFactions = new HashMap<>();
    private final HashMap<String, AdminFaction> adminFactions = new HashMap<>();

    // Faction members
    private final HashMap<UUID, PlayerFaction> playerToFactionMap = new HashMap<>();
    private final HashMap<UUID, Faction> temporaryPlayerToFactionMap = new HashMap<>();

    // Faction invites & requests
    private final HashMap<UUID, HashMap<PlayerFaction, Long>> playerInvites = new HashMap<>();

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

    public Collection<PlayerFaction> getPlayerFactions() {
        return Collections.unmodifiableCollection(playerFactions.values());
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
        else {
            playerToFactionMap.put(uuid, playerFaction);
            playerInvites.compute(uuid, (uuid1, factions) -> {
                if(factions == null)
                    return null;

                factions.remove(playerFaction);
                return factions;
            });
        }
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

    public HashMap<PlayerFaction, Long> getPlayerInvites(UUID uuid) {
        return playerInvites.getOrDefault(uuid, new HashMap<>());
    }

    public void invitePlayer(UUID uuid, PlayerFaction faction) {
        playerInvites.compute(uuid, (uuid1, factions) -> {
            if(factions == null)
                factions = new HashMap<>();

            factions.put(faction, System.currentTimeMillis());
            return factions;
        });
    }

    public boolean validateId(String id) {
        id = id.toLowerCase(Locale.ROOT);
        Matcher matcher = identifierRegexPattern.matcher(id);
        return matcher.matches();
    }
}
