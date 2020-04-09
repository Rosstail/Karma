package fr.rosstail.karma;

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
        int playerKarma = getPlayerKarma(player);
        int min = this.karma.getConfig().getInt("karma.minimum-karma");
        int max = this.karma.getConfig().getInt("karma.maximum-karma");

        if (playerKarma < min || playerKarma > max) {
            playerKarma = setKarmaToLimit(player, min, max);
        }

        setTier.checkTier(player);
        return playerKarma;
    }
}
