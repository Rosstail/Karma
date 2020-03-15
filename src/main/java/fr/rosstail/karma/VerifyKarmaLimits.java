package fr.rosstail.karma;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class VerifyKarmaLimits {
    private Karma karma = Karma.getInstance();
    private SetTier setTier = new SetTier();

    public VerifyKarmaLimits() {
    }

    /**
     * Check if the karma of specified player is inside the limits fork, specified in config.yml
     * @param player
     * @return
     */
    public int checkKarmaLimit(Player player) {
        File file = new File(this.karma.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        if (configuration.getInt("karma") > this.karma.getConfig().getInt("karma.maximum-karma")) {
            this.setKarmaToMaximum(file, configuration);
        } else if (configuration.getInt("karma") < this.karma.getConfig().getInt("karma.minimum-karma")) {
            this.setKarmaToMinimum(file, configuration);
        }
        this.setTier.checkTier(player);
        return configuration.getInt("karma");
    }

    /**
     * When the karma is too high, set it to the maximum allowed.
     * @param file
     * @param configuration
     */
    public void setKarmaToMaximum(File file, YamlConfiguration configuration) {
        try {
            configuration.set("karma", this.karma.getConfig().getInt("karma.maximum-karma"));
            configuration.save(file);
        } catch (IOException var5) {
            var5.printStackTrace();
        }
    }

    /**
     * When the player karma is too low, set it as minimum allowed
     * @param file
     * @param configuration
     */
    public void setKarmaToMinimum(File file, YamlConfiguration configuration) {
        try {
            configuration.set("karma", this.karma.getConfig().getInt("karma.minimum-karma"));
            configuration.save(file);
        } catch (IOException var5) {
            var5.printStackTrace();
        }
    }
}
