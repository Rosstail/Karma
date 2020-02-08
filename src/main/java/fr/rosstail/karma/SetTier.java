package fr.rosstail.karma;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Change the tier of a user if changing karma put the player in another tier
 */
public class SetTier {
    private Karma karma = Karma.getInstance();
    TierChangesDispatchCommands tierChangesDispatchCommands = new TierChangesDispatchCommands();

    /**
     * Check the difference between old and new tiers of the player
     * @param player
     * @return
     */
    public String checkTier(Player player) {
        int tierMinimumKarma;
        int tierMaximumKarma;
        File file = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        int playerKarma = configuration.getInt("karma");
        String tier = configuration.getString("tier");
        String tierDisplay = karma.getConfig().getString("tiers." + tier + ".tier-display-name");
        Set<String> path = karma.getConfig().getConfigurationSection("tiers").getKeys(false);

        for (String tiers : path) {
            tierMinimumKarma = karma.getConfig().getInt("tiers." + tiers + ".tier-minimum-karma");
            tierMaximumKarma = karma.getConfig().getInt("tiers." + tiers + ".tier-maximum-karma");
            if (playerKarma >=  tierMinimumKarma && playerKarma <= tierMaximumKarma) {

                if (!tiers.equals(tier)) {
                    try {
                        configuration.set("tier", tiers);
                        configuration.save(file);
                        String newTierDisplay = karma.getConfig().getString("tiers." + tiers + ".tier-display-name");
                        player.sendMessage("You are now a " + newTierDisplay + ".");
                        tierChangesDispatchCommands.executeTierChangesCommands(player, tiers);
                        return newTierDisplay;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        return tierDisplay;
    }
}
