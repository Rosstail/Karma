package com.rosstail.karma.storage.mappers.playerdataentity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Map;

@Entity
@Table(name = "karma")
public class PlayerDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "uuid", unique = true, nullable = false)
    private String uuid;

    @Column(name = "karma", nullable = false)
    private float karma;
    @Column(name = "previous_karma", nullable = false)
    private float previousKarma;

    @Column(name = "tier", nullable = false)
    private String tierName;
    @Column(name = "previous_tier", nullable = false)
    private String previousTierName;

    @Column(name = "last_update", nullable = false)
    private long lastUpdate;
    private Map<String, Timestamp> overTimeStampMap;

    @Column(name = "wanted_time", nullable = false)
    private Timestamp wantedTimeStamp;
    @Column(name = "is_wanted", nullable = false)
    private boolean wanted;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isWanted() {
        return wanted;
    }

    public void setWanted(boolean wanted) {
        this.wanted = wanted;
    }
}
