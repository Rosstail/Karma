package com.rosstail.karma.players;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.events.karmaevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.events.karmaevents.PlayerOverTimeTriggerEvent;
import com.rosstail.karma.events.karmaevents.PlayerWantedPeriodEndEvent;
import com.rosstail.karma.storage.StorageManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.overtime.OvertimeLoop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataManager {
    private static final Karma plugin = Karma.getInstance();
    private static int scheduler;
    private static final AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();

    private static final Map<String, PlayerModel> playerModelMap = new HashMap<>();

    public static PlayerModel initPlayerModelToMap(PlayerModel model) {
        return playerModelMap.put(model.getUsername(), model);
    }

    public static PlayerModel removePlayerModelFromMap(Player player) {
        return playerModelMap.remove(player.getName());
    }

    public static void changePlayerKarmaMessage(Player player) {
        String message = LangManager.getMessage(LangMessage.KARMA_ON_CHANGE);
        if (message != null) {
            adaptMessage.sendToPlayer(player, adaptMessage.adaptMessage(adaptMessage.adaptPlayerMessage(player, message, PlayerType.PLAYER.getText())));
        }
    }

    public static void changePlayerTierMessage(Player player) {
        String message = LangManager.getMessage(LangMessage.TIER_ON_CHANGE);
        if (message != null) {
            adaptMessage.sendToPlayer(player, adaptMessage.adaptMessage(adaptMessage.adaptPlayerMessage(player, message, PlayerType.PLAYER.getText())));
        }
    }

    public static void setupScheduler() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Map.Entry<String, PlayerModel> e : PlayerDataManager.getPlayerModelMap().entrySet()) {
                String username = e.getKey();
                PlayerModel model = e.getValue();

                Player player = Bukkit.getPlayer(username);

                if (ConfigData.getConfigData().overtime.overtimeActive) {
                    for (Map.Entry<String, OvertimeLoop> entry : ConfigData.getConfigData().overtime.overtimeLoopMap.entrySet()) {
                        String s = entry.getKey();
                        OvertimeLoop overtimeLoop = entry.getValue();

                        if (model.getOverTimeStampMap().get(overtimeLoop.name).getTime() - System.currentTimeMillis() <= 0L) {
                            PlayerOverTimeTriggerEvent playerOverTimeTriggerEvent = new PlayerOverTimeTriggerEvent(player, s, 1, overtimeLoop.firstTimer);
                            Bukkit.getPluginManager().callEvent(playerOverTimeTriggerEvent);

                            model.getOverTimeStampMap().put(s, new Timestamp(System.currentTimeMillis() + overtimeLoop.nextTimer));
                        }
                    }
                }

                if (ConfigData.getConfigData().wanted.wantedEnable && model.isWanted() && !PlayerDataManager.isWanted(model)) {
                    PlayerWantedPeriodEndEvent playerWantedPeriodEndEvent = new PlayerWantedPeriodEndEvent(player, model, !ConfigData.getConfigData().pvp.sendMessageOnWantedChange);
                    Bukkit.getPluginManager().callEvent(playerWantedPeriodEndEvent);
                }
            }
        }, 20L, 20L);
    }

    /**
     * Set karma of player between karma limits from config
     *
     * @param value
     */
    public static float limitKarma(float value) {
        float min = ConfigData.getConfigData().karmaConfig.minKarma;
        float max = ConfigData.getConfigData().karmaConfig.maxKarma;
        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }
        return value;
    }

    public static void triggerOverTime(Player player, PlayerModel model, String overtimeName, int multiplier) {
        float currentKarma = model.getKarma();
        float newKarma = currentKarma;
        OvertimeLoop overtimeLoop = ConfigData.getConfigData().overtime.overtimeLoopMap.get(overtimeName);
        float amount = overtimeLoop.amount;

        if (overtimeLoop.hasMinKarma && currentKarma <= overtimeLoop.minKarma) {
            return;
        }
        if (overtimeLoop.hasMaxKarma && currentKarma >= overtimeLoop.maxKarma) {
            return;
        }

        if (overtimeLoop.hasMinKarma && currentKarma + amount < overtimeLoop.minKarma) {
            amount = overtimeLoop.minKarma - currentKarma;
        }
        if (overtimeLoop.hasMaxKarma && currentKarma + amount > overtimeLoop.maxKarma) {
            amount = overtimeLoop.maxKarma - currentKarma;
        }

        if (amount != 0F) {
            amount *= multiplier;
            if (overtimeLoop.hasMaxKarma) {
                newKarma = Math.min(currentKarma + amount, overtimeLoop.maxKarma);
            } else if (overtimeLoop.hasMinKarma) {
                newKarma = Math.max(currentKarma + amount, overtimeLoop.minKarma);
            }

        }

        if (newKarma != currentKarma) {
            PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, model, newKarma, !ConfigData.getConfigData().pvp.sendMessageOnKarmaChange);
            Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        }
        CommandManager.commandsLauncher(player, overtimeLoop.commands);
    }

    public static String getPlayerNameFromUUID(String uuid) {
        if (!Bukkit.getOnlineMode()) {
            return uuid;
        }
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();

                String response = responseBuilder.toString();

                return extractPlayerNameFromUUID(response);

            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get player name using UUID from Mojang API
     *
     * @param response
     * @return
     */
    private static String extractPlayerNameFromUUID(String response) {
        int index = response.indexOf("\"name\" : \"");
        if (index != -1) {
            int startIndex = index + "\"name\" : \"".length();
            int endIndex = response.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return response.substring(startIndex, endIndex);
            }
        }
        return null;
    }

    /**
     * Get player name using username from Mojang API
     *
     * @param username the name of targeted player
     * @return
     */
    public static String getPlayerUUIDFromName(String username) {
        if (!Bukkit.getOnlineMode()) {
            return username;
        }
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                reader.close();

                String uuid = extractUUID(responseBuilder.toString());

                if (uuid != null) {
                    return uuid.substring(0, 8) + "-" +
                            uuid.substring(8, 12) + "-" +
                            uuid.substring(12, 16) + "-" +
                            uuid.substring(16, 20) + "-" +
                            uuid.substring(20);
                } else {
                    AdaptMessage.print("Impossible to get UUID of " + username, AdaptMessage.prints.WARNING);
                }
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                AdaptMessage.print(LangManager.getMessage(LangMessage.COMMANDS_PLAYER_DOES_NOT_EXIST).replaceAll("\\[player]", username), AdaptMessage.prints.WARNING);
            } else {
                AdaptMessage.print("HTTP request error in PlayerDataManager#getPlayerUUIDFromName\n" + responseCode, AdaptMessage.prints.WARNING);
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String extractUUID(String response) {
        int index = response.indexOf("\"id\" : \"");
        if (index != -1) {
            int startIndex = index + "\"id\" : \"".length();
            int endIndex = response.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return response.substring(startIndex, endIndex);
            }
        }
        return null;
    }

    public static int getScheduler() {
        return scheduler;
    }

    public static Map<String, PlayerModel> getPlayerModelMap() {
        return playerModelMap;
    }

    public static long getWantedTime(PlayerModel model) {
        return model.getWantedTimeStamp().getTime();
    }

    public static long getWantedTimeLeft(PlayerModel model) {
        return Math.max(0L, getWantedTime(model) - System.currentTimeMillis());
    }

    public static long getOvertime(PlayerModel model, String name) {
        return model.getOverTimeStampMap().get(name).getTime() - System.currentTimeMillis();
    }

    public boolean isOverTime(PlayerModel model, String name) {
        return getOvertime(model, name) <= 0L;
    }

    public static void stopTimer(int scheduler) {
        Bukkit.getScheduler().cancelTask(scheduler);
    }

    public static boolean isWanted(PlayerModel model) {
        return getWantedTimeLeft(model) > 0L;
    }


    public static void setOverTimeStamp(PlayerModel model, String name, long value) {
        model.getOverTimeStampMap().put(name, new Timestamp(System.currentTimeMillis() + value));
    }

    public static void saveAllPlayerModelToStorage() {
        getPlayerModelMap().forEach((s, model) -> {
            StorageManager.getManager().updatePlayerModel(model, true);
        });
    }
}
