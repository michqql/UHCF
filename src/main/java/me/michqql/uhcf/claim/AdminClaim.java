package me.michqql.uhcf.claim;

import me.michqql.uhcf.faction.AdminFaction;
import org.bukkit.Chunk;

import java.util.HashSet;

public class AdminClaim extends Claim {

    private final AdminFaction owner;

    public AdminClaim(AdminFaction owner) {
        super(new HashSet<>());
        this.owner = owner;
    }

    public AdminFaction getAdminFactionOwner() {
        return owner;
    }

    public void claim(Chunk chunk) {
        chunks.add(chunk);
    }

    public void unclaim(Chunk chunk) {
        chunks.remove(chunk);
    }
}
