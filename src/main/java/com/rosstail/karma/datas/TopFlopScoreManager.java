package com.rosstail.karma.datas;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.datas.storage.StorageManager;

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
                    orderedPlayerModelList.add(index, playerModel);
                    break;
                }
                index++;
            }
            if (orderedPlayerModelList.isEmpty() || index == orderedPlayerModelList.size()) {
                orderedPlayerModelList.add(playerModel);
            }
        }

        //System.out.println("ORDERED ONLINE PLAYER LIST TOP TO BOTTOM");
        for (int i = orderedPlayerModelList.size() - 1; i >= 0; i--) {
            PlayerModel model = orderedPlayerModelList.get(i);
            //System.out.println("  " + i + "." + model.getUuid() + " " + model.getUsername() + " " + model.getKarma());
        }
        //System.out.println("END OF ONLINE PLAYER LIST");

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

        System.out.println("ORDERED ALL PLAYER LIST TOP TO BOTTOM");
        for (int i = orderedPlayerModelList.size() - 1; i >= 0; i--) {
            PlayerModel model = orderedPlayerModelList.get(i);
            System.out.println("  " + i + "." + model.getUuid() + " " + model.getUsername() + " " + model.getKarma());
        }
        System.out.println("END OF ALL PLAYER LIST");

        int currentLimit = Math.min(reqTopScores.size(), limit);

        /*
        Add online players karma to the scores if they fit inside.
         */
        if (currentLimit > 0) {
            onlinePlayerModels.forEach(onlinePlayerModel -> {
                float onlineKarma = onlinePlayerModel.getKarma();
                if (onlineKarma >= reqTopScores.get(currentLimit - 1).getKarma()) {
                    int i = currentLimit - 1;
                    while (onlineKarma >= reqTopScores.get(i).getKarma()) {
                        if (i == 0) {
                            break;
                        }
                        i--;
                    }
                    reqTopScores.add(i, onlinePlayerModel);
                    reqTopScores.remove(reqTopScores.size() - 1);
                }

                if (onlineKarma <= reqBottomScores.get(0).getKarma()) {
                    int i = 0;
                    while (onlineKarma <= reqBottomScores.get(i).getKarma()) {
                        if (i == currentLimit - 1) {
                            break;
                        }
                        i++;
                    }
                    reqBottomScores.add(i, onlinePlayerModel);
                    reqBottomScores.remove(currentLimit);
                }
            });
        }

        for (int index = 0; index < currentLimit; index++) {
            playerTopScoreList.set(index, reqTopScores.get(index));
            playerFlopScoreList.set(index, reqBottomScores.get(index));
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
