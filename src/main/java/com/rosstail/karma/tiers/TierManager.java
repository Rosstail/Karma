package com.rosstail.karma.tiers;

import com.rosstail.karma.Karma;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.datas.PlayerDataManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TierManager {
    private final Karma plugin;
    private static TierManager tierManager;
    private final Map<String, Tier> tiers = new HashMap<>();
    private static Tier noTier;

    public static void initTierManager(Karma plugin) {
        if (tierManager == null) {
            tierManager = new TierManager(plugin);
        }
    }

    TierManager(Karma plugin) {
        this.plugin = plugin;
    }

    public void setupTiers() {
        FileConfiguration config = plugin.getCustomConfig();
        Set<String> configTiers = config.getConfigurationSection("tiers.list").getKeys(false);

        for (Map.Entry<String, Tier> entry : tiers.entrySet()) { //Check and remove tiers that do not exist anymore
            String s = entry.getKey();
            ConfigurationSection tierConfigSection = config.getConfigurationSection("tiers.list." + s);
            if (tierConfigSection == null) {
                tiers.remove(s);
            }
        }

        configTiers.forEach(tierID -> {
            ConfigurationSection tierConfigSection = config.getConfigurationSection("tiers.list." + tierID);
            if (tierConfigSection != null) {
                if (tiers.containsKey(tierID)) { //Just update
                    tiers.get(tierID).init(tierConfigSection);
                } else { //create and update
                    Tier tier = new Tier(tierID);
                    tier.init(tierConfigSection);
                    tiers.put(tierID, tier);
                }
            }
        });
        if (noTier == null) {
            noTier = new Tier();
        }
        noTier.initNoTier(config.getString("tiers.none-display"), config.getString("tiers.none-short-display"));

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

    public static Tier getNoTier() {
        return noTier;
    }
}
