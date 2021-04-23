package fr.rosstail.karma.tiers;

import fr.rosstail.karma.Karma;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

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

        noTier = new Tier(config.getString("tiers.none-display"));
        config.getConfigurationSection("tiers.list").getKeys(false).forEach(tierID -> {
            ConfigurationSection tierConfigSection = config.getConfigurationSection("tiers.list." + tierID);
            if (tierConfigSection != null) {
                tiers.put(tierID, new Tier(tierConfigSection, tierID));
            }
        });
    }

    public static TierManager getTierManager() {
        return tierManager;
    }

    public Map<String, Tier> getTiers() {
        return tiers;
    }

    public static Tier getNoTier() {
        return noTier;
    }
}
