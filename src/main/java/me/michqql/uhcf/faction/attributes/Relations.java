package me.michqql.uhcf.faction.attributes;

import me.michqql.uhcf.faction.PlayerFaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Relations {

    public enum Relation {
        NONE, TRUCE, ALLY
    }

    private final HashMap<PlayerFaction, Relation> relations = new HashMap<>();

    public boolean isFriendly(PlayerFaction other) {
        return getRelation(other) != Relation.NONE;
    }

    public boolean isTruce(PlayerFaction other) {
        return getRelation(other) == Relation.TRUCE;
    }

    public boolean isAlly(PlayerFaction other) {
        return getRelation(other) == Relation.ALLY;
    }

    public Relation getRelation(PlayerFaction other) {
        return relations.getOrDefault(other, Relation.NONE);
    }

    public Set<PlayerFaction> getAlliances() {
        Set<PlayerFaction> result = new HashSet<>();
        relations.forEach((playerFaction, relation) -> {
            if(relation == Relation.ALLY)
                result.add(playerFaction);
        });
        return result;
    }

    public Set<PlayerFaction> getTruces() {
        Set<PlayerFaction> result = new HashSet<>();
        relations.forEach((playerFaction, relation) -> {
            if(relation == Relation.TRUCE)
                result.add(playerFaction);
        });
        return result;
    }

    public void setRelation(PlayerFaction other, Relation relation) {
        if(relation == null || relation == Relation.NONE)
            relations.remove(other);
        else
            relations.put(other, relation);
    }
}
