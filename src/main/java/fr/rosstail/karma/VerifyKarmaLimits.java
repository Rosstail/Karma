package fr.rosstail.karma;

import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class VerifyKarmaLimits extends GetSet {
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
        int playerKarma = configuration.getInt("karma");
        int min = this.karma.getConfig().getInt("karma.minimum-karma");
        int max = this.karma.getConfig().getInt("karma.maximum-karma");

        if (playerKarma < min || playerKarma > max) {
            setKarmaToLimit(player, min, max);
        }

        setTier.checkTier(player);
        return configuration.getInt("karma");
    }

    public void setKarmaToLimit(Player player, int min, int max) {
        int playerKarma = getPlayerKarma(player);
        if (playerKarma < min) {
            setKarmaToPlayer(player, min);
        }
        else {
            setKarmaToPlayer(player, max);
        }

    }
}
