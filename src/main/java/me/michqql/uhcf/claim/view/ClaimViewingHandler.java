package me.michqql.uhcf.claim.view;

import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.claim.AdminClaim;
import me.michqql.uhcf.claim.Claim;
import me.michqql.uhcf.claim.ClaimsManager;
import me.michqql.uhcf.faction.FactionsManager;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClaimViewingHandler {

    private final static HashMap<UUID, View> VIEWERS = new HashMap<>();

    public static boolean isViewing(UUID uuid) {
        return VIEWERS.containsKey(uuid);
    }

    public static void toggle(UHCFPlugin plugin, FactionsManager factionsManager, ClaimsManager claimsManager,
                              Player player) {

        View view = VIEWERS.remove(player.getUniqueId());
        if(view == null) {
            // Add player to new view
            VIEWERS.put(player.getUniqueId(), new View(plugin, factionsManager, claimsManager, player));
        } else {
            view.runnable.cancel();
        }
    }

    public static void setEnabled(UHCFPlugin plugin, FactionsManager factionsManager, ClaimsManager claimsManager,
                                  Player player, boolean enabled) {
        if(!enabled) {
            View view = VIEWERS.remove(player.getUniqueId());
            if(view != null)
                view.runnable.cancel();
        } else {
            VIEWERS.putIfAbsent(player.getUniqueId(), new View(plugin, factionsManager, claimsManager, player));
        }
    }

    public static void update(UUID uuid) {
        View view = VIEWERS.get(uuid);
        if(view != null)
            view.update();
    }

    private static class View {
        private final static int FORCE_FIELD_HEIGHT = 3;

        final FactionsManager factionsManager;
        final ClaimsManager claimsManager;

        final Player player;

        final BukkitTask runnable;

        Set<Location> locations;

        public View(UHCFPlugin plugin, FactionsManager factionsManager, ClaimsManager claimsManager, Player player) {
            this.factionsManager = factionsManager;
            this.claimsManager = claimsManager;
            this.player = player;
            update(); // sets up nearby claims
            this.runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    tick();
                }
            }.runTaskTimer(plugin, 0L, 10L);
        }

        void update() {
            final World world = player.getWorld();
            final Chunk playerChunk = player.getLocation().getChunk();

            // Get nearby claims around player based on view distance
            int viewDistance = world.getViewDistance();
            Set<Claim> nearby = claimsManager.getClaimsAroundCenter(playerChunk, viewDistance);

            this.locations = new HashSet<>();
            for(Claim claim : nearby) {
                int y = claim instanceof AdminClaim ? 1 : 0;
                for(Chunk chunk : claim.getClaimedChunksCopy()) {
                    boolean[] adjacent = hasAdjacentClaims(claim, chunk);
                    // X: 0 -> 15 (east to west)
                    for(int x = 0; x < 17; x++) {
                        // North
                        if(!adjacent[0]) {
                            locations.add(new Location(
                                    world,
                                    chunk.getX() * 16 + x,
                                    y,
                                    chunk.getZ() * 16
                            ));
                        }

                        // South
                        if(!adjacent[1]) {
                            locations.add(new Location(
                                    world,
                                    chunk.getX() * 16 + x,
                                    y,
                                    chunk.getZ() * 16 + 16
                            ));
                        }
                    }

                    // Z: 0 -> 15 (north to south)
                    for(int z = 0; z < 17; z++) {
                        // West
                        if(!adjacent[3]) {
                            locations.add(new Location(
                                    world,
                                    chunk.getX() * 16,
                                    y,
                                    chunk.getZ() * 16 + z
                            ));
                        }

                        // East
                        if(!adjacent[2]) {
                            locations.add(new Location(
                                    world,
                                    chunk.getX() * 16 + 16,
                                    y,
                                    chunk.getZ() * 16 + z
                            ));
                        }
                    }
                }
            }
        }

        private void tick() {
            if(locations == null)
                return;

            final double playerYLevel = player.getLocation().getY() + player.getEyeHeight();

            // Draw particles
            final double increment = 0.4;
            for (double y = -FORCE_FIELD_HEIGHT; y < FORCE_FIELD_HEIGHT; y += increment) {
                for(Location location : locations) {
                    boolean adminClaim = location.getY() == 1;

                    location.add(0, playerYLevel + y, 0);
                    player.spawnParticle(adminClaim ? Particle.TOTEM : Particle.END_ROD, location, 0, 0, 0, 0);
                    location.subtract(0, playerYLevel + y, 0);
                }
            }
        }

        private boolean[] hasAdjacentClaims(Claim claim, Chunk chunk) {
            final World world = chunk.getWorld();
            final int x = chunk.getX();
            final int z = chunk.getZ();

            final boolean[] adjacent = new boolean[4];

            // North
            Chunk north = world.getChunkAt(x, z - 1);
            adjacent[0] = claim.isClaimed(north) && isAdjacent(chunk, north);

            // South
            Chunk south = world.getChunkAt(x, z + 1);
            adjacent[1] = claim.isClaimed(south) && isAdjacent(chunk, south);

            // East
            Chunk east = world.getChunkAt(x + 1, z);
            adjacent[2] = claim.isClaimed(east) && isAdjacent(chunk, east);

            // West
            Chunk west = world.getChunkAt(x - 1, z);
            adjacent[3] = claim.isClaimed(west) && isAdjacent(chunk, west);

            return adjacent;
        }

        private boolean isAdjacent(Chunk c1, Chunk c2) {
            if(!c1.getWorld().equals(c2.getWorld()))
                return false;

            int x1 = c1.getX();
            int z1 = c1.getZ();

            int x2 = c2.getX();
            int z2 = c2.getZ();

            int dist = Math.abs(x1 - x2 + z1 - z2);
            return dist == 1;
        }
    }
}
