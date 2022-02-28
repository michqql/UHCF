package me.michqql.uhcf.gui.faction;

import me.michqql.core.gui.Gui;
import me.michqql.core.gui.GuiHandler;
import me.michqql.core.item.ItemBuilder;
import me.michqql.core.util.OfflineUUID;
import me.michqql.core.util.Pair;
import me.michqql.core.util.text.CenteredText;

import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Members;
import me.michqql.uhcf.faction.attributes.Relations;
import me.michqql.uhcf.faction.roles.FactionRole;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ViewFactionInfoGui extends Gui {

    private final static String FACTION_KEY = "warpoint";

    private final static int BACK_SLOT = 0;
    private final static int INFORMATION_SLOT = 4;

    private final static int[] PLAYER_SLOTS = {
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    };

    private final static int[] WARPOINT_POSITIVE_SLOTS = {
            45, 46, 47, 48
    };

    private final static int[] WARPOINT_NEGATIVE_SLOTS = {
            50, 51, 52, 53
    };

    private final FactionsManager factionsManager;
    private final PlayerFaction faction;
    private final boolean hasBackButton;

    public ViewFactionInfoGui(GuiHandler guiHandler, Player player,
                              FactionsManager factionsManager, PlayerFaction faction) {
        super(guiHandler, player);
        this.factionsManager = factionsManager;
        this.faction = faction;
        this.hasBackButton = false;

        String prefix = Relations.getRelation(player,
                factionsManager.getPlayerFactionByPlayer(player.getUniqueId()), faction).getPrefix();
        build(CenteredText.forGuiTitle(prefix + faction.getDisplayName()), 6);
    }

    public ViewFactionInfoGui(GuiHandler guiHandler, Player player,
                              FactionsManager factionsManager, PlayerFaction faction, boolean hasBackButton) {
        super(guiHandler, player);
        this.factionsManager = factionsManager;
        this.faction = faction;
        this.hasBackButton = hasBackButton;

        String prefix = Relations.getRelation(player,
                factionsManager.getPlayerFactionByPlayer(player.getUniqueId()), faction).getPrefix();
        build(CenteredText.forGuiTitle(prefix + faction.getDisplayName()), 6);
    }

    @Override
    protected void createInventory() {
        updateInventory();
    }

    @Override
    protected void updateInventory() {
        // Back button
        if(hasBackButton) {
            this.inventory.setItem(BACK_SLOT, new ItemBuilder(Material.RED_BED)
                    .displayName("&7< Go back").getItem());
        }

        // Information
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("&eCreator &f" + OfflineUUID.getName(faction.getCreator()));
        lore.add("&eFormed &f5.1 days ago");
        lore.add("&eMembers &f" + faction.getMembers().getOnlinePlayers().size() + " / " + faction.getMembers().getSize());
        lore.add("&eClaims &f" + faction.getClaim().getNumberOfChunks() + " / 27");
        lore.add("");
        lore.add("&eUpkeep &f123 / 234");
        lore.add("&eUpkeep depletes &f2.5 days");

        Relations relations = faction.getRelations();
        if(!relations.getAlliances().isEmpty()) {
            lore.add("");
            lore.add("&eAllies");
            lore.add(" " + FactionRole.ALLY.getPrefix() + relations.getAlliances().toString());
        }

        if(!relations.getTruces().isEmpty()) {
            lore.add("");
            lore.add("&eTruces");
            lore.add(" " + FactionRole.TRUCE.getPrefix() + relations.getTruces().toString());
        }
        this.inventory.setItem(INFORMATION_SLOT, new ItemBuilder(Material.BOOK)
                .displayName("&3&l" + faction.getDisplayName()).lore(lore).getItem());

        // Players
        Members members = faction.getMembers();
        LinkedHashMap<OfflinePlayer, FactionRole> players = members.getPlayersSortedByRole();
        int index = 0;

        // Leader
        OfflinePlayer leader = Bukkit.getOfflinePlayer(members.getLeader());
        this.inventory.setItem(PLAYER_SLOTS[index], getSkullItem(leader, FactionRole.LEADER).getItem());
        index++;

        // Members
        for (Map.Entry<OfflinePlayer, FactionRole> entry : players.entrySet()) {
            this.inventory.setItem(PLAYER_SLOTS[index], getSkullItem(entry.getKey(), entry.getValue()).getItem());
            index++;
        }

        // Warpoints
        List<Pair<PlayerFaction, Integer>> ordered = faction.getWarpoints().getOrderedWarpoints(false);

        // Positive
        for(int i = 0; i < WARPOINT_POSITIVE_SLOTS.length; i++) {
            if(i >= ordered.size())
                break;

            Pair<PlayerFaction, Integer> pair = ordered.get(i);
            if(pair.getValue() <= 0)
                break;

            int value = Math.abs(pair.getValue());
            this.inventory.setItem(WARPOINT_POSITIVE_SLOTS[i], new ItemBuilder(Material.LIME_DYE)
                    .displayName("&a" + pair.getKey().getDisplayName())
                    .lore("", "&eWar Points &f+" + value)
                    .amount(value)
                    .persistentData(
                            plugin, FACTION_KEY, PersistentDataType.STRING,
                            pair.getKey().getUniqueIdentifier()
                    ).getItem());
        }

        // Negative
        for(int i = 0; i < WARPOINT_NEGATIVE_SLOTS.length; i++) {
            if(i >= ordered.size())
                break;

            Pair<PlayerFaction, Integer> pair = ordered.get(ordered.size() - 1 - i);
            if(pair.getValue() >= 0)
                break;

            int value = Math.abs(pair.getValue());
            this.inventory.setItem(WARPOINT_NEGATIVE_SLOTS[WARPOINT_NEGATIVE_SLOTS.length - 1 - i], new ItemBuilder(Material.RED_DYE)
                    .displayName("&c" + pair.getKey().getDisplayName())
                    .lore("", "&eWar Points &f-" + value)
                    .amount(value)
                    .persistentData(
                            plugin, FACTION_KEY, PersistentDataType.STRING,
                            pair.getKey().getUniqueIdentifier()
                    ).getItem());
        }
    }

    @Override
    protected void onClose() {

    }

    @Override
    protected boolean onClickEvent(int slot, ClickType clickType) {
        if(hasBackButton) {
            if(slot == BACK_SLOT)
                guiHandler.openPreviousGui(player);
            return true;
        }

        ItemStack item = this.inventory.getItem(slot);
        if(item == null)
            return true;

        ItemMeta meta = item.getItemMeta();
        if(meta == null)
            return true;

        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

        // View warpoint faction information functionality
        if(ItemBuilder.hasPersistentData(plugin, dataContainer, FACTION_KEY, PersistentDataType.STRING)) {
            String factionId = ItemBuilder.getPersistentData(plugin, dataContainer, FACTION_KEY, PersistentDataType.STRING);
            PlayerFaction playerFaction = factionsManager.getPlayerFactionById(factionId);

            new ViewFactionInfoGui(guiHandler, player, factionsManager, playerFaction, true).openGui();
            return true;
        }

        return true;
    }

    @Override
    protected boolean onPlayerInventoryClickEvent(int i, ClickType clickType) {
        return true;
    }

    private ItemBuilder getSkullItem(OfflinePlayer player, FactionRole role) {
        Material material = player.isOnline() ? Material.PLAYER_HEAD : Material.SKELETON_SKULL;
        return new ItemBuilder(material)
                .displayName((player.isOnline() ? "&a" : "&c") + player.getName())
                .lore("&eRole &f" + role.getName())
                .meta(meta -> {
                    if(meta == null)
                        return;

                    if(!(meta instanceof SkullMeta skullMeta))
                        return;

                    skullMeta.setOwningPlayer(player);
                });
    }
}
