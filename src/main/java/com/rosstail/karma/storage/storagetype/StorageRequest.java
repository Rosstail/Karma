package com.rosstail.karma.storage.storagetype;

import com.rosstail.karma.players.PlayerModel;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public interface StorageRequest {
    ZoneId serverZoneId = ZoneId.systemDefault();

    long offsetMillis = serverZoneId.getRules().getOffset(Instant.now()).getTotalSeconds() * 1000L;

    void setupStorage(String host, short port, String database, String username, String password);

    /**
     * CREATE
     * Insert player model into storage
     * @param model The model to insert
     */
    boolean insertPlayerModel(PlayerModel model);

    /**
     * READ
     * get player model from storage
     * @param uuid The uuid of player
     * @return his PlayerModel
     */
    PlayerModel selectPlayerModel(String uuid);

    /**
     * UPDATE
     * Edit player model to storage
     * @param model The model to update
     */
    void updatePlayerModel(PlayerModel model);

    /**
     * DELETE
     * destroys player model from storage
     * @param uuid The target uuid
     */
    void deletePlayerModel(String uuid);

    /**
     * SELECT get a list of player models with an order and a limit.
     * @param order ASC or DESC.
     * @param limit the amount of models to retreive
     * @return the models found
     */
    List<PlayerModel> selectPlayerModelList(String order, int limit);
}
