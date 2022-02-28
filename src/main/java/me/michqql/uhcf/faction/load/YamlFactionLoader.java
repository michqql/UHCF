package me.michqql.uhcf.faction.load;

import me.michqql.core.data.JsonData;
import me.michqql.core.data.YamlData;
import me.michqql.core.io.JsonFile;
import me.michqql.core.io.YamlFile;
import me.michqql.core.util.FileUtils;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Members;

import java.util.List;

public class YamlFactionLoader extends FactionLoader {

    public YamlFactionLoader(UHCFPlugin plugin, FactionsManager factionsManager) {
        super(plugin, factionsManager);
    }

    @Override
    public void loadPlayer(String id) {
        YamlFile file = new YamlFile(plugin, PLAYER_FACTIONS_DIRECTORY, id);
        YamlData data = new YamlData(file);

        PlayerFaction playerFaction = new PlayerFaction(id);
        playerFaction.read(data);

        factionsManager.createPlayerFaction(playerFaction);

        Members members = playerFaction.getMembers();
        factionsManager.setPlayerFaction(members.getLeader(), playerFaction);
        members.getMembers().forEach(uuid -> factionsManager.setPlayerFaction(uuid, playerFaction));
    }

    @Override
    public void loadPlayerSaved() {
        List<String> playerFactions = FileUtils.getFilenamesInDirectory(plugin.getDataFolder(), PLAYER_FACTIONS_DIRECTORY);

        // Pre-load factions
        for(String id : playerFactions) {
            PlayerFaction playerFaction = new PlayerFaction(id);
            factionsManager.createPlayerFaction(playerFaction);
        }

        // Post-load faction data
        for(PlayerFaction faction : factionsManager.getPlayerFactions()) {
            YamlFile file = new YamlFile(plugin, PLAYER_FACTIONS_DIRECTORY, faction.getUniqueIdentifier());
            YamlData data = new YamlData(file);
            faction.read(data);

            Members members = faction.getMembers();
            factionsManager.setPlayerFaction(members.getLeader(), faction);
            members.getMembers().forEach(uuid -> factionsManager.setPlayerFaction(uuid, faction));
        }
    }

    @Override
    public void savePlayer(PlayerFaction faction) {
        YamlFile file = new YamlFile(plugin, PLAYER_FACTIONS_DIRECTORY, faction.getUniqueIdentifier());
        YamlData data = new YamlData(file);

        faction.write(data);
        data.save();
        data.close();
    }

    @Override
    public void savePlayerAll() {
        factionsManager.getPlayerFactions().forEach(this::savePlayer);
    }

    @Override
    public void loadAdmin(String id) {
        YamlFile file = new YamlFile(plugin, ADMIN_FACTIONS_DIRECTORY, id);
        YamlData data = new YamlData(file);

        AdminFaction adminFaction = new AdminFaction(id);
        adminFaction.read(data);

        factionsManager.createAdminFaction(adminFaction);
    }

    @Override
    public void loadAdminSaved() {
        List<String> adminFactions = FileUtils.getFilenamesInDirectory(plugin.getDataFolder(), ADMIN_FACTIONS_DIRECTORY);

        // Pre-load factions
        for(String id : adminFactions) {
            PlayerFaction playerFaction = new PlayerFaction(id);
            factionsManager.createPlayerFaction(playerFaction);
        }

        // Post-load faction data
        for(AdminFaction faction : factionsManager.getAdminFactions()) {
            YamlFile file = new YamlFile(plugin, ADMIN_FACTIONS_DIRECTORY, faction.getUniqueIdentifier());
            YamlData data = new YamlData(file);
            faction.read(data);
        }
    }

    @Override
    public void saveAdmin(AdminFaction faction) {
        YamlFile file = new YamlFile(plugin, ADMIN_FACTIONS_DIRECTORY, faction.getUniqueIdentifier());
        YamlData data = new YamlData(file);

        faction.write(data);
        data.save();
        data.close();
    }

    @Override
    public void saveAdminAll() {
        factionsManager.getAdminFactions().forEach(this::saveAdmin);
    }
}
