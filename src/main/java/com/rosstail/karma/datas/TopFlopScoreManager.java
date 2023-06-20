package com.rosstail.karma.datas;

import com.rosstail.karma.ConfigData;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class TopFlopScoreManager {
    private static final TopFlopScoreManager topFlopScoreManager = new TopFlopScoreManager();
    private final List<AbstractMap.SimpleEntry<String, Double>> playerTopScoreList = new ArrayList<>();
    private final List<String> playerTopScoreListDisplay = new ArrayList<>();
    private final List<AbstractMap.SimpleEntry<String, Double>> playerFlopScoreList = new ArrayList<>();
    private final List<String> playerFlopScoreListDisplay = new ArrayList<>();

    public void getScores() {
        DBInteractions dbInteractions  = DBInteractions.getInstance();
        if (dbInteractions != null) {
            int limit = ConfigData.getConfigData().topScoreLimit;
            playerTopScoreList.addAll(dbInteractions.getPlayersKarmaTop("DESC", limit));
            playerFlopScoreList.addAll(dbInteractions.getPlayersKarmaTop("ASC", limit));

            playerTopScoreList.forEach(stringDoubleSimpleEntry -> {
                String uuid = stringDoubleSimpleEntry.getKey();
                String playerName = getPlayerNameFromUUID(uuid);
                playerTopScoreListDisplay.add(playerName);
            });
            playerFlopScoreList.forEach(stringDoubleSimpleEntry -> {
                String uuid = stringDoubleSimpleEntry.getKey();
                String playerName = getPlayerNameFromUUID(uuid);
                playerFlopScoreListDisplay.add(playerName);
            });
        } else {
            System.out.println("Not compatible yet with local storage");
        }
    }

    public static TopFlopScoreManager getTopFlopScoreManager() {
        return topFlopScoreManager;
    }

    private String getPlayerNameFromUUID(String uuid) {
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

                // Analyse du JSON manuellement
                playerName = extractPlayerName(response);

            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return playerName;
    }

    private String extractPlayerName(String response) {
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

    public List<AbstractMap.SimpleEntry<String, Double>> getPlayerTopScoreList() {
        return playerTopScoreList;
    }

    public List<AbstractMap.SimpleEntry<String, Double>> getPlayerFlopScoreList() {
        return playerFlopScoreList;
    }

    public List<String> getPlayerTopScoreListDisplay() {
        return playerTopScoreListDisplay;
    }

    public List<String> getPlayerFlopScoreListDisplay() {
        return playerFlopScoreListDisplay;
    }

    public void updateTopFlopScore(Player player, Double karma) {
        for (AbstractMap.SimpleEntry<String, Double> stringDoubleSimpleEntry : getPlayerTopScoreList()) {
        }
    }
}
