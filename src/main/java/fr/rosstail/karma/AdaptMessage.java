package fr.rosstail.karma;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AdaptMessage {
    private Karma karma = Karma.getInstance();
    private Map<String, Long> cooldown = new HashMap<String, Long>();
    private String message = null;

    /**
     * Replaces every placeholders when a player hit an entity
     * @param message
     * @param player
     * @param karma
     * @param reward
     * @return
     */
    private String setEntityHitMessage(String message, Player player, int karma, int reward) {
        message = message.replaceAll("<attacker>", player.getName());
        message = message.replaceAll("<reward>", Integer.toString(reward));
        message = message.replaceAll("<previousKarma>", Integer.toString(karma));
        message = message.replaceAll("<karma>", Integer.toString(karma + reward));
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    public void getEntityHitMessage(String message, Player player, int karma, int reward) {

        if (cooldown.containsKey(player.getName())) {
            int seconds = this.karma.getConfig().getInt("general.delay-between-hit-messages");
            long timeLeft = ((cooldown.get(player.getName())) / 1000 + seconds) - (System.currentTimeMillis() / 1000);
            if (timeLeft > 0) {
                return;
            }
            else {
                if (message != null) {
                    message = setEntityHitMessage(message, player, karma, reward);
                }
            }
        }
        cooldown.put(player.getName(), System.currentTimeMillis());
        if (message != null)
            player.sendMessage(message);
    }

    /**
     * Replaces every placeholders when a player kills an entity
     * @param message
     * @param player
     * @param karma
     * @param reward
     * @return
     */
    private String setEntityKillMessage(String message, Player player, int karma, int reward) {
        message = message.replaceAll("<attacker>", player.getName());
        message = message.replaceAll("<reward>", Integer.toString(reward));
        message = message.replaceAll("<previousKarma>", Integer.toString(karma));
        message = message.replaceAll("<karma>", Integer.toString(karma + reward));
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    public void getEntityKillMessage(String message, Player player, int karma, int reward) {

        if (cooldown.containsKey(player.getName())) {
            int seconds = this.karma.getConfig().getInt("general.delay-between-kill-messages");
            long timeLeft = ((cooldown.get(player.getName())) / 1000 + seconds) - (System.currentTimeMillis() / 1000);
            if (timeLeft > 0) {
                return;
            }
            else {
                if (message != null) {
                    message = setEntityKillMessage(message, player, karma, reward);
                }
            }
        }
        cooldown.put(player.getName(), System.currentTimeMillis());
        if (message != null)
            player.sendMessage(message);
    }


    /**
     *
     * @param message
     * @param attacker
     * @param initialKarma
     * @param newKarma
     * @return
     */
    private String setPlayerHitMessage(String message, Player attacker, int initialKarma, int newKarma) {
        message = message.replaceAll("<attacker>", attacker.getName());
        message = message.replaceAll("<previousKarma>", Integer.toString(initialKarma));
        message = message.replaceAll("<karma>", Integer.toString(newKarma));
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    public void getPlayerHitMessage(String message, Player player, int initialKarma, int newKarma) {
        if (cooldown.containsKey(player.getName())) {
            int seconds = this.karma.getConfig().getInt("general.delay-between-hit-messages");
            long timeLeft = ((cooldown.get(player.getName())) / 1000 + seconds) - (System.currentTimeMillis() / 1000);
            if (timeLeft > 0) {
                return;
            }
            else {
                if (message != null) {
                    message = setPlayerHitMessage(message, player, initialKarma, newKarma);
                }
            }
        }
        cooldown.put(player.getName(), System.currentTimeMillis());
        if (message != null)
            player.sendMessage(message);
    }

    /**
     * Replaces every placeholders of a message when a player kills another player
     * @param message
     * @param killer
     * @param initialKarma
     * @param newKarma
     * @return
     */
    private String setPlayerKillMessage(String message, Player killer, int initialKarma, int newKarma) {
        message = message.replaceAll("<killer>", killer.getName());
        message = message.replaceAll("<previousKarma>", Integer.toString(initialKarma));
        message = message.replaceAll("<karma>", Integer.toString(newKarma));
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    public void getPlayerKillMessage(String message, Player player, int initialKarma, int newKarma) {
        if (cooldown.containsKey(player.getName())) {
            int seconds = this.karma.getConfig().getInt("general.delay-between-kill-messages");
            long timeLeft = ((cooldown.get(player.getName())) / 1000 + seconds) - (System.currentTimeMillis() / 1000);
            if (timeLeft > 0) {
                return;
            }
            else {
                if (message != null) {
                    message = setPlayerKillMessage(message, player, initialKarma, newKarma);
                }
            }
        }
        cooldown.put(player.getName(), System.currentTimeMillis());
        if (message != null)
            player.sendMessage(message);
    }
}
