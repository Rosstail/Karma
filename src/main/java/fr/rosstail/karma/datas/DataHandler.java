package fr.rosstail.karma.datas;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.apis.PAPI;
import fr.rosstail.karma.lang.AdaptMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Gonna be used to optimize the research of values
 */
public class DataHandler {
    private static final Karma plugin = Karma.getInstance();
    private static final FileConfiguration config = plugin.getConfig();
    private static final File langFile = new File(plugin.getDataFolder(), "lang/" + config.getString("general.lang") + ".yml");
    private static final YamlConfiguration configLang = YamlConfiguration.loadConfiguration(langFile);
    private static final int nbDec = config.getInt("general.decimal-number-to-show");
    private static final PAPI papi = new PAPI();
    private static final AdaptMessage adaptMessage = new AdaptMessage(plugin);

    /**
     * return the displaying name of the tier.
     *
     * @return
     */
    private String loadPlayerDisplayTier(Player player, String playerTier) {
        return config.getString("tiers." + playerTier + ".tier-display-name");
    }

    /**
     * Get the karma limits of karma for specified tier in Config.yml
     *
     * @return
     */
    public static double[] getTierLimits(String tier) {
        double tierMinimumKarma =
            config.getDouble("tiers." + tier + ".tier-minimum-karma");
        double tierMaximumKarma =
            config.getDouble("tiers." + tier + ".tier-maximum-karma");
        return new double[] {tierMinimumKarma, tierMaximumKarma};
    }

    public static String[] getSystemTimeLimits(String time) {
        String minimumHourMin =
            config.getString("times.system-times." + time + ".starting-time");
        String maximumHourMin =
            config.getString("times.system-times." + time + ".ending-time");
        return new String[] {minimumHourMin, maximumHourMin};
    }

    public static long[] getWorldTimeLimits(String time) {
        String minimumHourMin =
            config.getString("times.worlds-times." + time + ".starting-time");
        String maximumHourMin =
            config.getString("times.worlds-times." + time + ".ending-time");

        assert minimumHourMin != null;
        assert maximumHourMin != null;
        String[] convMinHourMin = minimumHourMin.split(":", 2);
        String[] convMaxHourMin = maximumHourMin.split(":", 2);
        long minHour = (long) (1000 * Integer.parseInt(convMinHourMin[0]) + 16.66 * Integer
            .parseInt(convMinHourMin[1])) + 18000L;
        long maxHour = (long) (1000 * Integer.parseInt(convMaxHourMin[0]) + 16.66 * Integer
            .parseInt(convMinHourMin[1])) + 18000L;
        if (minHour > 24000L) {
            minHour -= 24000L;
        }
        if (maxHour > 24000L) {
            maxHour -= 24000L;
        }
        return new long[] {minHour, maxHour};
    }

    public static boolean getTime(Player player) {
        String type = config.getString("times.use-both-system-and-worlds-time");
        if (type != null && !type.equalsIgnoreCase("NONE")) {
            if (type.equals("BOTH")) {
                return getSystemTime() && getPlayerWorldTime(player);
            } else if (type.equalsIgnoreCase("SYSTEM")) {
                return getSystemTime();
            } else if (type.equalsIgnoreCase("WORLDS")) {
                return getPlayerWorldTime(player);
            }
        }
        return true;
    }

    public static boolean getSystemTime() {
        Set<String> path =
            config.getConfigurationSection("times.system-times").getKeys(false);
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat hhmmFormat = new SimpleDateFormat("HH:mm");

        String[] timeLimits;

        for (String timeList : path) {
            timeLimits = getSystemTimeLimits(timeList);
            if (timeLimits[1].compareTo(timeLimits[0]) >= 0) {
                if (timeLimits[0].compareTo(hhmmFormat.format(now)) <= 0
                    && timeLimits[1].compareTo(hhmmFormat.format(now)) >= 0) {
                    if (ThreadLocalRandom.current().nextInt(0, 100) <= config
                        .getInt("times.system-times." + timeList + ".chance")) {
                        return true;
                    }
                }
            } else {
                if (timeLimits[0].compareTo(hhmmFormat.format(now)) <= 0
                    || timeLimits[1].compareTo(hhmmFormat.format(now)) >= 0) {
                    if (ThreadLocalRandom.current().nextInt(0, 100) <= config
                        .getInt("times.system-times." + timeList + ".chance")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean getPlayerWorldTime(Player player) {
        Set<String> path =
            config.getConfigurationSection("times.worlds-times").getKeys(false);
        World world = player.getWorld();
        long worldTime = world.getTime();
        long[] timeLimits;

        for (String timeList : path) {
            timeLimits = getWorldTimeLimits(timeList);
            if (timeLimits[0] <= timeLimits[1]) {
                if (timeLimits[0] <= worldTime && timeLimits[1] >= worldTime) {
                    if (ThreadLocalRandom.current().nextInt(0, 100) <= config
                        .getInt("times.worlds-times." + timeList + ".chance")) {
                        return true;
                    }
                }
            } else {
                if (timeLimits[0] <= worldTime || timeLimits[1] >= worldTime) {
                    if (ThreadLocalRandom.current().nextInt(0, 100) <= config
                        .getInt("times.worlds-times." + timeList + ".chance")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static void changePlayerTierMessage(Player player) {
        String message = configLang.getString("tier-change");
        if (message != null) {
            AdaptMessage adaptMessage = new AdaptMessage(plugin);
            adaptMessage.message(player, player, 0, message);
        }
    }

    private static void tierCommandsLauncher(Player player, String playerTier) {
        launchAllCommands(player, config.getStringList("tiers." + playerTier + ".commands"));
    }

    private static void tierCommandsLauncherOnUp(Player player, String playerTier) {
        launchAllCommands(player, config.getStringList("tiers." + playerTier + ".commands-on-up"));
    }

    private static void tierCommandsLauncherOnDown(Player player, String playerTier) {
        launchAllCommands(player, config.getStringList("tiers." + playerTier + ".commands-on-down"));
    }

    private static void launchAllCommands(Player player, List<String> listCommands) {
        for (String command : listCommands) {
            if (command != null) {
                placeCommands(player, command);
            }
        }
    }

    private static void placeCommands(Player player, String command) {
        PlayerData playerData = PlayerData.gets(player, plugin);
        command = command.replaceAll("<PLAYER>", player.getName());
        command = command
            .replaceAll("<KARMA>", String.format("%." + nbDec + "f", playerData.getPlayerKarma()));
        command = command.replaceAll("<TIER>", playerData.getPlayerDisplayTier());
        command = ChatColor.translateAlternateColorCodes('&', command);
        command = papi.setPlaceholdersOnMessage(command, player);

        if (command.startsWith("<MESSAGE>")) {
            command = command.replaceAll("<MESSAGE>", "").trim();
            adaptMessage.message(player, player, 0, command);
        } else if (command.startsWith("<@>")) {
            command = command.replaceAll("<@>", "");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            Bukkit.dispatchCommand(player, command);
        }
    }

}
