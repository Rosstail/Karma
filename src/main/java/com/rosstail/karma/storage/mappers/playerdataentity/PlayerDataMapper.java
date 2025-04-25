package com.rosstail.karma.storage.mappers.playerdataentity;

import com.rosstail.karma.players.PlayerDataModel;

public class PlayerDataMapper {

    // business -> entity
    public static PlayerDataEntity toEntity(PlayerDataModel profile) {
        PlayerDataEntity playerDataEntity = new PlayerDataEntity();
        playerDataEntity.setUuid(profile.getUuid());
        playerDataEntity.setKarma(profile.getKarma());
        playerDataEntity.setPreviousKarma(profile.getPreviousKarma());
        playerDataEntity.setTierName(profile.getTierName());
        playerDataEntity.setPreviousTierName(profile.getPreviousTierName());
        playerDataEntity.setWantedTimeStamp(profile.getWantedTimeStamp());
        playerDataEntity.setWanted(profile.isWanted());
        playerDataEntity.setLastUpdate(profile.getLastUpdate());
        playerDataEntity.setOverTimeStampMap(profile.getOverTimeStampMap());
        return playerDataEntity;
    }

    // entity -> business
    public static PlayerDataModel toProfile(PlayerDataEntity entity) {
        PlayerDataModel playerDataModel = new PlayerDataModel(entity.getUuid(), null);
        playerDataModel.setKarma(entity.getKarma());
        playerDataModel.setPreviousKarma(entity.getPreviousKarma());
        playerDataModel.setTierName(entity.getTierName());
        playerDataModel.setPreviousTierName(entity.getPreviousTierName());
        playerDataModel.setWantedTimeStamp(entity.getWantedTimeStamp());
        playerDataModel.setWanted(entity.isWanted());
        playerDataModel.setLastUpdate(entity.getLastUpdate());
        playerDataModel.setOverTimeStampMap(playerDataModel.getOverTimeStampMap());
        return playerDataModel;
    }
}
