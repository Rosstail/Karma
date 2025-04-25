package com.rosstail.karma.players;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.storage.StorageManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class TopFlopScoreManager {
    private static TopFlopScoreManager topFlopScoreManager;

    private final List<PlayerDataModel> playerTopScoreList = new ArrayList<>();
    private final List<PlayerDataModel> playerFlopScoreList = new ArrayList<>();
    private final int limitSize;

    public static void init() {
        if (topFlopScoreManager == null) {
            topFlopScoreManager = new TopFlopScoreManager();
        }
    }

    TopFlopScoreManager() {
        this.limitSize = ConfigData.getConfigData().general.topScoreLimit;
        presetValuesInList(playerTopScoreList);
        presetValuesInList(playerFlopScoreList);
    }

    private void presetValuesInList(List<PlayerDataModel> list) {
        while (list.size() < limitSize) {
            list.add(null);
        }
    }

    public void getScores() {
        int limit = ConfigData.getConfigData().general.topScoreLimit;
        StorageManager storageManager = StorageManager.getManager();
        Collection<PlayerDataModel> onlinePlayerDataModels = PlayerDataManager.getPlayerModelMap().values();
        List<PlayerDataModel> orderedPlayerDataModelList = new ArrayList<>();
        for (PlayerDataModel playerDataModel : onlinePlayerDataModels) {
            int index = 0;

            for (PlayerDataModel model : orderedPlayerDataModelList) {
                if (playerDataModel.getKarma() < model.getKarma()) {
                    orderedPlayerDataModelList.add(index, new PlayerDataModel(playerDataModel));
                    break;
                }
                index++;
            }
            if (orderedPlayerDataModelList.isEmpty() || index == orderedPlayerDataModelList.size()) {
                orderedPlayerDataModelList.add(new PlayerDataModel(playerDataModel));
            }
        }

        List<PlayerDataModel> reqTopScores = storageManager.selectPlayerModelListTop(limit);
        List<PlayerDataModel> reqBottomScores = storageManager.selectPlayerModelListBottom(limit);

        for (PlayerDataModel playerDataModel : reqTopScores) {
            int index = 0;

            for (PlayerDataModel model : orderedPlayerDataModelList) {
                if (Objects.equals(playerDataModel.getUuid(), model.getUuid())) {
                    break;
                }
                if (playerDataModel.getKarma() < model.getKarma()) {
                    orderedPlayerDataModelList.add(index, playerDataModel);
                    break;
                }
                index++;
            }
            if (orderedPlayerDataModelList.isEmpty() || index == orderedPlayerDataModelList.size()) {
                orderedPlayerDataModelList.add(playerDataModel);
            }
        }

        for (PlayerDataModel playerDataModel : reqBottomScores) {
            int index = 0;

            for (PlayerDataModel model : orderedPlayerDataModelList) {
                if (Objects.equals(playerDataModel.getUuid(), model.getUuid())) {
                    break;
                }
                if (playerDataModel.getKarma() < model.getKarma()) {
                    orderedPlayerDataModelList.add(index, playerDataModel);
                    break;
                }
                index++;
            }
            if (orderedPlayerDataModelList.isEmpty() || index == orderedPlayerDataModelList.size()) {
                orderedPlayerDataModelList.add(playerDataModel);
            }
        }

        int currentLimit = Math.min(orderedPlayerDataModelList.size(), limit);

        for (int index = 0; index < currentLimit; index++) {
            playerTopScoreList.set(index, orderedPlayerDataModelList.get(orderedPlayerDataModelList.size() -1 - index));
            playerFlopScoreList.set(index, orderedPlayerDataModelList.get(index));
        }
    }

    public static TopFlopScoreManager getTopFlopScoreManager() {
        return topFlopScoreManager;
    }

    public List<PlayerDataModel> getPlayerTopScoreList() {
        return playerTopScoreList;
    }

    public List<PlayerDataModel> getPlayerFlopScoreList() {
        return playerFlopScoreList;
    }

    public int getLimitSize() {
        return limitSize;
    }
}
