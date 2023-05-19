package com.rosstail.karma;

import com.rosstail.karma.overtime.OvertimeLoop;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigData {
    private static ConfigData configValues;

    public final boolean debugMode;

    public final double defaultKarma;
    public final double minKarma;
    public final double maxKarma;

    public final String killedByTierPath;

    public final int decNumber;
    public final int titleFadeIn;
    public final int titleStay;
    public final int titleFadeOut;
    public final int pvpHitMessageDelay;
    public final int pvpKillMessageDelay;
    public final int pveHitMessageDelay;
    public final int pveKillMessageDelay;
    public final int saveDelay;

    public final boolean overtimeActive;
    public final Map<String, OvertimeLoop> overtimeLoopMap = new HashMap<>();

    public final boolean useWorldGuard;
    public final boolean wantedEnable;
    public final boolean wantedOnKarmaGain;
    public final boolean wantedOnKarmaUnchanged;
    public final boolean wantedOnKarmaLoss;
    public final boolean wantedRefresh;
    public final boolean wantedCountdownApplyOnDisconnect;
    public final boolean cancelWantedKarmaGain;
    public final boolean cancelWantedKarmaLoss;
    public final boolean cancelInnocentKarmaGain;
    public final boolean cancelInnocentKarmaLoss;

    public final String useTimeValue;
    private final String dateTimeFormat;
    private final String countDownFormat;
    public final String pvpHitRewardExpression;
    public final String pvpKillRewardExpression;
    public final String wantedDurationExpression;
    public final String wantedMaxDurationExpression;

    public final ArrayList<String> enterWantedCommands = new ArrayList<>();
    public final ArrayList<String> refreshWantedCommands = new ArrayList<>();
    public final ArrayList<String> leaveWantedCommands = new ArrayList<>();

    ConfigData(FileConfiguration config) {
        debugMode = config.getBoolean("general.debug-mode");

        defaultKarma = config.getDouble("karma.default");
        minKarma = config.getDouble("karma.minimum");
        maxKarma = config.getDouble("karma.maximum");

        String pluginName = Karma.getInstance().getName();
        killedByTierPath = "tiers.list.%" + pluginName.toLowerCase() + "_victim_tier%.commands.killed-commands.%" + pluginName.toLowerCase() + "_attacker_tier%";

        decNumber = config.getInt("general.decimal-display");
        pvpHitMessageDelay = config.getInt("pvp.messages-delay.hit");
        pvpKillMessageDelay = config.getInt("pvp.messages-delay.kill");
        pveHitMessageDelay = config.getInt("entities.messages-delay.hit");
        pveKillMessageDelay = config.getInt("entities.messages-delay.kill");
        titleFadeIn = config.getInt("general.title.fade-in");
        titleStay = config.getInt("general.title.stay");
        titleFadeOut = config.getInt("general.title.fade-out");

        int saveDelay = config.getInt("data-save-delay", 300);
        if (saveDelay <= 0) {
            saveDelay = 300;
        }
        this.saveDelay = saveDelay * 1000;

        pvpHitRewardExpression = config.getString("pvp.hit-reward-expression");
        pvpKillRewardExpression = config.getString("pvp.kill-reward-expression");

        overtimeActive = config.getBoolean("overtime.enable", false);
        if (overtimeActive) {
            config.getConfigurationSection("overtime.loops").getKeys(false).forEach(s -> {
                OvertimeLoop loop = new OvertimeLoop(config.getConfigurationSection("overtime.loops." + s));
                overtimeLoopMap.put(s, loop);
            });
        }

        useWorldGuard = Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && config.getBoolean("general.use-worldguard", false);
        wantedEnable = config.getBoolean("pvp.wanted.enable", true);
        wantedCountdownApplyOnDisconnect = config.getBoolean("pvp.wanted.countdown-on-disconnect", true);
        wantedOnKarmaGain = config.getBoolean("pvp.wanted.conditions.on-karma-gain", false);
        wantedOnKarmaUnchanged = config.getBoolean("pvp.wanted.conditions.on-karma-unchanged", false);
        wantedOnKarmaLoss = config.getBoolean("pvp.wanted.conditions.on-karma-loss", true);
        wantedRefresh = config.getBoolean("pvp.wanted.conditions.refresh", true);
        wantedDurationExpression = config.getString("pvp.wanted.duration");
        wantedMaxDurationExpression = config.getString("pvp.wanted.max-duration");

        dateTimeFormat = config.getString("general.date-time-format");
        countDownFormat = config.getString("general.countdown-format");

        useTimeValue = config.getString("times.use-both-system-and-worlds-time");

        cancelWantedKarmaGain = config.getBoolean("pvp.wanted.cancel-karma-change.wanted.on-karma-gain", true);
        cancelWantedKarmaLoss = config.getBoolean("pvp.wanted.cancel-karma-change.wanted.on-karma-loss", false);
        cancelInnocentKarmaGain = config.getBoolean("pvp.wanted.cancel-karma-change.innocent.on-karma-gain", false);
        cancelInnocentKarmaLoss = config.getBoolean("pvp.wanted.cancel-karma-change.innocent.on-karma-loss", true);
        enterWantedCommands.addAll(config.getStringList("pvp.wanted.commands.enter"));
        refreshWantedCommands.addAll(config.getStringList("pvp.wanted.commands.refresh"));
        leaveWantedCommands.addAll(config.getStringList("pvp.wanted.commands.exit"));
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

    public String getCountdownFormat() {
        if (countDownFormat == null) {
            return "{dd} {HH}:{mm}:{ss}";
        }
        return countDownFormat;
    }
}
