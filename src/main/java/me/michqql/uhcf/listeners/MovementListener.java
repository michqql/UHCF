package me.michqql.uhcf.listeners;

import me.michqql.core.io.CommentFile;
import me.michqql.core.util.AbstractListener;
import me.michqql.core.util.MessageHandler;
import me.michqql.uhcf.UHCFPlugin;
import me.michqql.uhcf.claim.Claim;
import me.michqql.uhcf.claim.ClaimsManager;
import me.michqql.uhcf.claim.view.ClaimViewingHandler;
import me.michqql.uhcf.events.PlayerChunkChangeEvent;
import me.michqql.uhcf.faction.AdminFaction;
import me.michqql.uhcf.faction.Faction;
import me.michqql.uhcf.faction.FactionsManager;
import me.michqql.uhcf.faction.PlayerFaction;
import me.michqql.uhcf.faction.attributes.Relations;
import me.michqql.uhcf.faction.roles.FactionRole;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashMap;

public class MovementListener extends AbstractListener {

    // Config
    private final int fadeInTicks, stayTicks, fadeOutTicks;
    private final String topText, subText, wildernessName;

    private final FactionsManager factionsManager;
    private final ClaimsManager claimsManager;

    public MovementListener(UHCFPlugin plugin, CommentFile config,
                            FactionsManager factionsManager, ClaimsManager claimsManager) {
        super(plugin);
        this.factionsManager = factionsManager;
        this.claimsManager = claimsManager;

        // Load config
        FileConfiguration f = config.getConfig();

        this.fadeInTicks = f.getInt("chunk-change-message.fade-in-ticks", 10);
        this.stayTicks = f.getInt("chunk-change-message.stay-ticks", 10);
        this.fadeOutTicks = f.getInt("chunk-change-message.fade-out-ticks", 10);

        String topText = f.getString("chunk-change-message.top-text", "Entering");
        if(topText.isEmpty())
            topText = " ";
        this.topText = topText;

        String bottomText = f.getString("chunk-change-message.bottom-text", "%faction%");
        if(bottomText.isEmpty())
            bottomText = " ";
        this.subText = bottomText;

        this.wildernessName = f.getString("chunk-change-message.wilderness-name", "&2Wilderness");
    }

    @EventHandler
    public void onChunkChange(PlayerChunkChangeEvent e) {
        // Displays claims to admins that have /admin viewclaims toggled on
        ClaimViewingHandler.update(e.getPlayer().getUniqueId());

        // Send title message
        Player player = e.getPlayer();
        Claim from = claimsManager.getClaimByChunk(e.getFrom());
        Claim to = claimsManager.getClaimByChunk(e.getTo());

        // Claim -> Claim
        if(from != null && to != null) {
            // Same claim
            if(from.getOwningFaction().equals(to.getOwningFaction()))
                return;

            // Different claim
            sendTitle(player, to);
        }
        else if(to != null) {
            sendTitle(player, to);

        }
        else if(from != null) {
            sendTitle(player);
        }
    }

    private void sendTitle(Player player, Claim claim) {
        final String colour = getRelationshipColour(player, claim);
        final Faction owner = claim.getOwningFaction();

        HashMap<String, String> placeholders = new HashMap<>(){{
            put("relation.colour", colour);
            put("relation.color", colour);
            put("relation.col", colour);
            put("rel.colour", colour);
            put("rel.color", colour);
            put("rel.col", colour);
            put("faction", owner.getDisplayName());
            put("faction.name", owner.getDisplayName());
            put("faction.id", owner.getUniqueIdentifier());
        }};

        player.sendTitle(
                MessageHandler.replacePlaceholdersStatic(topText, placeholders),
                MessageHandler.replacePlaceholdersStatic(subText, placeholders),
                fadeInTicks,
                stayTicks,
                fadeOutTicks
        );
    }

    private void sendTitle(Player player) {
        HashMap<String, String> placeholders = new HashMap<>(){{
            put("relation.colour", "");
            put("relation.color", "");
            put("relation.col", "");
            put("rel.colour", "");
            put("rel.color", "");
            put("rel.col", "");
            put("faction", wildernessName);
            put("faction.name", wildernessName);
            put("faction.id", wildernessName);
        }};

        player.sendTitle(
                MessageHandler.replacePlaceholdersStatic(topText, placeholders),
                MessageHandler.replacePlaceholdersStatic(subText, placeholders),
                fadeInTicks,
                stayTicks,
                fadeOutTicks
        );
    }

    private String getRelationshipColour(Player player, Claim claim) {
        Faction owner = claim.getOwningFaction();
        PlayerFaction faction = factionsManager.getPlayerFactionByPlayer(player.getUniqueId());

        if(owner instanceof AdminFaction)
            return "";

        if(faction == null)
            return FactionRole.NONE.getPrefix();

        // Player is in that faction
        if (faction.equals(owner))
            return FactionRole.MEMBER.getPrefix();

        // Is player in ally or truce faction
        PlayerFaction playerOwner = (PlayerFaction) owner;
        Relations relations = playerOwner.getRelations();

        if(relations.isAlly(faction))
            return FactionRole.ALLY.getPrefix();

        if(relations.isTruce(faction))
            return FactionRole.TRUCE.getPrefix();

        return FactionRole.NONE.getPrefix();
    }
}
