package fr.rosstail.karma.configData;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigData {
    private static ConfigData configValues;

    private final double defaultKarma;
    private final double minKarma;
    private final double maxKarma;

    private final int decNumber;
    private final int saveDelay;

    private final long pvpCrimeTimeDelay;

    private final boolean useWorldGuard;
    private final boolean pvpCrimeTimeEnabled;
    private final boolean pvpCrimeTimeOnUp;
    private final boolean pvpCrimeTimeOnStill;
    private final boolean pvpCrimeTimeOnDown;


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

        decNumber = config.getInt("general.decimal-number-to-show");
        pvpCrimeTimeDelay = config.getLong("pvp.crime-time.delay");
        int saveDelay = config.getInt("data-save-delay");
        if (saveDelay == 0) {
            saveDelay = 300;
        }
        this.saveDelay = saveDelay * 1000;

        pvpHitRewardExpression = config.getString("pvp.hit-reward-expression");
        pvpKillRewardExpression = config.getString("pvp.kill-reward-expression");

        useWorldGuard = Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && config.getBoolean("general.use-worldguard");
        pvpCrimeTimeEnabled = config.getBoolean("pvp.crime-time.enable");
        pvpCrimeTimeOnUp = config.getBoolean("pvp.crime-time.active-on-up");
        pvpCrimeTimeOnStill = config.getBoolean("pvp.crime-time.active-on-still");
        pvpCrimeTimeOnDown = config.getBoolean("pvp.crime-time.active-on-down");

        pvpHitMessageKarmaIncrease = config.getString("pvp.hit-message-on-karma-increase");
        pvpKillMessageKarmaIncrease = config.getString("pvp.kill-message-on-karma-increase");
        pvpHitMessageKarmaDecrease = config.getString("pvp.hit-message-on-karma-decrease");
        pvpKillMessageKarmaDecrease = config.getString("pvp.kill-message-on-karma-decrease");
        dateTimeFormat = config.getString("general.date-time-format");
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
}
