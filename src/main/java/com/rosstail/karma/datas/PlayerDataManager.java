package com.rosstail.karma.datas;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.customevents.Cause;
import com.rosstail.karma.customevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.customevents.PlayerOverTimeTriggerEvent;
import com.rosstail.karma.customevents.PlayerWantedPeriodEndEvent;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.overtime.OvertimeLoop;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataManager {
    private static final Karma plugin = Karma.getInstance();
    private static int scheduler;
    private static final AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();

    private static final Map<Player, PlayerData> playerDataMap = new HashMap<>();

    public static PlayerData getSet(Player player) {
        if (!playerDataMap.containsKey(player)) { // If player doesn't have instance
            playerDataMap.put(player, new PlayerData(player));
        }
        return playerDataMap.get(player);
    }

    public static PlayerData getNoSet(Player player) {
        if (!playerDataMap.containsKey(player)) { // If player doesn't have instance
            return new PlayerData(player);
        }
        return playerDataMap.get(player);
    }

    public static Map<Player, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    public static void changePlayerTierMessage(Player player) {
        String message = LangManager.getMessage(LangMessage.TIER_CHANGE);
        if (message != null) {
            adaptMessage.sendToPlayer(player, adaptMessage.adapt(player, message, PlayerType.PLAYER.getText()));
        }
    }

    public static void saveData(DBInteractions.reasons reason, Map<Player, PlayerData> map) {
        DBInteractions dbInteractions = DBInteractions.getInstance();
        if (dbInteractions != null) {
            try {
                dbInteractions.updatePlayersDB(reason, map);
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            for (PlayerData playerData : map.values()) {
                File playerFile = playerData.getPlayerFile();
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                playerConfig.set("karma", playerData.getKarma());
                playerConfig.set("previous-karma", playerData.getPreviousKarma());
                playerConfig.set("tier", playerData.getTier().getName());
                playerConfig.set("previous-tier", playerData.getPreviousTier().getName());
                playerConfig.set("wanted-time", playerData.getWantedTimeLeft());
                try {
                    playerConfig.save(playerFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void setupScheduler() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (PlayerData playerData : PlayerDataManager.getPlayerDataMap().values()) {
                Player player = playerData.player;

                if (ConfigData.getConfigData().overtimeActive) {
                    for (Map.Entry<String, OvertimeLoop> entry : ConfigData.getConfigData().overtimeLoopMap.entrySet()) {
                        String s = entry.getKey();
                        OvertimeLoop overtimeLoop = entry.getValue();

                        if (playerData.isOverTime(overtimeLoop.name)) {
                            PlayerOverTimeTriggerEvent playerOverTimeTriggerEvent = new PlayerOverTimeTriggerEvent(player, s, 1, overtimeLoop.firstTimer);
                            Bukkit.getPluginManager().callEvent(playerOverTimeTriggerEvent);

                            playerData.setOverTimeStamp(s, overtimeLoop.nextTimer);
                        }
                    }
                }

                if (ConfigData.getConfigData().wantedEnable && playerData.isWantedToken() && !playerData.isWanted()) {
                    PlayerWantedPeriodEndEvent playerWantedPeriodEndEvent = new PlayerWantedPeriodEndEvent(player, null);
                    Bukkit.getPluginManager().callEvent(playerWantedPeriodEndEvent);
                }


            }
        }, 20L, 20L);
    }

    public static void triggerOverTime(PlayerData playerData, String overtimeName, int multiplier) {
        Player player = playerData.player;
        double currentKarma = playerData.getKarma();
        double newKarma = currentKarma;

        OvertimeLoop overtimeLoop = ConfigData.getConfigData().overtimeLoopMap.get(overtimeName);

        if (overtimeLoop.hasMinKarma && currentKarma <= overtimeLoop.minKarma) {
            return;
        }
        if (overtimeLoop.hasMaxKarma && currentKarma >= overtimeLoop.maxKarma) {
            return;
        }

        double amount = overtimeLoop.amount;
        if (amount != 0D) {
            amount *= multiplier;
            if (overtimeLoop.hasMaxKarma && currentKarma < overtimeLoop.maxKarma) {
                newKarma = Math.min(currentKarma + amount, overtimeLoop.maxKarma);
            } else if (overtimeLoop.hasMinKarma && currentKarma > overtimeLoop.minKarma) {
                newKarma = Math.max(currentKarma + amount, overtimeLoop.minKarma);
            }

        }

        if (newKarma != currentKarma) {
            PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, newKarma, false, Cause.TIMER);
            Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        }
        CommandManager.commandsLauncher(player, overtimeLoop.commands);
    }

    public static int getScheduler() {
        return scheduler;
    }
}
