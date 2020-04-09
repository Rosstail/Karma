package fr.rosstail.karma;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Set;

/**
 * Change the tier of a user if changing karma put the player in another tier
 */
public class SetTier extends GetSet {
    private Karma karma = Karma.getInstance();

    /**
     * Check the difference between old and new tiers of the player
     * @param player
     * @return
     */
    public String checkTier(Player player) {
        int playerKarma = getPlayerKarma(player);
        String tierDisplay = getPlayerDisplayTier(player);

        int[] tierLimits;
        File lang = new File(this.karma.getDataFolder(), "lang/" + karma.getConfig().getString("general.lang") + ".yml");
        YamlConfiguration configurationLang = YamlConfiguration.loadConfiguration(lang);

        File file = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        String tier = configuration.getString("tier");
        Set<String> path = karma.getConfig().getConfigurationSection("tiers").getKeys(false);

        for (String tiers : path) {
            tierLimits = getTierLimits(tiers);
            if (playerKarma >=  tierLimits[0] && playerKarma <= tierLimits[1] && !tiers.equals(tier)) {
                setTierToPlayer(player, tiers);
                tierDisplay = getPlayerDisplayTier(player);

                String message = configurationLang.getString("tier-change");
                if (message != null) {
                    message = message.replaceAll("<tier>", tierDisplay);
                    message = ChatColor.translateAlternateColorCodes('&', message);
                    player.sendMessage(message);
                }

                break;
            }
        }
        return tierDisplay;
    }
}