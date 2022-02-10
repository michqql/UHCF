package me.michqql.uhcf.data.faction;

import me.michqql.core.io.JsonFile;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.Faction;
import me.michqql.uhcf.faction.PlayerFaction;

public class JsonFactionLoader extends FactionLoader {

    public JsonFactionLoader(UHCFPlugin plugin) {
        super(plugin);
    }

    @Override
    public PlayerFaction loadPlayerFaction(String identifier) {
        JsonFile file = new JsonFile(plugin, "factions/player", identifier);

        //String creator
        return null;
    }

    @Override
    public AdminFaction loadAdminFaction(String identifier) {
        return null;
    }

    @Override
    public void save(Faction faction) {

    }
}
