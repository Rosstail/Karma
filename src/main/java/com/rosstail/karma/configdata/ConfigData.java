package com.rosstail.karma.configdata;

import com.rosstail.karma.Karma;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.events.CustomEventHandler;
import com.rosstail.karma.events.Fights;
import com.rosstail.karma.lang.AdaptMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigData {
    private static ConfigData configValues;
    private final double defaultKarma;
    private final double minKarma;
    private final double maxKarma;

    private final String killedByTierPath;

    private final int decNumber;
    private final int saveDelay;

    private final long pvpCrimeTimeDelay;

    private final boolean isOvertimeActive;
    private final long overtimeFirstDelay;
    private final long overtimeNextDelay;
    private final double overtimeDecreaseValue;
    private final double overtimeDecreaseLimit;
    private final double overtimeIncreaseValue;
    private final double overtimeIncreaseLimit;
    private final List<String> overtimeDecreaseCommands;
    private final List<String> overtimeIncreaseCommands;

    private final boolean useWorldGuard;
    private final boolean pvpCrimeTimeEnabled;
    private final boolean pvpCrimeTimeOnUp;
    private final boolean pvpCrimeTimeOnStill;
    private final boolean pvpCrimeTimeOnDown;
    private final boolean pvpCrimeTimeRefresh;

    private final String useTimeValue;

    private final String dateTimeFormat;
    private final String pvpHitRewardExpression;
    private final String pvpKillRewardExpression;
    private final String pvpHitMessageKarmaIncrease;
    private final String pvpKillMessageKarmaIncrease;
    private final String pvpHitMessageKarmaDecrease;
    private final String pvpKillMessageKarmaDecrease;

    ConfigData(FileConfiguration config) {
        defaultKarma = config.getDouble("karma.default-karma");
        minKarma = config.getDouble("karma.minimum");
        maxKarma = config.getDouble("karma.maximum");

        String pluginName = Karma.getInstance().getName();
        killedByTierPath = "tiers.list.%" + pluginName.toLowerCase() + "_victim_tier%.commands.killed-commands.%" + pluginName.toLowerCase() + "_attacker_tier%";

        decNumber = config.getInt("general.decimal-number-to-show");
        pvpCrimeTimeDelay = config.getLong("pvp.crime-time.delay") * 1000;
        int saveDelay = config.getInt("data-save-delay");
        if (saveDelay == 0) {
            saveDelay = 300;
        }
        this.saveDelay = saveDelay * 1000;

        pvpHitRewardExpression = config.getString("pvp.hit-reward-expression");
        pvpKillRewardExpression = config.getString("pvp.kill-reward-expression");

        isOvertimeActive = config.getBoolean("overtime.active");
        overtimeFirstDelay = config.getLong("overtime.first-delay") * 20L;
        overtimeNextDelay = config.getLong("overtime.next-delay") * 20L;
        overtimeDecreaseValue = config.getDouble("overtime.values.decrease.value");
        overtimeDecreaseLimit = config.getDouble("overtime.values.decrease.limit");
        overtimeDecreaseCommands = config.getStringList("overtime.values.decrease.commands");
        overtimeIncreaseValue = config.getDouble("overtime.values.increase.value");
        overtimeIncreaseLimit = config.getDouble("overtime.values.increase.limit");
        overtimeIncreaseCommands = config.getStringList("overtime.values.increase.commands");

        useWorldGuard = Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && config.getBoolean("general.use-worldguard");
        pvpCrimeTimeEnabled = config.getBoolean("pvp.crime-time.enable");
        pvpCrimeTimeOnUp = config.getBoolean("pvp.crime-time.active-on-up");
        pvpCrimeTimeOnStill = config.getBoolean("pvp.crime-time.active-on-still");
        pvpCrimeTimeOnDown = config.getBoolean("pvp.crime-time.active-on-down");
        pvpCrimeTimeRefresh = config.getBoolean("pvp.crime-time.refresh");

        pvpHitMessageKarmaIncrease = config.getString("pvp.hit-message-on-karma-increase");
        pvpKillMessageKarmaIncrease = config.getString("pvp.kill-message-on-karma-increase");
        pvpHitMessageKarmaDecrease = config.getString("pvp.hit-message-on-karma-decrease");
        pvpKillMessageKarmaDecrease = config.getString("pvp.kill-message-on-karma-decrease");
        dateTimeFormat = config.getString("general.date-time-format");

        useTimeValue = config.getString("times.use-both-system-and-worlds-time");
    }

    public static void initKarmaValues(FileConfiguration config) {
        configValues = new ConfigData(config);
    }

    public static ConfigData getConfigData() {
        return configValues;
    }

    public double getDefaultKarma() {
        return defaultKarma;
    }

    public double getMinKarma() {
        return minKarma;
    }

    public double getMaxKarma() {
        return maxKarma;
    }

    public String getKilledByTierPath() {
        return killedByTierPath;
    }

    public int getDecNumber() {
        return decNumber;
    }

    public int getSaveDelay() {
        return saveDelay;
    }

    public String getDateTimeFormat() {
        if (dateTimeFormat == null) {
            return "yyyy-MM-dd HH:mm:ss";
        }
        return dateTimeFormat;
    }

    public boolean isOvertimeActive() {
        return isOvertimeActive;
    }

    public long getOvertimeFirstDelay() {
        return overtimeFirstDelay;
    }

    public long getOvertimeNextDelay() {
        return overtimeNextDelay;
    }

    public double getOvertimeDecreaseValue() {
        return overtimeDecreaseValue;
    }

    public double getOvertimeDecreaseLimit() {
        return overtimeDecreaseLimit;
    }

    public List<String> getOvertimeDecreaseCommands() {
        return overtimeDecreaseCommands;
    }

    public double getOvertimeIncreaseValue() {
        return overtimeIncreaseValue;
    }

    public double getOvertimeIncreaseLimit() {
        return overtimeIncreaseLimit;
    }

    public List<String> getOvertimeIncreaseCommands() {
        return overtimeIncreaseCommands;
    }

    public String getPvpHitRewardExpression() {
        return pvpHitRewardExpression;
    }

    public String getPvpKillRewardExpression() {
        return pvpKillRewardExpression;
    }

    public String getPvpHitMessageKarmaDecrease() {
        return pvpHitMessageKarmaDecrease;
    }

    public String getPvpKillMessageKarmaDecrease() {
        return pvpKillMessageKarmaDecrease;
    }

    public String getPvpHitMessageKarmaIncrease() {
        return pvpHitMessageKarmaIncrease;
    }

    public String getPvpKillMessageKarmaIncrease() {
        return pvpKillMessageKarmaIncrease;
    }

    public boolean doesUseWorldGuard() {
        return useWorldGuard;
    }

    public boolean isPvpCrimeTimeEnabled() {
        return pvpCrimeTimeEnabled;
    }

    public boolean isPvpCrimeTimeOnUp() {
        return pvpCrimeTimeOnUp;
    }

    public boolean isPvpCrimeTimeOnStill() {
        return pvpCrimeTimeOnStill;
    }

    public boolean isPvpCrimeTimeOnDown() {
        return pvpCrimeTimeOnDown;
    }

    public long getPvpCrimeTimeDelay() {
        return pvpCrimeTimeDelay;
    }

    public boolean isPvpCrimeTimeRefresh() {
        return pvpCrimeTimeRefresh;
    }

    public String getUseTimeValue() {
        return useTimeValue;
    }

    public static void applyNewConfigValues(FileConfiguration config) {
        initKarmaValues(config);
        PlayerData.setConfigData(configValues);
        CustomEventHandler.setConfigData(configValues);
        Fights.setConfigData(configValues);
        AdaptMessage.getAdaptMessage().setConfigData(configValues);
    }
}
