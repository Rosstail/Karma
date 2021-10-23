package com.rosstail.karma.datas;

import com.rosstail.karma.Karma;
import com.rosstail.karma.commands.KarmaCommand;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.customevents.*;
import com.rosstail.karma.events.Fights;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Gonna be used to optimize the research of values
 */
public class PlayerData {
    private static final Karma plugin = Karma.getInstance();
    private static ConfigData configData = ConfigData.getConfigData();
    private static final AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();

    private static final Map<Player, PlayerData> playerList = new HashMap<Player, PlayerData>();
    private final File playerFile;
    private final Player player;
    private double karma;
    private double previousKarma;
    private Tier tier;
    private Tier previousTier;
    private Timestamp wantedTimeStamp = new Timestamp(0L);
    private Timer updateDataTimer;
    private int overTimerScheduler;
    private int wantedScheduler;
    private boolean wanted;

    private PlayerData(Player player) {
        this.player = player;
        playerFile = new File(plugin.getDataFolder(), "playerdata/" + player.getUniqueId() + ".yml");
        loadPlayerData();
    }

    public static PlayerData gets(Player player) {
        if(!playerList.containsKey(player)){ // If player doesn't have instance
            playerList.put(player, new PlayerData(player));
        }
        return playerList.get(player);
    }

