package fr.rosstail.karma.datas;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.times.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Gonna be used to optimize the research of values
 */
public class DataHandler {
    private static final Karma plugin = Karma.getInstance();
    private static final FileConfiguration config = plugin.getConfig();
    private static final int nbDec = config.getInt("general.decimal-number-to-show");
    private static final AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();

    public static boolean getTime(Player player) {
        String type = config.getString("times.use-both-system-and-worlds-time");
        if (type != null && !type.equalsIgnoreCase("NONE")) {
            if (type.equals("BOTH")) {
                return TimeManager.getSystemTime() && TimeManager.getPlayerWorldTime(player);
            } else if (type.equalsIgnoreCase("SYSTEM")) {
                return TimeManager.getSystemTime();
            } else if (type.equalsIgnoreCase("WORLDS")) {
                return TimeManager.getPlayerWorldTime(player);
            }
        }
        return true;
    }

    private static void placeCommands(Player player, String command) {
        PlayerData playerData = PlayerData.gets(player, plugin);
        command = command.replaceAll("<PLAYER>", player.getName());
        command = command
            .replaceAll("<KARMA>", String.format("%." + nbDec + "f", playerData.getKarma()));
        command = command.replaceAll("<TIER>", playerData.getTier().getDisplay());
        command = ChatColor.translateAlternateColorCodes('&', command);

        if (command.startsWith("<MESSAGE>")) {
            command = command.replaceAll("<MESSAGE>", "").trim();
            player.sendMessage(adaptMessage.message(player, 0, command));
        } else if (command.startsWith("<@>")) {
            command = command.replaceAll("<@>", "");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            Bukkit.dispatchCommand(player, command);
        }
    }

}
