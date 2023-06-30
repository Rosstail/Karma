package com.rosstail.karma.datas;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.events.karmaevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.events.karmaevents.PlayerOverTimeTriggerEvent;
import com.rosstail.karma.events.karmaevents.PlayerWantedPeriodEndEvent;
import com.rosstail.karma.datas.storage.StorageManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.overtime.OvertimeLoop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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

    public static void changePlayerTierMessage(Player player) {
        String message = LangManager.getMessage(LangMessage.TIER_CHANGE);
        if (message != null) {
            adaptMessage.sendToPlayer(player, adaptMessage.adapt(player, message, PlayerType.PLAYER.getText()));
        }
    }

    public static void setupScheduler() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Map.Entry<String, PlayerModel> e : PlayerDataManager.getPlayerModelMap().entrySet()) {
                String username = e.getKey();
                PlayerModel model = e.getValue();

                Player player = Bukkit.getPlayer(username);

                if (ConfigData.getConfigData().overtimeActive) {
                    for (Map.Entry<String, OvertimeLoop> entry : ConfigData.getConfigData().overtimeLoopMap.entrySet()) {
                        String s = entry.getKey();
                        OvertimeLoop overtimeLoop = entry.getValue();

                        if (model.getOverTimeStampMap().get(overtimeLoop.name).getTime() <= 0L) {
                            PlayerOverTimeTriggerEvent playerOverTimeTriggerEvent = new PlayerOverTimeTriggerEvent(player, s, 1, overtimeLoop.firstTimer);
                            Bukkit.getPluginManager().callEvent(playerOverTimeTriggerEvent);

                            model.getOverTimeStampMap().put(s, new Timestamp(System.currentTimeMillis() + overtimeLoop.nextTimer));
                        }
                    }
                }

                if (ConfigData.getConfigData().wantedEnable && model.isWanted() && !(model.getWantedTimeStamp().getTime() > 0L)) {
                    PlayerWantedPeriodEndEvent playerWantedPeriodEndEvent = new PlayerWantedPeriodEndEvent(player, null);
                    Bukkit.getPluginManager().callEvent(playerWantedPeriodEndEvent);
                }
            }
        }, 20L, 20L);
    }

    /**
     * Set karma of player between karma limits from config
     * @param value
     */
    public static float limitKarma(float value) {
        float min = ConfigData.getConfigData().minKarma;
        float max = ConfigData.getConfigData().maxKarma;
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

        OvertimeLoop overtimeLoop = ConfigData.getConfigData().overtimeLoopMap.get(overtimeName);

        if (overtimeLoop.hasMinKarma && currentKarma <= overtimeLoop.minKarma) {
            return;
        }
        if (overtimeLoop.hasMaxKarma && currentKarma >= overtimeLoop.maxKarma) {
            return;
        }

        float amount = overtimeLoop.amount;
        if (amount != 0F) {
            amount *= multiplier;
            if (overtimeLoop.hasMaxKarma && currentKarma < overtimeLoop.maxKarma) {
                newKarma = Math.min(currentKarma + amount, overtimeLoop.maxKarma);
            } else if (overtimeLoop.hasMinKarma && currentKarma > overtimeLoop.minKarma) {
                newKarma = Math.max(currentKarma + amount, overtimeLoop.minKarma);
            }

        }

        if (newKarma != currentKarma) {
            PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, model, newKarma);
            Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        }
        CommandManager.commandsLauncher(player, overtimeLoop.commands);
    }

    public static String getPlayerNameFromUUID(String uuid) {
        String playerName = "UnknownPlayer";
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

                playerName = extractPlayerNameFromUUID(response);

            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return playerName;
    }

    private static String extractPlayerNameFromUUID(String response) {
        // Analyse du JSON manuellement
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

    public static String getPlayerUUIDFromName(String username) {
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
                    System.out.println("Impossible de récupérer l'UUID du joueur " + username);
                }
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                System.out.println("Le joueur " + username + " n'a pas été trouvé.");
            } else {
                System.out.println("Erreur lors de la requête HTTP : " + responseCode);
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
            StorageManager.getManager().updatePlayerModel(model);
        });
    }
}
