package com.rosstail.karma.tiers;

import com.rosstail.karma.Karma;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.datas.PlayerDataManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TierManager {

    private static TierManager tierManager;
    private final Map<String, Tier> tiers = new HashMap<>();
    private static Tier noTier;

    public static void initTierManager(Karma plugin) {
        if (tierManager == null) {
            tierManager = new TierManager(plugin);
        }
    }

    TierManager(Karma plugin) {
        FileConfiguration config = plugin.getCustomConfig();

        noTier = new Tier(config.getString("tiers.none-display"), config.getString("tiers.none-short-display"));
        config.getConfigurationSection("tiers.list").getKeys(false).forEach(tierID -> {
            ConfigurationSection tierConfigSection = config.getConfigurationSection("tiers.list." + tierID);
            if (tierConfigSection != null) {
                tiers.put(tierID, new Tier(tierConfigSection, tierID));
            }
        });
        for (Tier tier : tiers.values()) {
            tier.initScores(this);
        }
    }

    public static TierManager getTierManager() {
        return tierManager;
    }

    public Map<String, Tier> getTiers() {
        return tiers;
    }

    public Tier getTier(String tierName) {
        if (tierName != null && tiers.containsKey(tierName)) {
            return tiers.get(tierName);
        } else {
            return noTier;
        }
    }

    public Tier getTierReplacement(Player player, String tierName) {
        if (getTier(tierName) == noTier) {
            double karma = PlayerDataManager.getPlayerDataMap().get(player).getKarma();
            for (Map.Entry<String, Tier> entry : tiers.entrySet()) {
                Tier tier = entry.getValue();
                if (tier.getMinKarma() <= karma && tier.getMaxKarma() >= karma) {
                    return tier;
                }
            }
        }
        return noTier;
    }

    public static Tier getNoTier() {
        return noTier;
    }
}
