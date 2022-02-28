package me.michqql.uhcf.listeners;

import me.michqql.core.util.AbstractListener;
import me.michqql.core.util.MessageHandler;
import me.michqql.core.util.Placeholder;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Members;
import me.michqql.uhcf.player.PlayerData;
import me.michqql.uhcf.player.PlayerManager;
import me.michqql.uhcf.raiding.Raid;
import me.michqql.uhcf.raiding.RaidManager;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public class DamageListener extends AbstractListener {

    private final MessageHandler messageHandler;
    private final FactionsManager factionsManager;
    private final PlayerManager playerManager;
    private final RaidManager raidManager;

    public DamageListener(Plugin plugin, MessageHandler messageHandler,
                          FactionsManager factionsManager, PlayerManager playerManager, RaidManager raidManager) {
        super(plugin);
        this.messageHandler = messageHandler;
        this.factionsManager = factionsManager;
        this.playerManager = playerManager;
        this.raidManager = raidManager;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity damagee = e.getEntity();

        // We don't care if the entity being damaged is not a player
        if(!(damagee instanceof Player defender))
            return;

        final PlayerFaction faction = factionsManager.getPlayerFactionByPlayer(defender.getUniqueId());
        if(faction == null)
            return;

        final Members members = faction.getMembers();

        // Check #1: if the damager is a player, and they are in same faction
        // Check #2: if the damager and damaged are allies (but not truces)
        if(damager instanceof Player attacker) {
            if(members.isInFaction(attacker.getUniqueId())) {
                e.setCancelled(true);
                return;
            }

            PlayerFaction attackerFaction = factionsManager.getPlayerFactionByPlayer(attacker.getUniqueId());
            if(attackerFaction != null && faction.getRelations().isAlly(attackerFaction)) {
                e.setCancelled(true);
                return;
            }
        }

        // Check #3: if the damager is a projectile (arrow, etc...) and the shooter
        //           is in the same faction
        if(damager instanceof Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {

            if(members.isInFaction(shooter.getUniqueId())) {
                e.setCancelled(true);
                return;
            }
        }

        // Check #4: if the damager is a pet, and the owner is in the same faction
        if(damager instanceof Tameable tameable
                && tameable.isTamed()
                && tameable.getOwner() instanceof Player tamer) {

            if(members.isInFaction(tamer.getUniqueId())) {
                e.setCancelled(true);
                // In the future, if adding more code,
                // don't forget to return from this guard clause
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        PlayerData data = playerManager.get(player.getUniqueId());

        // Increments the deaths of the dead player
        if(data != null)
            data.incrementDeaths();

        EntityDamageEvent damageEvent = player.getLastDamageCause();
        if(damageEvent instanceof EntityDamageByEntityEvent damageByEntityEvent && !damageEvent.isCancelled()) {
            Entity attacker = damageByEntityEvent.getDamager();
            if(attacker instanceof Player playerAttacker) {
                // Death messages
                e.setDeathMessage(null);
                messageHandler.sendList(playerAttacker, "player-death.killer",
                        Placeholder.of("player", player.getName()));

                messageHandler.sendList(player, "player-death.dead",
                        Placeholder.of("player", playerAttacker.getName()));

                // Faction warpoints
                handleWarpoints(playerAttacker, player);

                // Player stats
                PlayerData dataAttacker = playerManager.get(playerAttacker.getUniqueId());
                dataAttacker.incrementKills();
            }
        }
    }

    private void handleWarpoints(Player attacker, Player defender) {
        PlayerFaction attackerFaction = factionsManager.getPlayerFactionByPlayer(attacker.getUniqueId());
        PlayerFaction defenderFaction = factionsManager.getPlayerFactionByPlayer(defender.getUniqueId());
        if(attackerFaction == null || defenderFaction == null)
            return;

        // If they are already raiding each other, return
        Raid raid = raidManager.getRaidByFactions(attackerFaction, defenderFaction);
        if(raid != null)
            return;

        attackerFaction.getWarpoints().increase(defenderFaction);
        defenderFaction.getWarpoints().decrease(attackerFaction);

        // Send messages
        int warpoints = attackerFaction.getWarpoints().getWarpoints(defenderFaction);
        messageHandler.sendList(attacker, "player-death.warpoints", new HashMap<>(){{
            put("your.faction", attackerFaction.getDisplayName());
            put("your.faction.name", attackerFaction.getDisplayName());
            put("other.faction", defenderFaction.getDisplayName());
            put("other.faction.name", defenderFaction.getDisplayName());
            put("positive.colour", (warpoints > 0 ? "&a" : (warpoints < 0 ? "&c" : "&f")));
            put("positive.color", (warpoints > 0 ? "&a" : (warpoints < 0 ? "&c" : "&f")));
            put("positive.sign", (warpoints > 0 ? "+" : ""));
            put("warpoints", String.valueOf(warpoints));
        }});

        messageHandler.sendList(defender, "player-death.warpoints", new HashMap<>(){{
            put("your.faction", defenderFaction.getDisplayName());
            put("your.faction.name", defenderFaction.getDisplayName());
            put("other.faction", attackerFaction.getDisplayName());
            put("other.faction.name", attackerFaction.getDisplayName());
            put("positive.colour", (warpoints > 0 ? "&c" : (warpoints < 0 ? "&a" : "&f")));
            put("positive.color", (warpoints > 0 ? "&c" : (warpoints < 0 ? "&a" : "&f")));
            put("positive.sign", (warpoints > 0 ? "+" : ""));
            put("warpoints", String.valueOf(-warpoints));
        }});

        // Check if raidable
        if(!raidManager.isAutomaticRaiding()) {
            // Send start raid command message
            messageHandler.sendList(attacker, "player-death.raidable", new HashMap<>(){{
                put("faction", defenderFaction.getDisplayName());
                put("faction.name", defenderFaction.getDisplayName());
            }});
            return;
        }

        boolean started = raidManager.startRaid(attackerFaction, defenderFaction);
        if(!started)
            return;

        // Send messages to all online faction members
        // Attackers
        HashMap<String, String> placeholdersAttackers = new HashMap<>(){{
            put("faction", defenderFaction.getDisplayName());
            put("faction.name", defenderFaction.getDisplayName());
        }};
        attackerFaction.getMembers().getOnlinePlayers().forEach(online ->
                messageHandler.sendList(online, "raid.started.raiders", placeholdersAttackers));

        // Defenders
        HashMap<String, String> placeholdersDefenders = new HashMap<>(){{
            put("faction", attackerFaction.getDisplayName());
            put("faction.name", attackerFaction.getDisplayName());
        }};
        defenderFaction.getMembers().getOnlinePlayers().forEach(online ->
                messageHandler.sendList(online, "raid.started.defenders", placeholdersDefenders));
    }
}
