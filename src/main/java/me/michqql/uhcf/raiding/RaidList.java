package me.michqql.uhcf.raiding;

import me.michqql.uhcf.faction.PlayerFaction;

import java.util.List;

public record RaidList(PlayerFaction basedOn, List<Raid> raids) {

    public int getRaids() {
        return raids.size();
    }

    public boolean isDefending() {
        for (Raid raid : raids) {
            if (raid.isDefender(basedOn))
                return true;
        }
        return false;
    }

    public boolean isInvolved(PlayerFaction other) {
        for (Raid raid : raids) {
            if (raid.isInvolved(other))
                return true;
        }
        return false;
    }

    public Raid getInvolved(PlayerFaction other) {
        for (Raid raid : raids) {
            if (raid.isInvolved(other))
                return raid;
        }
        return null;
    }
}
