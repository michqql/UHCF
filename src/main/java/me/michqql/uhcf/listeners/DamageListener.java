package me.michqql.uhcf.listeners;

import me.michqql.core.util.AbstractListener;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Members;
import me.michqql.uhcf.player.PlayerData;
import me.michqql.uhcf.player.PlayerManager;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

public class DamageListener extends AbstractListener {

    private final FactionsManager factionsManager;
    private final PlayerManager playerManager;

    public DamageListener(Plugin plugin, FactionsManager factionsManager, PlayerManager playerManager) {
        super(plugin);
        this.factionsManager = factionsManager;
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity damagee = e.getEntity();

        // We don't care if the entity being damaged is not a player
        if(!(damagee instanceof Player defender))
            return;

        final PlayerFaction faction = factionsManager.getPlayerFactionByPlayer(defender.getUniqueId());
        final Members members = faction.getMembers();

        // Check #1: if the damager is a player, and they are in same faction
        if(damager instanceof Player attacker) {
            if(members.isInFaction(attacker.getUniqueId())) {
                e.setCancelled(true);
                return;
            }
        }

        // Check #2: if the damager is a projectile (arrow, etc...) and the shooter
        //           is in the same faction
        if(damager instanceof Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {

            if(members.isInFaction(shooter.getUniqueId())) {
                e.setCancelled(true);
                return;
            }
        }

        // Check #3: if the damager is a pet, and the owner is in the same faction
        if(damager instanceof Tameable tameable
                && tameable.isTamed()
                && tameable.getOwner() instanceof Player tamer) {

            if(members.isInFaction(tamer.getUniqueId())) {
                e.setCancelled(true);
                return; // future-proof
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

        // Increments the kills of the attacker
        EntityDamageEvent damageEvent = player.getLastDamageCause();
        if(damageEvent instanceof EntityDamageByEntityEvent damageByEntityEvent && !damageEvent.isCancelled()) {
            Entity attacker = damageByEntityEvent.getDamager();
            if(attacker instanceof Player playerAttacker) {
                PlayerData dataAttacker = playerManager.get(playerAttacker.getUniqueId());
                dataAttacker.incrementKills();
            }
        }
    }
}
