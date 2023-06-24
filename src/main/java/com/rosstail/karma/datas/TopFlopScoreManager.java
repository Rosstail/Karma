package com.rosstail.karma.datas;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.datas.storage.DBInteractions;
import com.rosstail.karma.datas.storage.StorageManager;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class TopFlopScoreManager {
    private static TopFlopScoreManager topFlopScoreManager;

    private final List<PlayerModel> playerTopScoreList = new ArrayList<>();
    private final List<PlayerModel> playerFlopScoreList = new ArrayList<>();
    private final int limitSize;

    public static void init() {
        if (topFlopScoreManager == null) {
            topFlopScoreManager = new TopFlopScoreManager();
        }
    }

    TopFlopScoreManager() {
        this.limitSize = ConfigData.getConfigData().topScoreLimit;
        presetValuesInList(playerTopScoreList);
        presetValuesInList(playerFlopScoreList);
    }

    private void presetValuesInList(List<PlayerModel> list) {
        while (list.size() < limitSize) {
            list.add(null);
        }
    }

    public void getScores() {
        int limit = ConfigData.getConfigData().topScoreLimit;
        StorageManager storageManager = StorageManager.getManager();
        List<PlayerModel> topScores = storageManager.selectPlayerModelListTop(limit);
        List<PlayerModel> bottomScores = storageManager.selectPlayerModelListBottom(limit);
        int currentLimit = Math.min(topScores.size(), limit);

        for (int index = 0; index < currentLimit; index++) {
            playerTopScoreList.set(index, topScores.get(index));
            playerFlopScoreList.set(index, bottomScores.get(index));
        }
    }

    public static TopFlopScoreManager getTopFlopScoreManager() {
        return topFlopScoreManager;
    }

    public List<PlayerModel> getPlayerTopScoreList() {
        return playerTopScoreList;
    }

    public List<PlayerModel> getPlayerFlopScoreList() {
        return playerFlopScoreList;
    }

    public int getLimitSize() {
        return limitSize;
    }
}
