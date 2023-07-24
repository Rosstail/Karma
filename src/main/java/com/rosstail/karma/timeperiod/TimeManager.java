package com.rosstail.karma.timeperiod;

import com.rosstail.karma.Karma;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.timeperiod.times.SystemTimes;
import com.rosstail.karma.timeperiod.times.WorldsTimes;
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
    private final Karma plugin;
    private static TimeManager timeManager;
    private final List<SystemTimes> systemTimes = new ArrayList<>();
    private final List<WorldsTimes> worldTimes = new ArrayList<>();

    public static void initTimeManager(Karma plugin) {
        if (timeManager == null) {
            timeManager = new TimeManager(plugin);
        }
    }

    TimeManager(Karma plugin) {
        this.plugin = plugin;
    }

    public void setupTimes() {
        systemTimes.clear();
        worldTimes.clear();
        FileConfiguration config = plugin.getCustomConfig();
        for (String timeName : config.getConfigurationSection("times.system-times").getKeys(false)) {
            ConfigurationSection tierConfigSection = config.getConfigurationSection("times.system-times." + timeName);
            if (tierConfigSection != null) {
                systemTimes.add(new SystemTimes(tierConfigSection, timeName));
            }
        }

        for (String timeName : config.getConfigurationSection("times.worlds-times").getKeys(false)) {
            ConfigurationSection tierConfigSection = config.getConfigurationSection("times.worlds-times." + timeName);
            if (tierConfigSection != null) {
                worldTimes.add(new WorldsTimes(tierConfigSection, timeName));
            }
        }
    }

    public boolean isPlayerInTime(Player player) {
        String type = ConfigData.getConfigData().times.useTimeValue;
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
