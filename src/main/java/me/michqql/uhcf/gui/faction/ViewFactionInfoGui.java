package me.michqql.uhcf.gui.faction;

import me.michqql.core.gui.Gui;
import me.michqql.core.gui.GuiHandler;
import me.michqql.core.item.ItemBuilder;
import me.michqql.core.util.OfflineUUID;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Members;
import me.michqql.uhcf.faction.roles.FactionRole;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.*;

public class ViewFactionInfoGui extends Gui {

    private final static int INFORMATION_SLOT = 4;

    private final static int[] PLAYER_SLOTS = {
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35
    };

    private final PlayerFaction faction;

    public ViewFactionInfoGui(GuiHandler guiHandler, Player player, PlayerFaction faction) {
        super(guiHandler, player);
        this.faction = faction;

        build("&0 ", 6);
    }

    @Override
    protected void createInventory() {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("&eCreator &f" + OfflineUUID.getName(faction.getCreator()));
        lore.add("&eFormed &f5.1 days ago");
        lore.add("&eMembers &f" + faction.getMembers().getOnlinePlayers().size() + " / " + faction.getMembers().getSize());
        lore.add("&eClaims &f" + faction.getClaim().getNumberOfChunks() + " / 27");
        lore.add("");
        lore.add("&eUpkeep &f123 / 234");
        lore.add("&eUpkeep depletes &f2.5 days");
        lore.add("");
        lore.add("&eAllies");
        this.inventory.setItem(INFORMATION_SLOT, new ItemBuilder(Material.BOOK)
                .displayName("&3&l" + faction.getDisplayName()).lore(lore).getItem());

        // Players
        Members members = faction.getMembers();
        LinkedHashMap<OfflinePlayer, FactionRole> players = members.getPlayersSortedByRole();
        int index = 0;

        // Leader
        OfflinePlayer leader = Bukkit.getOfflinePlayer(members.getLeader());
        this.inventory.setItem(PLAYER_SLOTS[index], new ItemBuilder(Material.SKELETON_SKULL)
                .displayName("&c" + leader.getName()).lore(
                        "&eRole &f" + FactionRole.LEADER.getName()
                ).getItem());
        index++;

        // Members
        for (Map.Entry<OfflinePlayer, FactionRole> entry : players.entrySet()) {
            this.inventory.setItem(PLAYER_SLOTS[index], new ItemBuilder(Material.SKELETON_SKULL)
                    .displayName("&c" + entry.getKey().getName()).lore(
                            "&eRole &f" + entry.getValue().getName()
                    ).getItem());

            index++;
        }
    }

    @Override
    protected void updateInventory() {

    }

    @Override
    protected void onClose() {

    }

    @Override
    protected boolean onClickEvent(int i, ClickType clickType) {
        return true;
    }

    @Override
    protected boolean onPlayerInventoryClickEvent(int i, ClickType clickType) {
        return true;
    }
}
