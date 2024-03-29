package com.rosstail.karma.players;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.storage.StorageManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
        this.limitSize = ConfigData.getConfigData().general.topScoreLimit;
        presetValuesInList(playerTopScoreList);
        presetValuesInList(playerFlopScoreList);
    }

    private void presetValuesInList(List<PlayerModel> list) {
        while (list.size() < limitSize) {
            list.add(null);
        }
    }

    public void getScores() {
        int limit = ConfigData.getConfigData().general.topScoreLimit;
        StorageManager storageManager = StorageManager.getManager();
        Collection<PlayerModel> onlinePlayerModels = PlayerDataManager.getPlayerModelMap().values();
        List<PlayerModel> orderedPlayerModelList = new ArrayList<>();
        for (PlayerModel playerModel : onlinePlayerModels) {
            int index = 0;

            for (PlayerModel model : orderedPlayerModelList) {
                if (playerModel.getKarma() < model.getKarma()) {
                    orderedPlayerModelList.add(index, new PlayerModel(playerModel));
                    break;
                }
                index++;
            }
            if (orderedPlayerModelList.isEmpty() || index == orderedPlayerModelList.size()) {
                orderedPlayerModelList.add(new PlayerModel(playerModel));
            }
        }

        List<PlayerModel> reqTopScores = storageManager.selectPlayerModelListTop(limit);
        List<PlayerModel> reqBottomScores = storageManager.selectPlayerModelListBottom(limit);

        for (PlayerModel playerModel : reqTopScores) {
            int index = 0;

            for (PlayerModel model : orderedPlayerModelList) {
                if (Objects.equals(playerModel.getUuid(), model.getUuid())) {
                    break;
                }
                if (playerModel.getKarma() < model.getKarma()) {
                    orderedPlayerModelList.add(index, playerModel);
                    break;
                }
                index++;
            }
            if (orderedPlayerModelList.isEmpty() || index == orderedPlayerModelList.size()) {
                orderedPlayerModelList.add(playerModel);
            }
        }

        for (PlayerModel playerModel : reqBottomScores) {
            int index = 0;

            for (PlayerModel model : orderedPlayerModelList) {
                if (Objects.equals(playerModel.getUuid(), model.getUuid())) {
                    break;
                }
                if (playerModel.getKarma() < model.getKarma()) {
                    orderedPlayerModelList.add(index, playerModel);
                    break;
                }
                index++;
            }
            if (orderedPlayerModelList.isEmpty() || index == orderedPlayerModelList.size()) {
                orderedPlayerModelList.add(playerModel);
            }
        }

        int currentLimit = Math.min(orderedPlayerModelList.size(), limit);

        for (int index = 0; index < currentLimit; index++) {
            playerTopScoreList.set(index, orderedPlayerModelList.get(orderedPlayerModelList.size() -1 - index));
            playerFlopScoreList.set(index, orderedPlayerModelList.get(index));
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
