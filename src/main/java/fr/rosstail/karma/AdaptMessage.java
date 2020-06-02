package fr.rosstail.karma;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AdaptMessage extends GetSet {
    private Karma karma = Karma.get();
    private Map<String, Long> cooldown = new HashMap<String, Long>();
    private int nbDec = karma.getConfig().getInt("general.decimal-number-to-show");

    /**
     * Sends automatically the message to the sender with some parameters
     * @param sender the sender, can be console, player or null.
     * @param player the player targetted
     * @param value the value. Can be reward or a simple value
     * @param message the content of the message
     */
    public void message(CommandSender sender, Player player, double value, String message) {

        if (message != null) {

            if (player != null) {
                double playerKarma = getPlayerKarma(player);
                message = message.replaceAll("<PLAYER>", player.getName());
                message = message.replaceAll("<KARMA>", String.format("%." + nbDec + "f", playerKarma));
                message = message.replaceAll("<TIER>", getPlayerDisplayTier(player));
                message = message.replaceAll("<VALUE>", String.format("%." + nbDec + "f", value));
                message = message.replaceAll("<OLD_KARMA>", String.format("%." + nbDec + "f", playerKarma - value));
            }

            message = ChatColor.translateAlternateColorCodes('&', message);
            if (message != null) {
                if (sender != null) {
                    sender.sendMessage(message);
                } else {
                    player.sendMessage(message);
                }
            }
        }
    }

    public void entityHitMessage(String message, Player player, double value) {
        double playerKarma = getPlayerKarma(player);

        if (message != null) {
            message = message.replaceAll("<ATTACKER>", player.getName());
            message = message.replaceAll("<VALUE>", String.format("%." + nbDec + "f", value));
            message = message.replaceAll("<OLD_KARMA>", String.format("%." + nbDec + "f", playerKarma - value));
            message = message.replaceAll("<KARMA>", String.format("%." + nbDec + "f", playerKarma));
            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        if (cooldown.containsKey(player.getName())) {
            double seconds = this.karma.getConfig().getDouble("general.delay-between-hit-messages");
            double timeLeft = cooldown.get(player.getName()) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        cooldown.put(player.getName(), System.currentTimeMillis());
        if (message != null) {
            player.sendMessage(message);
        }
    }

    public void entityKillMessage(String message, Player player, double value) {
        double playerKarma = getPlayerKarma(player);


        if (message != null) {
            message = message.replaceAll("<ATTACKER>", player.getName());
            message = message.replaceAll("<VALUE>", String.format("%." + nbDec + "f", value));
            message = message.replaceAll("<OLD_KARMA>", String.format("%." + nbDec + "f", playerKarma - value));
            message = message.replaceAll("<KARMA>", String.format("%." + nbDec + "f", playerKarma));
            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        if (cooldown.containsKey(player.getName())) {
            double seconds = this.karma.getConfig().getDouble("general.delay-between-kill-messages");
            double timeLeft = cooldown.get(player.getName()) - System.currentTimeMillis() + seconds * 1000f;

            if (!(timeLeft <= 0)) {
                return;
            }
        }

        cooldown.put(player.getName(), System.currentTimeMillis());
        if (message != null) {
            player.sendMessage(message);
        }
    }

    public void playerHitMessage(String message, Player attacker, Player victim, double value) {
        double attackerKarma = getPlayerKarma(attacker);
        double victimKarma = getPlayerKarma(victim);

        if (message != null) {
            message = message.replaceAll("<ATTACKER>", attacker.getName());
            message = message.replaceAll("<VICTIM>", victim.getName());
            message = message.replaceAll("<ATTACKER_OLD_KARMA>", String.format("%." + nbDec + "f", value));
            message = message.replaceAll("<VALUE>", String.format("%." + nbDec + "f", attackerKarma - value));
            message = message.replaceAll("<ATTACKER_KARMA>", String.format("%." + nbDec + "f", attackerKarma));
            message = message.replaceAll("<ATTACKER_TIER>", getPlayerDisplayTier(attacker));

            message = message.replaceAll("<VICTIM_KARMA>", String.format("%." + nbDec + "f", victimKarma));
            message = message.replaceAll("<VICTIM_TIER>", getPlayerDisplayTier(victim));

            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        if (cooldown.containsKey(attacker.getName())) {
            double seconds = this.karma.getConfig().getDouble("general.delay-between-hit-messages");
            double timeLeft = cooldown.get(attacker.getName()) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        cooldown.put(attacker.getName(), System.currentTimeMillis());
        if (message != null) {
            attacker.sendMessage(message);
        }
    }

    public void playerKillMessage(String message, Player killer, Player victim, double value) {
        double killerKarma = getPlayerKarma(killer);
        double victimKarma = getPlayerKarma(victim);

        if (message != null) {
            message = message.replaceAll("<ATTACKER>", killer.getName());
            message = message.replaceAll("<VICTIM>", victim.getName());
            message = message.replaceAll("<ATTACKER_OLD_KARMA>", String.format("%." + nbDec + "f", value));
            message = message.replaceAll("<VALUE>", String.format("%." + nbDec + "f", killerKarma - value));
            message = message.replaceAll("<ATTACKER_KARMA>", String.format("%." + nbDec + "f", killerKarma));
            message = message.replaceAll("<ATTACKER_TIER>", getPlayerDisplayTier(killer));

            message = message.replaceAll("<VICTIM_KARMA>", String.format("%." + nbDec + "f", victimKarma));
            message = message.replaceAll("<VICTIM_TIER>", getPlayerDisplayTier(victim));

            message = ChatColor.translateAlternateColorCodes('&', message);
        }

        if (cooldown.containsKey(killer.getName())) {
            double seconds = this.karma.getConfig().getDouble("general.delay-between-kill-messages");
            double timeLeft = cooldown.get(killer.getName()) - System.currentTimeMillis() + seconds * 1000f;
            if (!(timeLeft <= 0)) {
                return;
            }
        }

        cooldown.put(killer.getName(), System.currentTimeMillis());
        if (message != null) {
            killer.sendMessage(message);
        }
    }

}