    public Timer getUpdateDataTimer() {
        return updateDataTimer;
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
     * Request the player Tier inside file
     *
     */
    public void loadPlayerData() {
        DBInteractions dbInteractions = DBInteractions.getInstance();
        if (dbInteractions == null) {
            YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
            karma = playerConfig.getDouble("karma");
            previousKarma = playerConfig.getDouble("previous-karma");
            tier = TierManager.getTierManager().getTiers().get(playerConfig.getString("tier"));
            previousTier = TierManager.getTierManager().getTiers().get(playerConfig.getString("previous-tier"));
            wantedTimeStamp = new Timestamp(playerConfig.getLong("wanted-time"));
            wanted = isWanted();
        }
    }

    /**
     * Initialize the file / Line of the player with UUID, name, karma and tier
     *
     */
    public void initPlayerData() {
        DBInteractions dbInteractions = DBInteractions.getInstance();
        if (dbInteractions != null) {
            if (!dbInteractions.getPlayerData(player)) {
                dbInteractions.initPlayerDB(player);
            }
        } else if (!playerFile.exists()){
            initPlayerDataLocale();
        }
    }

    private void initPlayerDataLocale() {
        double defaultKarma = configData.defaultKarma;

        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, defaultKarma, true, Cause.OTHER);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
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
     * @param value  -> The new karma amount of the player
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

    public void updateData() {
        try {
            DBInteractions dbInteractions = DBInteractions.getInstance();
            if (dbInteractions != null) {
                dbInteractions.updatePlayerDB(player);
            } else {
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                playerConfig.set("karma", getKarma());
                playerConfig.set("previous-karma", getPreviousKarma());
                playerConfig.set("tier", getTier().getName());
                playerConfig.set("previous-tier", getPreviousTier().getName());
                playerConfig.set("wanted-time", getWantedTimeStamp().getTime());
                playerConfig.save(playerFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the new tier of the player if change is needed.
     * Uses local files or Database if connection is active
     *
     */
    public void checkTier() {
        for (Tier tier : TierManager.getTierManager().getTiers().values()) {
            if (karma >= tier.getMinKarma() && karma <= tier.getMaxKarma() && !tier.equals(getTier())) {
                PlayerTierChangeEvent playerTierChangeEvent = new PlayerTierChangeEvent(player, tier);
                Bukkit.getPluginManager().callEvent(playerTierChangeEvent);
                break;
            }
        }
    }

    public void setTier(Tier tier) {
        this.tier = tier;
    }

    public void setPreviousTier(Tier previousTier) {
        this.previousTier = previousTier;
    }

    public void setUpdateDataTimer(int delay) {
        this.updateDataTimer = new Timer();
        updateDataTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateData();
            }
        }, delay, delay);;
    }

    public static void setOverTimerChange(Player player) {
        PlayerData playerData = PlayerData.gets(player);
        stopTimer(playerData.getOverTimerScheduler());
        if (!ConfigData.getConfigData().isOvertimeActive) {
            return;
        }
        playerData.setOverTimerScheduler(setupNewOverTime(player));
    }

    public static void replaceWantedScheduler(Player player) {
        PlayerData playerData = PlayerData.gets(player);
        stopTimer(playerData.getWantedScheduler());
        playerData.setWantedScheduler(setupNewWantedPeriod(player));
    }

    private static int setupNewOverTime(Player player) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            PlayerOverTimeTriggerEvent playerOverTimeTriggerEvent = new PlayerOverTimeTriggerEvent(player);
            Bukkit.getPluginManager().callEvent(playerOverTimeTriggerEvent);
        }, ConfigData.getConfigData().overtimeFirstDelay, ConfigData.getConfigData().overtimeNextDelay);
    }
    private static int setupNewWantedPeriod(Player player) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            PlayerData playerData = PlayerData.gets(player);
            if (playerData.isWanted() && !Fights.isPlayerWanted(playerData.getWantedTimeStamp().getTime())) {
                PlayerWantedPeriodEndEvent playerWantedPeriodEndEvent = new PlayerWantedPeriodEndEvent(player, null);
                Bukkit.getPluginManager().callEvent(playerWantedPeriodEndEvent);
            }
        }, 1, 1);
    }

    public void setWanted(boolean b) {
        this.wanted = b;
    }

    public static void triggerOverTime(Player player) {
        PlayerData playerData = PlayerData.gets(player);
        double currentKarma = playerData.getKarma();
        double newKarma = currentKarma;
        double decreaseValue = ConfigData.getConfigData().overtimeDecreaseValue;
        double increaseValue = ConfigData.getConfigData().overtimeIncreaseValue;
        if (decreaseValue > 0) {
            double decreaseLimit = ConfigData.getConfigData().overtimeDecreaseLimit;
            if (currentKarma > decreaseLimit) {
                newKarma = currentKarma - decreaseValue;
                if (newKarma < decreaseLimit) {
                    newKarma = decreaseLimit;
                }

                KarmaCommand.commandsLauncher(player, ConfigData.getConfigData().overtimeDecreaseCommands);
            }
        }
        if (increaseValue > 0) {
            double increaseLimit = ConfigData.getConfigData().overtimeIncreaseLimit;
            if (currentKarma < increaseLimit) {
                newKarma = currentKarma + increaseValue;
                if (newKarma > increaseLimit) {
                    newKarma = increaseLimit;
                }

                KarmaCommand.commandsLauncher(player, ConfigData.getConfigData().overtimeIncreaseCommands);
            }
        }

        if (newKarma != currentKarma) {
            PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, newKarma, false, Cause.TIMER);
            Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        }
    }

    public static void stopTimer(int scheduler) {
        Bukkit.getScheduler().cancelTask(scheduler);
    }

    /**
     * Set the timestamp of the player's attack moment if needed
     *
     */
    public void setWantedTimeStamp(Timestamp lastAttack) {
        if (player.hasMetadata("NPC")) {
            return;
        }
        this.wantedTimeStamp = lastAttack;
    }

    public static void changePlayerTierMessage(Player player) {
        String message = LangManager.getMessage(LangMessage.TIER_CHANGE);
        if (message != null) {
            adaptMessage.sendToPlayer(player, adaptMessage.adapt(player, message, PlayerType.player.toString()));
        }
    }

    public boolean isWanted() {
        return wanted;
    }

    public static Map<Player, PlayerData> getPlayerList() {
        return playerList;
    }

    public int getOverTimerScheduler() {
        return overTimerScheduler;
    }

    public int getWantedScheduler() {
        return wantedScheduler;
    }

    public void setOverTimerScheduler(int overTimerScheduler) {
        this.overTimerScheduler = overTimerScheduler;
    }

    public void setWantedScheduler(int wantedScheduler) {
        this.wantedScheduler = wantedScheduler;
    }

    public static void setConfigData(ConfigData configData) {
        PlayerData.configData = configData;
    }
}
