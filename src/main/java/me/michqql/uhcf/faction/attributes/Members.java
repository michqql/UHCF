package me.michqql.uhcf.faction.attributes;

import me.michqql.core.data.IData;
import me.michqql.core.data.IReadWrite;
import me.michqql.uhcf.faction.roles.FactionRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class Members implements IReadWrite {

    private UUID leader;
    private final HashMap<UUID, FactionRole> members = new HashMap<>();

    @Override
    public void read(IData data) {
        String leaderUuidString = data.getString("leader");
        UUID leaderUuid;
        try {
            leaderUuid = UUID.fromString(leaderUuidString);
        } catch(IllegalArgumentException e) {
            Bukkit.getLogger().warning("[UHCF] Error while loading Members: malformed leader uuid");
            throw e;
        }

        setLeader(leaderUuid);

        IData memberData = data.getSection("members");
        for(String memberUuidString : memberData.getKeys()) {
            UUID memberUuid;
            try {
                memberUuid = UUID.fromString(memberUuidString);
            } catch(IllegalArgumentException e) {
                Bukkit.getLogger().warning("[UHCF] Error while loading Members: malformed member uuid");
                throw e;
            }

            String roleString = memberData.getString(memberUuidString);
            FactionRole role = FactionRole.valueOf(roleString);

            addMember(memberUuid);
            setFactionRole(memberUuid, role);
        }
    }

    @Override
    public void write(IData data) {
        data.set("leader", leader.toString());

        IData memberData = data.createSection("members");
        members.forEach((memberUuid, factionRole) ->
                memberData.set(memberUuid.toString(), factionRole.name()));
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public boolean isLeader(UUID uuid) {
        return leader.equals(uuid);
    }

    public Set<UUID> getMembers() {
        return members.keySet();
    }

    public int getSize() {
        // Leader counts as a member, so add 1
        return 1 + members.size();
    }

    public boolean isInFaction(UUID uuid) {
        return leader.equals(uuid) || members.containsKey(uuid);
    }

    public void addMember(UUID uuid) {
        members.put(uuid, FactionRole.RECRUIT);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public FactionRole getFactionRole(UUID uuid) {
        if(isLeader(uuid))
            return FactionRole.LEADER;

        return members.getOrDefault(uuid, FactionRole.NONE);
    }

    public void setFactionRole(UUID uuid, FactionRole role) {
        if(!isInFaction(uuid) || !role.isGuildRole())
            return;

        // If set to leader, make old leader admin
        if(role == FactionRole.LEADER) {
            setFactionRole(leader, FactionRole.LEADER.getDemotion());
            setLeader(uuid);
            members.remove(uuid);
        } else {
            members.put(uuid, role);
        }
    }

    public List<Player> getOnlinePlayers() {
        List<Player> online = new ArrayList<>();

        Player leader = Bukkit.getPlayer(this.leader);
        if(leader != null)
            online.add(leader);

        for(UUID uuid : getMembers()) {
            Player player = Bukkit.getPlayer(uuid);
            if(player != null)
                online.add(player);
        }

        return online;
    }

    public List<OfflinePlayer> getPlayers() {
        List<OfflinePlayer> players = new ArrayList<>();

        players.add(Bukkit.getOfflinePlayer(leader));

        for(UUID uuid : getMembers()) {
            players.add(Bukkit.getOfflinePlayer(uuid));
        }

        return players;
    }

    public LinkedHashMap<OfflinePlayer, FactionRole> getPlayersSortedByRole() {
        LinkedHashMap<OfflinePlayer, FactionRole> result = new LinkedHashMap<>();
        members.entrySet().stream().sorted(Comparator.comparingInt(e -> e.getValue().getWeight()))
                .forEachOrdered(entry -> result.put(Bukkit.getOfflinePlayer(entry.getKey()), entry.getValue()));
        return result;
    }
}
