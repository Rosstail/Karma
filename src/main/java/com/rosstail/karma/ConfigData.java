package com.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigData {
    private static ConfigData configValues;

    public final double defaultKarma;
    public final double minKarma;
    public final double maxKarma;

    public final String killedByTierPath;

    public final int decNumber;
    public final int titleFadeIn;
    public final int titleStay;
    public final int titleFadeOut;
    public final int hitMessageDelay;
    public final int killMessageDelay;
    public final int saveDelay;

    public final boolean isOvertimeActive;
    public final long overtimeFirstDelay;
    public final long overtimeNextDelay;
    public final double overtimeDecreaseValue;
    public final double overtimeDecreaseLimit;
    public final double overtimeIncreaseValue;
    public final double overtimeIncreaseLimit;
    public final List<String> overtimeDecreaseCommands;
    public final List<String> overtimeIncreaseCommands;

    public final boolean useWorldGuard;
    public final boolean wantedEnable;
    public final boolean wantedOnKarmaGain;
    public final boolean wantedOnKarmaUnchanged;
    public final boolean wantedOnKarmaLoss;
    public final boolean wantedRefresh;
    public final boolean cancelWantedKarmaGain;
    public final boolean cancelWantedKarmaLoss;
    public final boolean cancelInnocentKarmaGain;
    public final boolean cancelInnocentKarmaLoss;

    public final String useTimeValue;
    private final String dateTimeFormat;
    public final String pvpHitRewardExpression;
    public final String pvpKillRewardExpression;
    public final String wantedDurationExpression;
    public final String wantedMaxDurationExpression;


    ConfigData(FileConfiguration config) {
        defaultKarma = config.getDouble("karma.default-karma");
        minKarma = config.getDouble("karma.minimum");
        maxKarma = config.getDouble("karma.maximum");

        String pluginName = Karma.getInstance().getName();
        killedByTierPath = "tiers.list.%" + pluginName.toLowerCase() + "_victim_tier%.commands.killed-commands.%" + pluginName.toLowerCase() + "_attacker_tier%";

        decNumber = config.getInt("general.decimal-display");
        hitMessageDelay = config.getInt("pvp.messages-delay.hit");
        killMessageDelay = config.getInt("pvp.messages-delay.kill");
        titleFadeIn = config.getInt("general.title.fade-in");
        titleStay = config.getInt("general.title.stay");
        titleFadeOut = config.getInt("general.title.fade-out");

        int saveDelay = config.getInt("data-save-delay");
        if (saveDelay == 0) {
            saveDelay = 300;
        }
        this.saveDelay = saveDelay * 1000;

        pvpHitRewardExpression = config.getString("pvp.hit-reward-expression");
        pvpKillRewardExpression = config.getString("pvp.kill-reward-expression");

        isOvertimeActive = config.getString("overtime.active") != null && config.getBoolean("overtime.active");
        overtimeFirstDelay = config.getLong("overtime.first-delay") * 20L;
        overtimeNextDelay = config.getLong("overtime.next-delay") * 20L;
        overtimeDecreaseValue = config.getDouble("overtime.values.decrease.value");
        overtimeDecreaseLimit = config.getDouble("overtime.values.decrease.limit");
        overtimeDecreaseCommands = config.getStringList("overtime.values.decrease.commands");
        overtimeIncreaseValue = config.getDouble("overtime.values.increase.value");
        overtimeIncreaseLimit = config.getDouble("overtime.values.increase.limit");
        overtimeIncreaseCommands = config.getStringList("overtime.values.increase.commands");

        useWorldGuard = Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && config.getBoolean("general.use-worldguard");
        wantedEnable = config.getString("pvp.wanted.enable") != null && config.getBoolean("pvp.wanted.enable");
        wantedOnKarmaGain = config.getString("pvp.wanted.conditions.on-karma-gain") != null && config.getBoolean("pvp.wanted.conditions.on-karma-gain");
        wantedOnKarmaUnchanged = config.getString("pvp.wanted.conditions.on-karma-unchanged") != null && config.getBoolean("pvp.wanted.conditions.on-karma-unchanged");
        wantedOnKarmaLoss = config.getString("pvp.wanted.conditions.on-karma-loss") != null && config.getBoolean("pvp.wanted.conditions.on-karma-loss");
        wantedRefresh = config.getString("pvp.wanted.conditions.refresh") != null && config.getBoolean("pvp.wanted.conditions.refresh");
        wantedDurationExpression = config.getString("pvp.wanted.duration");
        wantedMaxDurationExpression = config.getString("pvp.wanted.max-duration");

        dateTimeFormat = config.getString("general.date-time-format");

        useTimeValue = config.getString("times.use-both-system-and-worlds-time");

        cancelWantedKarmaGain = config.getString("pvp.wanted.cancel-karma-change.wanted.on-karma-gain") != null
                && config.getBoolean("pvp.wanted.cancel-karma-change.wanted.on-karma-gain");
        cancelWantedKarmaLoss = config.getString("pvp.wanted.cancel-karma-change.wanted.on-karma-loss") != null
                && config.getBoolean("pvp.wanted.cancel-karma-change.wanted.on-karma-loss");
        cancelInnocentKarmaGain = config.getString("pvp.wanted.cancel-karma-change.innocent.on-karma-gain") != null
                && config.getBoolean("pvp.wanted.cancel-karma-change.innocent.on-karma-gain");
        cancelInnocentKarmaLoss = config.getString("pvp.wanted.cancel-karma-change.innocent.on-karma-loss") != null
                && config.getBoolean("pvp.wanted.cancel-karma-change.innocent.on-karma-loss");
    }

    public static void init(FileConfiguration config) {
        configValues = new ConfigData(config);
    }

    public static ConfigData getConfigData() {
        return configValues;
    }

    public String getDateTimeFormat() {
        if (dateTimeFormat == null) {
            return "yyyy-MM-dd HH:mm:ss";
        }
        return dateTimeFormat;
    }
}
