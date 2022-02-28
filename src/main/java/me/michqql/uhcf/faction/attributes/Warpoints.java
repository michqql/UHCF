package me.michqql.uhcf.faction.attributes;

import me.michqql.core.data.IData;
import me.michqql.core.data.IReadWrite;
import me.michqql.core.util.Pair;

import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Warpoints implements IReadWrite {

    private final HashMap<PlayerFaction, Integer> warpoints = new HashMap<>();
    private List<Pair<PlayerFaction, Integer>> cachedOrdered;

    @Override
    public void read(IData data) {
        FactionsManager factionsManager = UHCFPlugin.getInstance().getFactionsManager();

        for(String factionId : data.getKeys()) {
            PlayerFaction faction = factionsManager.getPlayerFactionById(factionId);
            if(faction == null) {
                // The faction war point evaded
                Bukkit.getLogger().warning("[UHCF] Error while loading Warpoints: invalid faction id: " + factionId);
                continue;
            }

            int amount = data.getInteger(factionId);
            setWarpoints(faction, amount);
        }
    }

    @Override
    public void write(IData data) {
        warpoints.forEach((faction, amount) -> data.set(faction.getUniqueIdentifier(), amount));
    }

    public boolean hasWarpoints(PlayerFaction faction) {
        return warpoints.containsKey(faction);
    }

    public int getWarpoints(PlayerFaction faction) {
        return warpoints.getOrDefault(faction, 0);
    }

    public boolean isPositive(PlayerFaction faction) {
        return getWarpoints(faction) > 0;
    }

    public boolean isNegative(PlayerFaction faction) {
        return getWarpoints(faction) < 0;
    }

    public void setWarpoints(PlayerFaction faction, int amount) {
        warpoints.put(faction, amount);
        cachedOrdered = null;
    }

    public void setWarpoints(PlayerFaction faction, Function<Integer, Integer> function) {
        int current = getWarpoints(faction);
        Integer result = function.apply(current);
        if(result == null)
            warpoints.remove(faction);
        else
            setWarpoints(faction, result);
    }

    public void increase(PlayerFaction faction) {
        setWarpoints(faction, integer -> ++integer);
    }

    public void decrease(PlayerFaction faction) {
        setWarpoints(faction, integer -> --integer);
    }

    public List<Pair<PlayerFaction, Integer>> getOrderedWarpoints(boolean ascending) {
        if(cachedOrdered != null)
            return cachedOrdered;

        List<Pair<PlayerFaction, Integer>> list = new ArrayList<>();
        warpoints.forEach((faction, integer) -> list.add(new Pair<>(faction, integer)));

        // Sort the values (descending)
        cachedOrdered = list.stream().sorted((o1, o2) -> {
            if(ascending)
                return o1.getValue() - o2.getValue();
            else
                return o2.getValue() - o1.getValue();
        }).collect(Collectors.toList());
        return cachedOrdered;
    }
}
