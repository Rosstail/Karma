package fr.rosstail.karma.datas;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.times.TimeManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Gonna be used to optimize the research of values
 */
public class DataHandler {
    private static final Karma plugin = Karma.getInstance();
    private static final String type = plugin.getCustomConfig().getString("times.use-both-system-and-worlds-time");

    public static boolean getTime(Player player) {
        if (type != null && !type.equalsIgnoreCase("NONE")) {
            if (type.equals("BOTH")) {
                return !TimeManager.getSystemTime() || !TimeManager.getPlayerWorldTime(player);
            } else if (type.equalsIgnoreCase("SYSTEM")) {
                return !TimeManager.getSystemTime();
            } else if (type.equalsIgnoreCase("WORLDS")) {
                return !TimeManager.getPlayerWorldTime(player);
            }
        }
        return false;
    }

}
