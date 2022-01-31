package me.michqql.uhcf.faction.attributes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class Members {

    private UUID owner;
    private final Set<UUID> members = new HashSet<>();

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public int getSize() {
        // Leader counts as a member, so add 1
        return 1 + members.size();
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public List<Player> getOnlinePlayers() {
        List<Player> online = new ArrayList<>();

        Player leader = Bukkit.getPlayer(owner);
        if(leader != null)
            online.add(leader);

        for(UUID uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if(player != null)
                online.add(player);
        }

        return online;
    }
}
