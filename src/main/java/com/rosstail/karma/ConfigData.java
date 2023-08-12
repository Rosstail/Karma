package com.rosstail.karma;

import com.rosstail.karma.overtime.OvertimeLoop;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfigData {
    private final Karma plugin = Karma.getInstance();
    private static ConfigData configData;

    public final ConfigStorage storage;
    public final ConfigGeneral general;
    public final ConfigLocale locale;
    public final ConfigKarma karmaConfig;
    public final ConfigTiers tiers;
    public final ConfigOvertime overtime;
    public final ConfigWanted wanted;
    public final ConfigPvp pvp;
    public final ConfigPve pve;
    public final ConfigTimes times;

    public class ConfigStorage {
        public final FileConfiguration configFile;
        public final String storageType;
        public final String storageHost;
        public final short storagePort;
        public final String storageDatabase;
        public final String storageUser;
        public final String storagePass;
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
        public final FileConfiguration configFile;

        public final String lang;
        public final int decNumber;
        public final int titleFadeIn;
        public final int titleStay;
        public final int titleFadeOut;
        //public final String dateTimeFormat;
        //public final String countDownFormat;

        ConfigLocale(FileConfiguration config) {
            this.configFile = config;

            lang = config.getString("locale.lang");
            decNumber = config.getInt("locale.decimal-display");
            titleFadeIn = config.getInt("locale.title.fade-in");
            titleStay = config.getInt("locale.title.stay");
            titleFadeOut = config.getInt("locale.title.fade-out");
            //dateTimeFormat = config.getString("locale.datetime-format");
            //countDownFormat = config.getString("locale.countdown-format");
        }

        /*public String getDateTimeFormat() {
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
        }*/
    }

    public class ConfigGeneral {
        public FileConfiguration configFile;

        public final float configVersion;
        public final int topScoreLimit;
        public final boolean useWorldGuard;

        ConfigGeneral(FileConfiguration config) {
            configFile = config;

            configVersion = (float) config.getDouble("general.config-version", 1.0F);
            topScoreLimit = config.getInt("general.topscore-limit", 10);
            useWorldGuard = Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && config.getBoolean("general.use-worldguard", false);

        }
    }

    public class ConfigKarma {
        public final FileConfiguration fileConfig;
        public final float defaultKarma;
        public final float minKarma;
        public final float maxKarma;

        ConfigKarma(FileConfiguration config) {
            fileConfig = config;

            defaultKarma = (float) config.getDouble("karma.default");
            minKarma = (float) config.getDouble("karma.minimum", Float.MIN_VALUE);
            maxKarma = (float) config.getDouble("karma.maximum", Float.MAX_VALUE);
        }
    }

    public class ConfigTiers {
        public final FileConfiguration fileConfig;

        ConfigTiers(FileConfiguration config) {
            this.fileConfig = config;
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
        public final String pvpHitAttackerChangeExpression;
        public final String pvpKillAttackerChangeExpression;
        public final String pvpHitVictimChangeExpression;
        public final String pvpKillVictimChangeExpression;
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

            pvpHitAttackerChangeExpression = config.getString("pvp.hit-attacker-change-expression");
            pvpKillAttackerChangeExpression = config.getString("pvp.kill-attacker-change-expression");
            pvpHitVictimChangeExpression = config.getString("pvp.hit-victim-change-expression");
            pvpKillVictimChangeExpression = config.getString("pvp.kill-victim-change-expression");
            pvpHitMessageDelay = config.getInt("pvp.messages-delay.hit");
            pvpKillMessageDelay = config.getInt("pvp.messages-delay.kill");
            wantedHitDurationExpression = config.getString("pvp.wanted.hit-duration");
            wantedKillDurationExpression = config.getString("pvp.wanted.kill-duration");
            scoreboardTeamSystemCancel = config.getBoolean("pvp.team-system-cancel.scoreboard-team.enable", true);
            scoreboardTeamSystemCancelSameTeam = config.getBoolean("pvp.team-system-cancel.scoreboard-team.same-team", true);
            scoreboardTeamSystemCancelOtherTeam = config.getBoolean("pvp.team-system-cancel.scoreboard-team.other-team", false);

            wantedOnKarmaGain = config.getBoolean("pvp.wanted.requirements.on-karma-gain", false);
            wantedOnKarmaUnchanged = config.getBoolean("pvp.wanted.requirements.on-karma-unchanged", false);
            wantedOnKarmaLoss = config.getBoolean("pvp.wanted.requirements.on-karma-loss", true);
            wantedRefresh = config.getBoolean("pvp.wanted.requirements.refresh", true);
            cancelWantedKarmaGain = config.getBoolean("pvp.wanted.cancel-karma-change.wanted.on-karma-gain", true);
            cancelWantedKarmaLoss = config.getBoolean("pvp.wanted.cancel-karma-change.wanted.on-karma-loss", false);
            cancelInnocentKarmaGain = config.getBoolean("pvp.wanted.cancel-karma-change.innocent.on-karma-gain", false);
            cancelInnocentKarmaLoss = config.getBoolean("pvp.wanted.cancel-karma-change.innocent.on-karma-loss", true);
        }
    }

    public class ConfigPve {
        public final FileConfiguration fileConfig;

        public final int pveHitMessageDelay;
        public final int pveKillMessageDelay;

        ConfigPve(FileConfiguration config) {
            fileConfig = config;

            pveHitMessageDelay = config.getInt("pve.messages-delay.hit");
            pveKillMessageDelay = config.getInt("pve.messages-delay.kill");
        }
    }

    public class ConfigTimes {
        FileConfiguration fileConfig;

        public final String useTimeValue;

        ConfigTimes(FileConfiguration config) {
            fileConfig = config;
            useTimeValue = config.getString("time-periods.type");
        }
    }

    public final FileConfiguration config;

    ConfigData(FileConfiguration config) {
        this.config = config;

        this.storage = new ConfigStorage(readConfig(config, "storage"));
        this.locale = new ConfigLocale(readConfig(config, "locale"));
        this.general = new ConfigGeneral(readConfig(config, "general"));
        this.karmaConfig = new ConfigKarma(readConfig(config, "karma"));
        this.tiers = new ConfigTiers(readConfig(config, "tiers"));
        this.overtime = new ConfigOvertime(readConfig(config, "overtime"));
        this.wanted = new ConfigWanted(readConfig(config, "wanted"));
        this.pvp = new ConfigPvp(readConfig(config, "pvp"));
        this.pve = new ConfigPve(readConfig(config, "pve"));
        this.times = new ConfigTimes(readConfig(config, "time-periods"));
    }

    private FileConfiguration readConfig(FileConfiguration baseConfig, String item) {
        try {
            File file = new File("plugins/" + plugin.getName() + "/" + baseConfig.getString(item) + ".yml");
            if (!(file.exists())) {
                return baseConfig;
            }
            return YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            e.printStackTrace();
            //If error such a ConfigurationSection instead of String
            return baseConfig;
        }
    }

    public static void init(FileConfiguration config) {
        configData = new ConfigData(config);
    }

    public static ConfigData getConfigData() {
        return configData;
    }

}
