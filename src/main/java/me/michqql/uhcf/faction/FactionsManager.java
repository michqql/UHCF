package me.michqql.uhcf.faction;

import me.michqql.core.io.CommentFile;
import me.michqql.core.util.Pair;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.attributes.Members;
import me.michqql.uhcf.faction.attributes.Relations;
import me.michqql.uhcf.faction.load.FactionLoader;
import me.michqql.uhcf.faction.load.JsonFactionLoader;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FactionsManager {

    // Config
    private final CommentFile factionsConfigFile;
    private String saveMethod;
    private Pattern identifierRegexPattern;
    private int maximumFactionSize;
    private double maximumDistributedSize;
    private double factionWeight;
    private double truceWeight;
    private double allianceWeight;

    // Factions
    private final HashMap<String, PlayerFaction> playerFactions = new HashMap<>();
    private final HashMap<String, AdminFaction> adminFactions = new HashMap<>();

    // Saving
    private FactionLoader loader;

    // Faction members
    private final HashMap<UUID, PlayerFaction> playerToFactionMap = new HashMap<>();
    // TODO: Clean up temporary members
    private final HashMap<UUID, Faction> temporaryPlayerToFactionMap = new HashMap<>();

    // Faction invites & requests
    // TODO: Refactor into different class
    private final HashMap<UUID, HashMap<PlayerFaction, Long>> playerInvites = new HashMap<>();
    private final HashMap<PlayerFaction, HashMap<PlayerFaction, Pair<Relations.Relation, Long>>> relationRequests = new HashMap<>();

    public FactionsManager(CommentFile factionsConfigFile) {
        this.factionsConfigFile = factionsConfigFile;
        loadConfig();
    }

    // TODO: Clean up methods

    private void loadConfig() {
        FileConfiguration f = factionsConfigFile.getConfig();

        this.saveMethod = f.getString("save-method", "json");
        if("json".equalsIgnoreCase(saveMethod)) {
            // TODO: lazy
            this.loader = new JsonFactionLoader(UHCFPlugin.getInstance(), this);
        }

        String regexPattern = f.getString("faction-id-regex-pattern");
        if(regexPattern == null) {
            Bukkit.getLogger().warning("[config.yml] No faction id regex pattern given at 'faction-id-regex-pattern'");
            regexPattern = "[a-zA-Z0-9]{3,10}";
        }
        this.identifierRegexPattern = Pattern.compile(regexPattern);

        this.maximumFactionSize = f.getInt("max-faction-size", 18);
        this.maximumDistributedSize = f.getDouble("max-distributed-size", 20.0D);
        this.factionWeight = f.getDouble("multipliers.truce", 1.0D);
        this.truceWeight = f.getDouble("multipliers.ally", 1.5D);
        this.allianceWeight = f.getDouble("multipliers.faction", 2.0D);
    }

    public void save() {
        loader.saveAdminAll();
        loader.savePlayerAll();
    }

    public void load() {
        loader.loadAdminSaved();
        loader.loadPlayerSaved();
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

    public Collection<AdminFaction> getAdminFactions() {
        return Collections.unmodifiableCollection(adminFactions.values());
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

    public boolean canInvitePlayer(PlayerFaction faction) {
        int memberSize = faction.getMembers().getSize();
        if(memberSize >= maximumFactionSize)
            return false;

        double currentSize = calculateDistributedSize(faction);
        double increase = factionWeight;

        return currentSize + increase <= maximumDistributedSize;
    }

    public void invitePlayer(UUID uuid, PlayerFaction faction) {
        playerInvites.compute(uuid, (uuid1, factions) -> {
            if(factions == null)
                factions = new HashMap<>();

            factions.put(faction, System.currentTimeMillis());
            return factions;
        });
    }

    public HashMap<PlayerFaction, Pair<Relations.Relation, Long>> getRelationRequests(PlayerFaction requester) {
        return relationRequests.getOrDefault(requester, new HashMap<>());
    }

    public boolean canHaveRelation(PlayerFaction requester, PlayerFaction requestee, Relations.Relation type) {
        if(type == Relations.Relation.NONE)
            return true;

        double currentSize = calculateDistributedSize(requester);
        double increase = requestee.getMembers().getSize() * (type == Relations.Relation.ALLY ? allianceWeight : truceWeight);

        return currentSize + increase <= maximumDistributedSize;
    }

    public boolean isRequestingRelation(PlayerFaction requester, PlayerFaction requestee, Relations.Relation type) {
        HashMap<PlayerFaction, Pair<Relations.Relation, Long>> requests = getRelationRequests(requester);
        if(requests.isEmpty())
            return false;

        Pair<Relations.Relation, Long> pair = requests.get(requestee);
        if(pair == null)
            return false;

        return type == pair.getKey();
    }

    public void requestRelation(PlayerFaction requester, PlayerFaction requestee, Relations.Relation type) {
        relationRequests.compute(requester, (f1, requests) -> {
            if(requests == null)
                requests = new HashMap<>();

            requests.put(requestee, new Pair<>(type, System.currentTimeMillis()));
            return requests;
        });
    }

    public void acceptRequest(PlayerFaction requester, PlayerFaction requestee) {
        relationRequests.compute(requester, (f1, requests) -> {
            if(requests == null)
                return null;

            Pair<Relations.Relation, Long> pair = requests.remove(requestee);
            if(pair == null)
                return null;

            Relations.Relation type = pair.getKey();
            requester.getRelations().setRelation(requestee, type);
            requestee.getRelations().setRelation(requester, type);
            return requests;
        });
    }

    public boolean validateId(String id) {
        id = id.toLowerCase(Locale.ROOT);
        Matcher matcher = identifierRegexPattern.matcher(id);
        return matcher.matches();
    }

    public double calculateDistributedSize(PlayerFaction faction) {
        // size = (fS * fM) + (aS * aM) + (tS * tM)
        double size = 0.0D;

        Members members = faction.getMembers();
        size += members.getSize() * factionWeight;

        Relations relations = faction.getRelations();
        for(PlayerFaction ally : relations.getAlliances())
            size += ally.getMembers().getSize() * allianceWeight;

        for(PlayerFaction truce : relations.getTruces())
            size += truce.getMembers().getSize() * truceWeight;

        return size;
    }
}
