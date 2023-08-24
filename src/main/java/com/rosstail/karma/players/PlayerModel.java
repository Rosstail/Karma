package com.rosstail.karma.players;

import com.rosstail.karma.ConfigData;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class PlayerModel {
    private String uuid;
    private String username;

    private float karma = ConfigData.getConfigData().karmaConfig.defaultKarma;
    private float previousKarma = ConfigData.getConfigData().karmaConfig.defaultKarma;

    private String tierName;
    private String previousTierName;

    private long lastUpdate = 0;
    private Map<String, Timestamp> overTimeStampMap = new HashMap<>();

    private Timestamp wantedTimeStamp = new Timestamp(0L);
    private boolean wanted = false;

    /**
     * Constructor if the selected player is connected
     * @param player - a user joining the server
     */
    public PlayerModel(Player player) {
        this.uuid = player.getUniqueId().toString();
        this.username = player.getName();
    }

    /**
     * Constructor if the selected player is not connected
     * @param uuid - microsoft/mojang identifier of selected player
     * @param username - current username of the selected player
     */
    public PlayerModel(String uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.checkForData();
    }

    /**
     * Creates a copy of an existing PlayerModel. Should only be used to read players data, not
     * @param playerModel
     */
    public PlayerModel(PlayerModel playerModel) {
        this.uuid = playerModel.getUuid();
        this.username = playerModel.getUsername();
        this.karma = playerModel.getKarma();
        this.tierName = playerModel.getTierName();
        this.previousKarma = playerModel.getPreviousKarma();
        this.previousTierName = playerModel.getPreviousTierName();
        this.lastUpdate = playerModel.getLastUpdate();
        this.wanted = playerModel.isWanted();
        this.wantedTimeStamp = playerModel.getWantedTimeStamp();
        this.overTimeStampMap = playerModel.getOverTimeStampMap();
    }

    /**
     * Will check if the player already has data in localstorage or database.
     * @return the success of the check
     */
    private boolean checkForData() {
        return false;
    }

    /*
    Getters setters
     */

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public float getKarma() {
        return karma;
    }

    public void setKarma(float karma) {
        this.karma = karma;
    }

    public float getPreviousKarma() {
        return previousKarma;
    }

    public void setPreviousKarma(float previousKarma) {
        this.previousKarma = previousKarma;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public String getPreviousTierName() {
        return previousTierName;
    }

    public void setPreviousTierName(String previousTierName) {
        this.previousTierName = previousTierName;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Map<String, Timestamp> getOverTimeStampMap() {
        return overTimeStampMap;
    }

    public void setOverTimeStampMap(Map<String, Timestamp> overTimeStampMap) {
        this.overTimeStampMap = overTimeStampMap;
    }

    public Timestamp getWantedTimeStamp() {
        return wantedTimeStamp;
    }

    public void setWantedTimeStamp(Timestamp wantedTimeStamp) {
        this.wantedTimeStamp = wantedTimeStamp;
    }

    public boolean isWanted() {
        return wanted;
    }

    public void setWanted(boolean wanted) {
        this.wanted = wanted;
    }
}
