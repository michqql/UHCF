package me.michqql.uhcf.worldevents.type;

import me.michqql.core.data.IData;
import me.michqql.core.item.ItemBuilder;

import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.player.PlayerData;
import me.michqql.uhcf.player.PlayerManager;
import me.michqql.uhcf.worldevents.WorldEvent;
import me.michqql.uhcf.worldevents.arena.Area;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class KOTH extends WorldEvent {

    private final static double ANIMATION_LENGTH = 5.0D;

    protected final FactionsManager factionsManager;
    protected final PlayerManager playerManager;

    protected final Area area = new Area();
    protected World world;
    protected Location rewardChestLocation;
    protected String displayName;
    protected int timeToCapture;

    protected boolean resetOnAreaLeave = true;
    protected boolean resetOnDifferentFaction = true;
    protected boolean pauseOnMultipleFactions = true;

    // Current session
    private boolean captured;
    protected int timeRemaining;
    protected Player capturingPlayer;

    private Material sessionPreviousMat;
    private BlockData sessionPreviousData;

    public KOTH(UHCFPlugin plugin, FactionsManager factionsManager, PlayerManager playerManager, World world) {
        super(plugin, "koth", 20);
        this.factionsManager = factionsManager;
        this.playerManager = playerManager;
        this.world = world;
        this.displayName = "King of the Hill";
    }

    @Override
    protected void start() {
        this.captured = false;
        this.timeRemaining = timeToCapture;
        this.capturingPlayer = null;
    }

    @Override
    protected void tick() {
        if(captured)
            return;

        List<Player> inside = area.getPlayersInside(world);

        // Less than one player inside area
        if(inside.size() == 0) {
            this.capturingPlayer = null;
            if(resetOnAreaLeave)
                timeRemaining = timeToCapture;
            return;
        }

        // More than one player inside area
        if(inside.size() > 1 && pauseOnMultipleFactions) {
            PlayerFaction faction = factionsManager.getPlayerFactionByPlayer(inside.get(0).getUniqueId());
            if(faction == null) {
                // If the first player's faction is null, then it isn't possible for all the players to be
                // in the same faction, as this player is not in a faction.
                this.capturingPlayer = null;
                return;
            } else {
                for (int i = 1; i < inside.size(); i++) {
                    Player player = inside.get(i);
                    if(!faction.getMembers().isInFaction(player.getUniqueId())) {
                        this.capturingPlayer = null;
                        return;
                    }
                }
            }
        }

        // Exactly one player inside of region
        Player player = inside.get(0);
        if(!player.equals(capturingPlayer)) {
            capturingPlayer = player;
            if(resetOnDifferentFaction) {
                timeRemaining = timeToCapture;
                return;
            }
        }

        timeRemaining--;

        // Check if KOTH has been captured
        if(timeRemaining <= 0) {
            captured = true;

            PlayerData data = playerManager.get(capturingPlayer.getUniqueId());
            data.computeCustomStatistic("koth-captures", (s, o) -> {
                int caps = 0;
                if(o instanceof Integer)
                    caps = (int) o;

                return ++caps;
            });

            // Reward player
            startRewardProcess();
        }
    }

    @Override
    protected void end() {
        resetRewardChest();
    }

    @Override
    public void read(IData data) {
        String worldString = data.getString("world");
        this.world = Bukkit.getWorld(worldString);
        if(world == null) {
            Bukkit.getLogger().warning("[UHCF] Error while loading KOTH event, world does not exist: " + worldString);
            return;
        }

        IData rewardLocation = data.getSection("reward-location");
        int x = rewardLocation.getInteger("x");
        int y = rewardLocation.getInteger("y");
        int z = rewardLocation.getInteger("z");
        this.rewardChestLocation = new Location(world, x, y, z);

        this.displayName = data.getString("display-name");
        this.timeToCapture = data.getInteger("time-to-capture");

        IData settings = data.getSection("settings");
        this.resetOnAreaLeave = settings.getBoolean("reset-timer-on-zero-players");
        this.resetOnDifferentFaction = settings.getBoolean("reset-timer-on-different-faction");
        this.pauseOnMultipleFactions = settings.getBoolean("pause-timer-on-multiple-factions");

        area.read(data.getSection("area"));
    }

    @Override
    public void write(IData data) {
        data.set("world", world.getName());

        IData rewardLocation = data.createSection("reward-location");
        rewardLocation.set("x", rewardChestLocation.getBlockX());
        rewardLocation.set("y", rewardChestLocation.getBlockY());
        rewardLocation.set("z", rewardChestLocation.getBlockZ());

        data.set("display-name", displayName);
        data.set("time-to-capture", timeToCapture);

        IData settings = data.createSection("settings");
        settings.set("reset-timer-on-zero-players", resetOnAreaLeave);
        settings.set("reset-timer-on-different-faction", resetOnDifferentFaction);
        settings.set("pause-timer-on-multiple-factions", pauseOnMultipleFactions);

        area.write(data.createSection("area"));
    }

    public Area getArea() {
        return area;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Session based data
    public boolean isCaptured() {
        return captured;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public Player getCapturingPlayer() {
        return capturingPlayer;
    }

    private void startRewardProcess() {
        final double blocksPerPeriod = (world.getMaxHeight() - rewardChestLocation.getBlockY()) / ANIMATION_LENGTH / 4;

        new BukkitRunnable() {
            final Location location = new Location(
                    world,
                    rewardChestLocation.getBlockX() + 0.5D,
                    world.getMaxHeight(),
                    rewardChestLocation.getBlockZ() + 0.5D
            );

            @Override
            public void run() {
                // Spawn firework effect (handles sound too)
                Firework firework = (Firework) world.spawnEntity(location, EntityType.FIREWORK);
                FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder().withColor(Color.WHITE).with(FireworkEffect.Type.BALL_LARGE).build());
                meta.setPower(1);
                firework.setFireworkMeta(meta);
                firework.detonate();

                if(location.subtract(0, blocksPerPeriod, 0).getY() < rewardChestLocation.getY()) {
                    // Spawn in chest
                    // Fill with loot
                    // Cancel this runnable
                    Block block = world.getBlockAt(rewardChestLocation);

                    sessionPreviousMat = block.getType();
                    sessionPreviousData = block.getBlockData();

                    block.setType(Material.CHEST);

                    Chest chest = (Chest) block.getState();
                    final Inventory inventory = chest.getBlockInventory();
                    inventory.setContents(new ItemStack[]{
                            new ItemBuilder(Material.DIAMOND).getItem()
                    });
                    chest.update();

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if(block.getState() instanceof Chest)
                            resetRewardChest();
                    }, 20 * 30);

                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void resetRewardChest() {
        Block block = world.getBlockAt(rewardChestLocation);

        if(block.getState() instanceof Chest chest) {
            chest.getBlockInventory().getViewers().forEach(HumanEntity::closeInventory);
            ItemStack[] items = chest.getBlockInventory().getContents();
            for (ItemStack item : items) {
                if(item == null)
                    continue;

                world.dropItemNaturally(rewardChestLocation, item);
            }

            block.setType(sessionPreviousMat);
            block.setBlockData(sessionPreviousData);
        }
    }
}
