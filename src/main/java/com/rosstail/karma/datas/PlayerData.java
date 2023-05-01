package com.rosstail.karma.datas;

import com.rosstail.karma.Karma;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.customevents.*;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

/**
 * Gonna be used to optimize the research of values
 */
public class PlayerData {
    private static final Karma plugin = Karma.getInstance();

    private final File playerFile;
    final Player player;
    private double karma;
    private double previousKarma;
    private Tier tier;
    private Tier previousTier;
    private long lastUpdate = 0;
    private Timestamp wantedTimeStamp = new Timestamp(0L);
    private Timestamp overTimeStamp = new Timestamp(0L);
    private boolean wantedToken;

    PlayerData(Player player) {
        this.player = player;
        playerFile = new File(plugin.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
    }

    public double getKarma() {
        return karma;
    }

    public Tier getTier() {
        if (tier != null) {
            return tier;
        }
        return TierManager.getNoTier();
    }

    public Timestamp getWantedTimeStamp() {
        return wantedTimeStamp; //milliseconds are important only for calculations
    }

    public double getPreviousKarma() {
        return previousKarma;
    }

    public Tier getPreviousTier() {
        if (previousTier != null) {
            return previousTier;
        }
        return TierManager.getNoTier();
    }

    /**
     * Load data of player or initiate it
     */
    public void loadPlayerData() {
        DBInteractions dbInteractions = DBInteractions.getInstance();
        if (dbInteractions != null) {
            if (!dbInteractions.getPlayerData(player)) {
                dbInteractions.initPlayerDB(player);
            }
        } else if (!playerFile.exists()) {
            initPlayerDataLocale();
        } else {
            loadPlayerDataFile();
        }

        long nextDelay = ConfigData.getConfigData().overtimeFirstDelay;

        if (ConfigData.getConfigData().isOvertimeActive) {
            if (ConfigData.getConfigData().isOvertimeCountdownOnDisconnect) {
                long deltaUpdates = System.currentTimeMillis() - lastUpdate;
                deltaUpdates -= ConfigData.getConfigData().overtimeFirstDelay;
                if (deltaUpdates >= 0f) {
                    int occurrence = (int) (Math.floorDiv(deltaUpdates, ConfigData.getConfigData().overtimeNextDelay) + 1);
                    nextDelay = deltaUpdates % ConfigData.getConfigData().overtimeNextDelay;
                    new PlayerOverTimeTriggerEvent(player, occurrence, nextDelay);
                    Bukkit.getPluginManager().callEvent(new PlayerOverTimeTriggerEvent(player, occurrence, nextDelay));
                } else {
                    nextDelay = -deltaUpdates;
                }
            }
        }
        setOverTimeStamp(nextDelay);
    }

    public void loadPlayerDataFile() {
        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        karma = playerConfig.getDouble("karma");
        previousKarma = playerConfig.getDouble("previous-karma");
        tier = TierManager.getTierManager().getTiers().get(playerConfig.getString("tier"));
        previousTier = TierManager.getTierManager().getTiers().get(playerConfig.getString("previous-tier"));
        lastUpdate = playerFile.lastModified();

        wantedTimeStamp = new Timestamp(
                (ConfigData.getConfigData().wantedCountdownApplyOnDisconnect
                        ? playerFile.lastModified()
                        : System.currentTimeMillis())
                        + playerConfig.getLong("wanted-time"));
        if (ConfigData.getConfigData().wantedEnable && isWanted()) {
            PlayerWantedPeriodRefreshEvent event = new PlayerWantedPeriodRefreshEvent(player, Cause.OTHER, true);
            Bukkit.getPluginManager().callEvent(event);

            AdaptMessage.getAdaptMessage().sendToPlayer(player, AdaptMessage.getAdaptMessage().adapt(player, LangManager.getMessage(LangMessage.WANTED_CONNECT_REFRESH), PlayerType.PLAYER.getText()));
        }
    }

    private void initPlayerDataLocale() {
        double defaultKarma = ConfigData.getConfigData().defaultKarma;

        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, defaultKarma, true, Cause.OTHER);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        lastUpdate = System.currentTimeMillis();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            playerConfig.set("karma", getKarma());
            playerConfig.set("tier", getTier().getName());
            try {
                playerConfig.save(playerFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * /**
     * Update the new karma of the player if change is needed.
     * Uses local files or Database if connection is active
     *
     * @param value -> The new karma amount of the player
     */
    public void setKarma(double value) {
        double min = ConfigData.getConfigData().minKarma;
        double max = ConfigData.getConfigData().maxKarma;
        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }

        setPreviousKarma(this.karma);
        this.karma = value;
    }

    public void setPreviousKarma(double previousKarma) {
        this.previousKarma = previousKarma;
    }

    public void checkKarma() {
        ConfigData configData = ConfigData.getConfigData();
        if (!(karma >= configData.minKarma && karma <= configData.maxKarma)) {
            double newKarma = configData.minKarma;
            if (karma > configData.maxKarma) {
                newKarma = configData.maxKarma;
            }
            PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, newKarma, true, Cause.COMMAND);
            Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        }
    }
    public void checkTier() {
        for (Tier tier : TierManager.getTierManager().getTiers().values()) {
            if (karma >= tier.getMinKarma() && karma <= tier.getMaxKarma() && !tier.equals(getTier())) {
                PlayerTierChangeEvent playerTierChangeEvent = new PlayerTierChangeEvent(player, tier);
                Bukkit.getPluginManager().callEvent(playerTierChangeEvent);
                break;
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setTier(Tier tier) {
        this.tier = tier;
    }

    public void setPreviousTier(Tier previousTier) {
        this.previousTier = previousTier;
    }

    public long getWantedTime() {
        return Math.max(0L, wantedTimeStamp.getTime() - System.currentTimeMillis());
    }

    public long getOverTime() {
        return overTimeStamp.getTime() - System.currentTimeMillis();
    }

    public static void stopTimer(int scheduler) {
        Bukkit.getScheduler().cancelTask(scheduler);
    }

    /**
     * Set the timestamp of the player's attack moment if needed
     */
    public void setWantedTimeStamp(Timestamp wantedTimeStamp) {
        if (player.hasMetadata("NPC")) {
            return;
        }
        this.wantedTimeStamp = wantedTimeStamp;
    }

    public boolean isWanted() {
        return getWantedTime() > 0L;
    }

    public boolean isOverTime() {
        return getOverTime() <= 0L;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isWantedToken() {
        return wantedToken;
    }

    public void setWantedToken(boolean wantedToken) {
        this.wantedToken = wantedToken;
    }

    public File getPlayerFile() {
        return playerFile;
    }

    public void setOverTimeStamp(long value) {
        this.overTimeStamp = new Timestamp(System.currentTimeMillis() + value);
    }
}
