package fr.rosstail.karma.tiers;

import fr.rosstail.karma.Karma;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class TierManager {

    private static TierManager tierManager;
    private final Map<String, Tier> tiers = new HashMap<>();

    public static void initTierManager(Karma plugin) {
        if (tierManager == null) {
            tierManager = new TierManager(plugin);
        }
    }

    TierManager(Karma plugin) {
        FileConfiguration config = plugin.getConfig();
        config.getConfigurationSection("tiers").getKeys(false).forEach(tierID -> {
            ConfigurationSection tierConfigSection = config.getConfigurationSection("tiers." + tierID);
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
}
