package fr.rosstail.karma;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

/**
 * Gonna be used to optimize the research of values
 */
public class GetSet {
    private Karma karma = Karma.getInstance();

    public int getPlayerKarma(Player player) {
        File playerFile = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        return playerConfig.getInt("karma");
    }

    public String getPlayerTier(Player player) {
        File playerFile = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        return playerConfig.getString("tier");
    }

    public String getPlayerDisplayTier(Player player) {
        return karma.getConfig().getString("tiers." + getPlayerTier(player) + ".tier-display-name");
    }

    /**
     * Get the karma limits of karma for specified tier in Config.yml
     * @param tier
     * @return
     */
    public int[] getTierLimits(String tier) {
        int tierMinimumKarma = karma.getConfig().getInt("tiers." + tier + ".tier-minimum-karma");
        int tierMaximumKarma = karma.getConfig().getInt("tiers." + tier + ".tier-maximum-karma");
        return new int[]{tierMinimumKarma, tierMaximumKarma};
    }






    /**
     * SETTERS
     *
     */





    public void setKarmaToPlayer(Player player, int karma) {
        File playerFile = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        playerConfig.set("karma", karma);
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTierToPlayer(Player player, String tier) {
        File playerFile = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        playerConfig.set("tier", tier);
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
