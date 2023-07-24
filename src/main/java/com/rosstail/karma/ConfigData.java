package com.rosstail.karma;

import com.rosstail.karma.overtime.OvertimeLoop;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfigData {
    private static ConfigData configData;
    public ConfigStorage storage;
    public ConfigGeneral general;
    public ConfigLocale locale;
    public ConfigKarma karmaConfig;
    public ConfigOvertime overtime;
    public ConfigWanted wanted;
    public ConfigPvp pvp;
    public ConfigPve pve;
    public ConfigTimes times;

    public class ConfigStorage {
        public FileConfiguration configFile;
        public String storageType;
        public String storageHost;
        public short storagePort;
        public String storageDatabase;
        public String storageUser;
        public String storagePass;
        public final int saveDelay;

        ConfigStorage(FileConfiguration config) {
            this.configFile = config;
            storageType = config.getString("storage.type", "LocalStorage");
            storageHost = config.getString("storage.host");
            storagePort = (short) config.getInt("storage.port", 3306);
            storageDatabase = config.getString("storage.database");
            storageUser = config.getString("storage.username");
            storagePass = config.getString("storage.password");

            int saveDelay = config.getInt("storage.save-delay", 300);
            if (saveDelay <= 0) {
                saveDelay = 300;
            }
            this.saveDelay = saveDelay * 1000;
        }
    }

    public class ConfigLocale {
        public FileConfiguration configFile;

        public String lang;
        public int decNumber;
        public int titleFadeIn;
        public int titleStay;
        public int titleFadeOut;
        public String dateTimeFormat;
        public String countDownFormat;

        ConfigLocale(FileConfiguration config) {
            this.configFile = config;

            decNumber = config.getInt("locale.decimal-display");
            titleFadeIn = config.getInt("locale.title.fade-in");
            titleStay = config.getInt("locale.title.stay");
            titleFadeOut = config.getInt("locale.title.fade-out");
            dateTimeFormat = config.getString("locale.date-time-format");
            countDownFormat = config.getString("locale.countdown-format");
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

    public class ConfigGeneral {
        public FileConfiguration configFile;

        public final int topScoreLimit;
        public final boolean useWorldGuard;

        ConfigGeneral(FileConfiguration config) {
            topScoreLimit = config.getInt("general.topscore-limit", 10);
            useWorldGuard = Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && config.getBoolean("general.use-worldguard", false);

        }
    }

    public class ConfigKarma {
        public FileConfiguration fileConfig;
        public float defaultKarma;
        public float minKarma;
        public float maxKarma;

        ConfigKarma(FileConfiguration config) {
            fileConfig = config;

            defaultKarma = (float) config.getDouble("karma.default");
            minKarma = (float) config.getDouble("karma.minimum", Float.MIN_VALUE);
            maxKarma = (float) config.getDouble("karma.maximum", Float.MAX_VALUE);
        }
    }

    public class ConfigOvertime {
        FileConfiguration fileConfig;

        public final boolean overtimeActive;
        public final Map<String, OvertimeLoop> overtimeLoopMap = new HashMap<>();

        ConfigOvertime(FileConfiguration config) {
            this.fileConfig = config;

            overtimeActive = config.getBoolean("overtime.enable", false);
            if (overtimeActive) {
                config.getConfigurationSection("overtime.loops").getKeys(false).forEach(s -> {
                    OvertimeLoop loop = new OvertimeLoop(config.getConfigurationSection("overtime.loops." + s));
                    overtimeLoopMap.put(s, loop);
                });
            }
        }
    }

    public class ConfigWanted {
        FileConfiguration fileConfig;

        public final boolean wantedEnable;
        public final String wantedMaxDurationExpression;
        public final boolean wantedCountdownApplyOnDisconnect;
        public final ArrayList<String> enterWantedCommands = new ArrayList<>();
        public final ArrayList<String> refreshWantedCommands = new ArrayList<>();
        public final ArrayList<String> leaveWantedCommands = new ArrayList<>();

        ConfigWanted(FileConfiguration config) {
            fileConfig = config;

            wantedMaxDurationExpression = config.getString("wanted.maximum-duration");
            wantedEnable = config.getBoolean("wanted.enable", true);
            wantedCountdownApplyOnDisconnect = config.getBoolean("wanted.countdown-on-disconnect", true);

            enterWantedCommands.addAll(config.getStringList("wanted.commands.enter"));
            refreshWantedCommands.addAll(config.getStringList("wanted.commands.refresh"));
            leaveWantedCommands.addAll(config.getStringList("wanted.commands.exit"));
        }
    }

    public class ConfigPvp {
        FileConfiguration fileConfig;

        public final boolean scoreboardTeamSystemCancel;
        public final boolean scoreboardTeamSystemCancelSameTeam;
        public final boolean scoreboardTeamSystemCancelOtherTeam;

        public final int pvpHitMessageDelay;
        public final int pvpKillMessageDelay;
        public final String pvpHitRewardExpression;
        public final String pvpKillRewardExpression;
        public final String wantedHitDurationExpression;
        public final String wantedKillDurationExpression;

        public final boolean wantedOnKarmaGain;
        public final boolean wantedOnKarmaUnchanged;
        public final boolean wantedOnKarmaLoss;
        public final boolean wantedRefresh;
        public final boolean cancelWantedKarmaGain;
        public final boolean cancelWantedKarmaLoss;
        public final boolean cancelInnocentKarmaGain;
        public final boolean cancelInnocentKarmaLoss;
        ConfigPvp(FileConfiguration config) {
            fileConfig = config;

            pvpHitRewardExpression = config.getString("pvp.hit-reward-expression");
            pvpKillRewardExpression = config.getString("pvp.kill-reward-expression");
            pvpHitMessageDelay = config.getInt("pvp.messages-delay.hit");
            pvpKillMessageDelay = config.getInt("pvp.messages-delay.kill");
            wantedHitDurationExpression = config.getString("pvp.wanted.hit-duration");
            wantedKillDurationExpression = config.getString("pvp.wanted.kill-duration");
            scoreboardTeamSystemCancel = config.getBoolean("pvp.team-system-cancel.scoreboard-team.enable", true);
            scoreboardTeamSystemCancelSameTeam = config.getBoolean("pvp.team-system-cancel.scoreboard-team.same-team", true);
            scoreboardTeamSystemCancelOtherTeam = config.getBoolean("pvp.team-system-cancel.scoreboard-team.other-team", false);

            wantedOnKarmaGain = config.getBoolean("pvp.wanted.conditions.on-karma-gain", false);
            wantedOnKarmaUnchanged = config.getBoolean("pvp.wanted.conditions.on-karma-unchanged", false);
            wantedOnKarmaLoss = config.getBoolean("pvp.wanted.conditions.on-karma-loss", true);
            wantedRefresh = config.getBoolean("pvp.wanted.conditions.refresh", true);
            cancelWantedKarmaGain = config.getBoolean("pvp.wanted.cancel-karma-change.wanted.on-karma-gain", true);
            cancelWantedKarmaLoss = config.getBoolean("pvp.wanted.cancel-karma-change.wanted.on-karma-loss", false);
            cancelInnocentKarmaGain = config.getBoolean("pvp.wanted.cancel-karma-change.innocent.on-karma-gain", false);
            cancelInnocentKarmaLoss = config.getBoolean("pvp.wanted.cancel-karma-change.innocent.on-karma-loss", true);
        }
    }

    public class ConfigPve {
        FileConfiguration fileConfig;

        public final int pveHitMessageDelay;
        public final int pveKillMessageDelay;

        ConfigPve(FileConfiguration config) {
            fileConfig = config;

            pveHitMessageDelay = config.getInt("entities.messages-delay.hit");
            pveKillMessageDelay = config.getInt("entities.messages-delay.kill");
        }
    }

    public class ConfigTimes {
        FileConfiguration fileConfig;

        public final String useTimeValue;

        ConfigTimes(FileConfiguration config) {
            fileConfig = config;
            useTimeValue = config.getString("times.use-both-system-and-worlds-time");
        }
    }

    public final FileConfiguration config;

    ConfigData(FileConfiguration config) {
        this.config = config;

        this.storage = new ConfigStorage(config);
        this.locale = new ConfigLocale(config);
        this.general = new ConfigGeneral(config);
        this.karmaConfig = new ConfigKarma(config);
        this.overtime = new ConfigOvertime(config);
        this.wanted = new ConfigWanted(config);
        this.pvp = new ConfigPvp(config);
        this.pve = new ConfigPve(config);
        this.times = new ConfigTimes(config);
    }

    public static void init(FileConfiguration config) {
        configData = new ConfigData(config);
    }

    public static ConfigData getConfigData() {
        return configData;
    }

}
