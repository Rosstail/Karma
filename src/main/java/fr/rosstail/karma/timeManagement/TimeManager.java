package fr.rosstail.karma.timeManagement;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.configData.ConfigData;
import fr.rosstail.karma.timeManagement.times.SystemTimes;
import fr.rosstail.karma.timeManagement.times.WorldsTimes;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TimeManager {

    private static TimeManager timeManager;
    private final List<SystemTimes> systemTimes;
    private final List<WorldsTimes> worldTimes;
    private final String type = ConfigData.getConfigData().getUseTimeValue();

    public static void initTimeManager(Karma plugin) {
        if (timeManager == null) {
            timeManager = new TimeManager(plugin);
        }
    }

    TimeManager(Karma plugin) {
        FileConfiguration config = plugin.getCustomConfig();
        ArrayList<SystemTimes> systemTimes = new ArrayList<>();
        ArrayList<WorldsTimes> worldsTimes = new ArrayList<>();
        for (String timeName : config.getConfigurationSection("times.system-times").getKeys(false)) {
            ConfigurationSection tierConfigSection = config.getConfigurationSection("times.system-times." + timeName);
            if (tierConfigSection != null) {
                systemTimes.add(new SystemTimes(tierConfigSection, timeName));
            }
        }
        this.systemTimes = systemTimes;

        for (String timeName : config.getConfigurationSection("times.worlds-times").getKeys(false)) {
            ConfigurationSection tierConfigSection = config.getConfigurationSection("times.worlds-times." + timeName);
            if (tierConfigSection != null) {
                worldsTimes.add(new WorldsTimes(tierConfigSection, timeName));
            }
        }
        this.worldTimes = worldsTimes;
    }

    public boolean isPlayerInTime(Player player) {
        if (type != null && !type.equalsIgnoreCase("NONE")) {
            if (type.equalsIgnoreCase("BOTH")) {
                return isPlayerInSystemTime() || isPlayerInWorldTime(player);
            } else if (type.equalsIgnoreCase("SYSTEM")) {
                return isPlayerInSystemTime();
            } else if (type.equalsIgnoreCase("WORLDS")) {
                return isPlayerInWorldTime(player);
            }
        }
        return true;
    }

    public boolean isPlayerInSystemTime() {
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat hhmmFormat = new SimpleDateFormat("HH:mm");
        String formattedNow = hhmmFormat.format(now);

        for (SystemTimes systemTimes: getTimeManager().getSystemTimes()) {
            String formattedStartTime = systemTimes.getStartTime();
            String formattedEndTime = systemTimes.getEndTime();
            if (formattedStartTime.compareTo(formattedEndTime) <= 0) { //if start <= end
                if (formattedStartTime.compareTo(formattedNow) <= 0 && formattedEndTime.compareTo(formattedNow) >= 0) {
                    return systemTimes.roll();
                }
            } else if (formattedStartTime.compareTo(formattedNow) <= 0 || formattedEndTime.compareTo(formattedNow) >= 0) {
                if (ThreadLocalRandom.current().nextInt(0, 100) <= systemTimes.getRate()) {
                    return systemTimes.roll();
                }
            }
        }
        return false;
    }

    public boolean isPlayerInWorldTime(Player player) {
        World world = player.getWorld();
        long worldTime = world.getTime();

        for (WorldsTimes worldsTimes: getWorldTimes()) {
            if (worldsTimes.getStartTime() <= worldsTimes.getEndTime()) {
                if (worldsTimes.getStartTime() <= worldTime && worldsTimes.getEndTime() >= worldTime) {
                    return worldsTimes.roll();
                }
            } else if (worldsTimes.getStartTime() <= worldTime || worldsTimes.getEndTime() >= worldTime) {
                return worldsTimes.roll();
            }
        }
        return false;
    }



    public static TimeManager getTimeManager() {
        return timeManager;
    }

    public List<SystemTimes> getSystemTimes() {
        return systemTimes;
    }

    public List<WorldsTimes> getWorldTimes() {
        return worldTimes;
    }
}
