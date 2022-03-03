package com.rosstail.karma;

import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.entity.Player;

import java.util.Map;

public class KarmaAPI {

    /**
     * Returns the PlayerData of the selected player.
     * If the Player isn't in the list, he will be instanciated then added to list as long as the plugin is functionning
     * @param player
     * @return
     */
    public static PlayerData getSetPlayerData(Player player) {
        return PlayerDataManager.getSet(player);
    }
    /**
     * Returns the PlayerData of the selected player.
     * If the player isn't in the list, he will be instanciated.
     * @param player
     * @return
     */
    public static PlayerData getPlayerData(Player player) {
        return PlayerDataManager.getNoSet(player);
    }

    /**
     * Returns the tier depending of name.
     * If the tier doesn't exist, returns noTier.
     * If the tierName is null, returns null.
     * @param tierName
     * @return
     */
    public static Tier getTierByName(String tierName) {
        if (tierName == null) {
            return null;
        }
        return TierManager.getTierManager().getTier(tierName);
    }

    /**
     * Returns the map of listed PlayerData in the server
     * @return
     */
    public static Map<Player, PlayerData> getPlayerDataList() {
        return PlayerDataManager.getPlayerDataMap();
    }

    /**
     * Returns the map of listed Tiers in the server
     * @return
     */
    public static Map<String, Tier> getTierList() {
        return TierManager.getTierManager().getTiers();
    }
}
