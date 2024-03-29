package com.rosstail.karma.tiers;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

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
        FileConfiguration tiersFileConfiguration = ConfigData.getConfigData().tiers.fileConfig;
        Set<String> configTiers = tiersFileConfiguration.getConfigurationSection("tiers.list").getKeys(false);

        if (noTier == null) {
            noTier = new Tier();
        }

        Map<String, Tier> tierMap = new HashMap<>(tiers);
        for (Map.Entry<String, Tier> entry : tierMap.entrySet()) { //Check and remove tiers that do not exist anymore
            String s = entry.getKey();
            ConfigurationSection tierConfigSection = tiersFileConfiguration.getConfigurationSection("tiers.list." + s);
            if (tierConfigSection == null) {
                tiers.remove(s);
            }
        }

        configTiers.forEach(tierID -> {
            ConfigurationSection tierConfigSection = tiersFileConfiguration.getConfigurationSection("tiers.list." + tierID);
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
        noTier.initNoTier(LangManager.getMessage(LangMessage.TIER_NONE_DISPLAY), LangManager.getMessage(LangMessage.TIER_NONE_SHORT_DISPLAY));
    }

    public static TierManager getTierManager() {
        return tierManager;
    }

    public Map<String, Tier> getTiers() {
        return tiers;
    }

    public Tier getTierByName(String tierName) {
        if (tierName != null && tiers.containsKey(tierName)) {
            return tiers.get(tierName);
        } else {
            return noTier;
        }
    }

    public Tier getTierByKarmaAmount(float amount) {
        for (Map.Entry<String, Tier> entry : tiers.entrySet()) {
            String s = entry.getKey();
            Tier tier = entry.getValue();
            if (amount >= tier.getMinKarma() && amount <= tier.getMaxKarma()) {
                return tier;
            }
        }
        return noTier;
    }

    public static Tier getNoTier() {
        return noTier;
    }
}
