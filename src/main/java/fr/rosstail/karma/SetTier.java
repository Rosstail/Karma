package fr.rosstail.karma;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class SetTier {
    private Karma karma = Karma.getInstance();

    public void checkTier(Player player) {
        int i = 1;
        File file = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        int playerKarma = configuration.getInt("karma");
        for (String path : karma.getConfig().getConfigurationSection("tiers").getKeys(false)) {
            if (playerKarma >= karma.getConfig().getInt("tiers." + path + ".tier-minimum-karma") &&
                    playerKarma <= karma.getConfig().getInt("tiers." + path + ".tier-maximum-karma")) {
                if (!path.equals(configuration.getString("tier"))) {
                    try {
                        configuration.set("tier", path);
                        configuration.save(file);
                        player.sendMessage("Vous êtes désormais un " + karma.getConfig().getString("tiers." + path + ".tier-display-name") + ".");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            i++;
        }
    }
}
