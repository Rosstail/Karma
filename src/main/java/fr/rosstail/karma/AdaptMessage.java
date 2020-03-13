package fr.rosstail.karma;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AdaptMessage {

    /**
     * Replaces every placeholders when a player kills an entity
     * @param message
     * @param killer
     * @param karma
     * @param reward
     * @return
     */
    private String setEntityKillMessage(String message, Player killer, int karma, int reward) {
        message = message.replaceAll("<attacker>", killer.getName());
        message = message.replaceAll("<reward>", Integer.toString(reward));
        message = message.replaceAll("<previousKarma>", Integer.toString(karma));
        message = message.replaceAll("<karma>", Integer.toString(karma + reward));
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    public String getEntityKillMessage(String message, Player killer, int karma, int reward) {
        message = setEntityKillMessage(message, killer, karma, reward);
        return message;
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

    public String getPlayerHitMessage(String message, Player attacker, int initialKarma, int newKarma) {
        message = setPlayerHitMessage(message, attacker, initialKarma, newKarma);
        return message;
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

    public String getPlayerKillMessage(String message, Player killer, int initialKarma, int newKarma) {
        message = setPlayerKillMessage(message, killer, initialKarma, newKarma);
        return message;
    }
}
