package com.rosstail.karma.fight;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.Karma;
import com.rosstail.karma.apis.ExpressionCalculator;
import com.rosstail.karma.apis.WGPreps;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.events.karmaevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.fight.pvpcommandhandlers.PvpCommandHandler;
import com.rosstail.karma.fight.teamfighthandlers.ScoreboardTeamFightHandler;
import com.rosstail.karma.fight.teamfighthandlers.TeamFightHandler;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.tiers.TierManager;
import com.rosstail.karma.timeperiod.TimeManager;
import com.rosstail.karma.wanted.WantedManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FightHandler {
    private static final Karma plugin = Karma.getInstance();
    private static final AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
    private static ConfigData configData = ConfigData.getConfigData();

    private static final List<TeamFightHandler> teamFightHandlerList = new ArrayList<>();

    public static void initFightHandler() {
        if (ConfigData.getConfigData().pvp.scoreboardTeamSystemCancel) {
            teamFightHandlerList.add(new ScoreboardTeamFightHandler());
        }
    }

    public static void pvpHit(Player attacker, Player victim) {
        boolean doesKarmaChange = true;

        if (!TimeManager.getTimeManager().isPlayerInTime(attacker)) {
            return;
        }

        PlayerModel victimModel = PlayerDataManager.getPlayerModelMap().get(victim.getName());
        PlayerModel attackerModel = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        String attackerKarmaChangeExpression = configData.pvp.pvpHitAttackerChangeExpression;
        String victimKarmaChangeExpression = configData.pvp.pvpHitVictimChangeExpression;

        Tier attackerTier = TierManager.getTierManager().getTierByName(attackerModel.getTierName());
        Tier victimTier = TierManager.getTierManager().getTierByName(victimModel.getTierName());

        if (attackerKarmaChangeExpression != null) {
            float result;
            float attackerInitialKarma = attackerModel.getKarma();
            attackerKarmaChangeExpression = adaptMessage.adaptPvpMessage(attacker, victim, attackerKarmaChangeExpression);
            attackerKarmaChangeExpression = attackerKarmaChangeExpression
                    .replaceAll("%attacker_victim_tier_score%", String.valueOf(attackerTier.getTierScore(victimTier.getName())))
                    .replaceAll("%victim_attacker_tier_score%", String.valueOf(victimTier.getTierScore(attackerTier.getName())));

            result = (float) ExpressionCalculator.eval(attackerKarmaChangeExpression);

            if (configData.general.useWorldGuard) {
                float multi = (float) WGPreps.getWgPreps().checkMultipleKarmaFlags(attacker);
                result *= multi;
            }

            float attackerNewKarma = attackerInitialKarma + result;

            if (ConfigData.getConfigData().wanted.wantedEnable) {
                WantedManager wantedManager = WantedManager.getWantedManager();
                doesKarmaChange = wantedManager.doKarmaChange(attackerModel, victimModel, result);
                wantedManager.wantedHandler(attacker, attackerNewKarma, victim, configData.pvp.wantedHitDurationExpression);
            }

            karmaChangeChecker(attacker, result, attackerModel, attackerInitialKarma, doesKarmaChange, attackerNewKarma);
        }

        if (victimKarmaChangeExpression != null) {
            float result;
            float victimInitialKarma = victimModel.getKarma();
            victimKarmaChangeExpression = adaptMessage.adaptPvpMessage(attacker, victim, victimKarmaChangeExpression);
            victimKarmaChangeExpression = victimKarmaChangeExpression
                    .replaceAll("%attacker_victim_tier_score%", String.valueOf(attackerTier.getTierScore(victimTier.getName())))
                    .replaceAll("%victim_attacker_tier_score%", String.valueOf(victimTier.getTierScore(attackerTier.getName())));

            result = (float) ExpressionCalculator.eval(victimKarmaChangeExpression);

            if (configData.general.useWorldGuard) {
                float multi = (float) WGPreps.getWgPreps().checkMultipleKarmaFlags(victim);
                result *= multi;
            }

            float victimNewKarma = victimInitialKarma + result;

            karmaChangeChecker(victim, result, victimModel, victimInitialKarma, doesKarmaChange, victimNewKarma);
        }

    }

    public static void pvpKill(Player attacker, Player victim) {
        boolean doesKarmaChange = true;

        if (!TimeManager.getTimeManager().isPlayerInTime(attacker)) {
            return;
        }

        PlayerModel victimModel = PlayerDataManager.getPlayerModelMap().get(victim.getName());
        PlayerModel attackerModel = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        String attackerKarmaChangeExpression = configData.pvp.pvpKillAttackerChangeExpression;
        String victimKarmaChangeExpression = configData.pvp.pvpKillVictimChangeExpression;
        Tier attackerTier = TierManager.getTierManager().getTierByName(attackerModel.getTierName());
        Tier victimTier = TierManager.getTierManager().getTierByName(victimModel.getTierName());

        //Pvp kill commands when guarantee is FALSE
        PvpCommandHandler.getPvpCommandHandler().handle(attacker, victim, false);

        if (attackerKarmaChangeExpression != null) {
            float attackerInitialKarma = attackerModel.getKarma();
            float result;

            attackerKarmaChangeExpression = adaptMessage.adaptPvpMessage(attacker, victim, attackerKarmaChangeExpression);

            attackerKarmaChangeExpression = attackerKarmaChangeExpression
                    .replaceAll("%attacker_victim_tier_score%", String.valueOf(attackerTier.getTierScore(victimTier.getName())))
                    .replaceAll("%victim_attacker_tier_score%", String.valueOf(victimTier.getTierScore(attackerTier.getName())));

            result = (float) ExpressionCalculator.eval(attackerKarmaChangeExpression);
            if (configData.general.useWorldGuard) {
                float multi = (float) WGPreps.getWgPreps().checkMultipleKarmaFlags(attacker);
                result = result * multi;
            }

            float attackerNewKarma = attackerInitialKarma + result;

            if (ConfigData.getConfigData().wanted.wantedEnable) {
                WantedManager wantedManager = WantedManager.getWantedManager();
                doesKarmaChange = wantedManager.doKarmaChange(attackerModel, victimModel, result);
                wantedManager.wantedHandler(attacker, attackerNewKarma, victim, configData.pvp.wantedKillDurationExpression);
            }

            if (result == 0F) { //If no change, skip
                doesKarmaChange = false;
            } else if (attackerNewKarma > ConfigData.getConfigData().karmaConfig.maxKarma) { //If new karma > max karma
                if (attackerInitialKarma > ConfigData.getConfigData().karmaConfig.maxKarma && attackerNewKarma >= attackerInitialKarma) { //Avoid changes if OOB driving away
                    doesKarmaChange = false;
                } else {
                    attackerNewKarma = ConfigData.getConfigData().karmaConfig.maxKarma;
                }
            } else if (attackerNewKarma < ConfigData.getConfigData().karmaConfig.minKarma) { //if new karma < min karma
                if (attackerInitialKarma < ConfigData.getConfigData().karmaConfig.minKarma && attackerNewKarma <= attackerInitialKarma) { //Avoid changes if OOB driving away
                    doesKarmaChange = false;
                } else {
                    attackerNewKarma = ConfigData.getConfigData().karmaConfig.minKarma;
                }
            }

            if (doesKarmaChange) {
                PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(attacker, attackerModel, attackerNewKarma);
                Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
            }
        }

        if (victimKarmaChangeExpression != null) {
            float victimInitialKarma = victimModel.getKarma();
            float result;

            victimKarmaChangeExpression = adaptMessage.adaptPvpMessage(attacker, victim, victimKarmaChangeExpression);

            victimKarmaChangeExpression = victimKarmaChangeExpression
                    .replaceAll("%attacker_victim_tier_score%", String.valueOf(attackerTier.getTierScore(victimTier.getName())))
                    .replaceAll("%victim_attacker_tier_score%", String.valueOf(victimTier.getTierScore(attackerTier.getName())));

            result = (float) ExpressionCalculator.eval(victimKarmaChangeExpression);
            if (configData.general.useWorldGuard) {
                float multi = (float) WGPreps.getWgPreps().checkMultipleKarmaFlags(victim);
                result = result * multi;
            }

            float victimNewKarma = victimInitialKarma + result;

            if (result == 0F) { //If no change, skip
                doesKarmaChange = false;
            } else if (victimNewKarma > ConfigData.getConfigData().karmaConfig.maxKarma) { //If new karma > max karma
                if (victimInitialKarma > ConfigData.getConfigData().karmaConfig.maxKarma && victimNewKarma >= victimInitialKarma) { //Avoid changes if OOB driving away
                    doesKarmaChange = false;
                } else {
                    victimNewKarma = ConfigData.getConfigData().karmaConfig.maxKarma;
                }
            } else if (victimNewKarma < ConfigData.getConfigData().karmaConfig.minKarma) { //if new karma < min karma
                if (victimInitialKarma < ConfigData.getConfigData().karmaConfig.minKarma && victimNewKarma <= victimInitialKarma) { //Avoid changes if OOB driving away
                    doesKarmaChange = false;
                } else {
                    victimNewKarma = ConfigData.getConfigData().karmaConfig.minKarma;
                }
            }

            if (doesKarmaChange) {
                PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(victim, victimModel, victimNewKarma);
                Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
            }
        }
    }

    public static void pveHit(Player attacker, Mob victim) {

        String entityName = victim.getName();
        YamlConfiguration config = plugin.getCustomConfig();
        float reward = config.getInt("entities.list." + entityName + ".hit-karma-reward");
        CommandManager.commandsLauncher(attacker, config.getStringList("entities.list." + entityName + ".hit-commands"));

        rewardChecker(attacker, reward);

        adaptMessage.pveHitMessage(config.getString("entities.list." + entityName + ".hit-message"), attacker, victim);
    }

    private static void rewardChecker(Player attacker, float reward) {
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        float attackerInitialKarma = model.getKarma();

        if (configData.general.useWorldGuard) {
            reward *= (float) WGPreps.getWgPreps().checkMultipleKarmaFlags(attacker);
        }

        boolean doesKarmaChange = true;
        float attackerNewKarma = attackerInitialKarma + reward;

        karmaChangeChecker(attacker, reward, model, attackerInitialKarma, doesKarmaChange, attackerNewKarma);
    }

    private static void karmaChangeChecker(Player attacker, float reward, PlayerModel model, float attackerInitialKarma, boolean doesKarmaChange, float attackerNewKarma) {
        if (reward == 0F) { //If no change, skip
            doesKarmaChange = false;
        } else if (attackerNewKarma > ConfigData.getConfigData().karmaConfig.maxKarma) { //If new karma > max karma
            if (attackerInitialKarma > ConfigData.getConfigData().karmaConfig.maxKarma && attackerNewKarma >= attackerInitialKarma) { //Avoid changes if OOB driving away
                doesKarmaChange = false;
            } else {
                attackerNewKarma = ConfigData.getConfigData().karmaConfig.maxKarma;
            }
        } else if (attackerNewKarma < ConfigData.getConfigData().karmaConfig.minKarma) { //if new karma < min karma
            if (attackerInitialKarma < ConfigData.getConfigData().karmaConfig.minKarma && attackerNewKarma <= attackerInitialKarma) { //Avoid changes if OOB driving away
                doesKarmaChange = false;
            } else {
                attackerNewKarma = ConfigData.getConfigData().karmaConfig.minKarma;
            }
        }

        if (doesKarmaChange) {
            PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(attacker, model, attackerNewKarma);
            Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        }
    }

    public static void pveKill(Player attacker, Mob victim) {

        String entityName = victim.getName();
        YamlConfiguration config = plugin.getCustomConfig();
        float reward = config.getInt("entities.list." + entityName + ".kill-karma-reward");
        CommandManager.commandsLauncher(attacker, config.getStringList("entities.list." + entityName + ".kill-commands"));

        rewardChecker(attacker, reward);

        adaptMessage.pveKillMessage(config.getString("entities.list." + entityName + ".kill-message"), attacker, victim);
    }

    public static boolean isFakePlayer(Player player) {
        return player.hasMetadata("NPC");
    }

    public static boolean doesPlayerNPCHaveKarma(Player npc) {
        return npc.hasMetadata("Karma") && npc.getMetadata("Karma").get(0) != null;
    }

    public static void setConfigData(ConfigData configData) {
        FightHandler.configData = configData;
    }

    public static List<TeamFightHandler> getTeamFightHandlerList() {
        return teamFightHandlerList;
    }
}
