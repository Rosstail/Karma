package fr.rosstail.karma;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

/**
 * Change the tier of a user if changing karma put the player in another tier
 */
public class SetTier {
    private Karma karma = Karma.getInstance();

    /**
     * Check the difference between old and new tiers of the player
     * @param player
     * @return
     */
    public String checkTier(Player player) {
        int i = 1;
        int tierMinimumKarma;
        int tierMaximumKarma;
        File file = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        int playerKarma = configuration.getInt("karma");
        String tier = configuration.getString("tier");
        String tierDisplay = karma.getConfig().getString("tiers." + tier + ".tier-display-name");

        for (String path : karma.getConfig().getConfigurationSection("tiers").getKeys(false)) {
            tierMinimumKarma = karma.getConfig().getInt("tiers." + path + ".tier-minimum-karma");
            tierMaximumKarma = karma.getConfig().getInt("tiers." + path + ".tier-maximum-karma");
            if (playerKarma >=  tierMinimumKarma && playerKarma <= tierMaximumKarma) {

                if (!path.equals(tier)) {
                    try {
                        configuration.set("tier", path);
                        configuration.save(file);
                        String newTierDisplay = karma.getConfig().getString("tiers." + path + ".tier-display-name");
                        player.sendMessage("Vous êtes désormais un " + newTierDisplay + ".");
                        return newTierDisplay;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            i++;
        }
        return tierDisplay;
    }
}
