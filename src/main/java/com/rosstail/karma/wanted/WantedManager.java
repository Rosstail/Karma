package com.rosstail.karma.wanted;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import com.rosstail.karma.players.PlayerDataManager;
import com.rosstail.karma.players.PlayerDataModel;
import com.rosstail.karma.events.karmaevents.PlayerWantedChangeEvent;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Timestamp;

public class WantedManager {
    private static WantedManager wantedManager;
    private final Karma plugin;

    private boolean doesWantedTimeRefresh;
    private ConfigData configData;

    public WantedManager(Karma plugin) {
        this.plugin = plugin;
    }

    public static void init(Karma plugin) {
        if (wantedManager == null) {
            wantedManager = new WantedManager(plugin);
        }
    }

    public void setup() {
        configData = ConfigData.getConfigData();
        this.doesWantedTimeRefresh = ConfigData.getConfigData().pvp.wantedRefresh;
    }

    public void wantedHandler(Player attacker, float newKarma, Player victim, String expression) {
        PlayerDataModel attackerModel = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        PlayerDataModel victimModel = PlayerDataManager.getPlayerModelMap().get(victim.getName());
        float attackerInitialKarma = attackerModel.getKarma();
        long attackerLastWanted = attackerModel.getWantedTimeStamp().getTime();
        long victimLastWanted = victimModel.getWantedTimeStamp().getTime();

        boolean hasAttackerWantedOnce = hasBeenWantedOnce(attackerLastWanted);
        boolean hasVictimWantedOnce = hasBeenWantedOnce(victimLastWanted);

        boolean isAttackerWanted = isPlayerWanted(attackerLastWanted);
        boolean isVictimWanted = isPlayerWanted(victimLastWanted);

        boolean doesAttackerRisksGuilt = doesAttackerRisksGuilt(attackerInitialKarma, newKarma);
        boolean isGuilty = false;

        if (doesAttackerRisksGuilt) {
            if (!hasAttackerWantedOnce && !hasVictimWantedOnce) { //if none have been wanted in the past
                //Declare attacker guilty
                isGuilty = isGuilty(isAttackerWanted);
            } else if (!hasVictimWantedOnce) { //if the victim never been wanted in the past
                //Declare attacker guilty
                isGuilty = isGuilty(isAttackerWanted);
            } else if (!isVictimWanted || isAttackerWanted) { //If both have been wanted in the past
                isGuilty = isGuilty(isAttackerWanted);
            }
        }

        if (isGuilty) {
            AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
            String calculatedExpression = adaptMessage.adaptPlayerMessage(attacker, expression, PlayerType.PLAYER.getText());
            calculatedExpression = adaptMessage.adaptMessage(calculatedExpression);
            Timestamp timestamp = new Timestamp(AdaptMessage.evalDuration(attackerModel.getWantedTimeStamp().getTime(), "[player_wanted_time] " + calculatedExpression));
            PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(attacker, attackerModel, timestamp, !ConfigData.getConfigData().pvp.sendMessageOnWantedChange);
            Bukkit.getPluginManager().callEvent(playerWantedChangeEvent);
        }
    }

    public boolean doKarmaChange(PlayerDataModel attackerModel, PlayerDataModel victimModel, float karmaDiff) {
        if (!attackerModel.isWanted() && victimModel.isWanted()) {
            return (!(karmaDiff > 0F) || !configData.pvp.cancelInnocentKarmaGain) && (!(karmaDiff < 0F) || !configData.pvp.cancelInnocentKarmaLoss);
        } else if (attackerModel.isWanted()) {
            return (!(karmaDiff > 0F) || !configData.pvp.cancelWantedKarmaGain) && (!(karmaDiff < 0F) || !configData.pvp.cancelWantedKarmaLoss);
        }
        return true;
    }

    private boolean hasBeenWantedOnce(long wantedTime) {
        return wantedTime != 0;
    }

    public boolean isPlayerWanted(long wantedTime) {
        return (wantedTime >= System.currentTimeMillis());
    }

    private boolean isGuilty(boolean isAttackerWanted) {
        return !isAttackerWanted || doesWantedTimeRefresh;
    }

    public boolean doesAttackerRisksGuilt(float attackerInitialKarma, float attackerNewKarma) {
        if (attackerNewKarma > attackerInitialKarma) {
            return configData.pvp.wantedOnKarmaGain;
        } else if (attackerNewKarma == attackerInitialKarma) {
            return configData.pvp.wantedOnKarmaUnchanged;
        }
        return configData.pvp.wantedOnKarmaLoss;
    }

    public static WantedManager getWantedManager() {
        return wantedManager;
    }
}
