package fr.rosstail.karma;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * Gonna be used to optimize the research of values
 */
public class Getters {
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

}
