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
            AdaptMessage.print("[Karma] enabled scoreboard team cancel", AdaptMessage.prints.OUT);
            teamFightHandlerList.add(new ScoreboardTeamFightHandler());
        }
    }

    public static void pvpHit(Player attacker, Player victim) {
        boolean doesKarmaChange = true;

        if (!TimeManager.getTimeManager().isPlayerInTime(attacker)) {
            attacker.sendMessage("not affected because of time period.");
            return;
        }

        PlayerModel victimModel = PlayerDataManager.getPlayerModelMap().get(victim.getName());
        PlayerModel attackerModel = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        float attackerInitialKarma = attackerModel.getKarma();
        String expression = configData.pvp.pvpHitRewardExpression;

        if (expression == null) {
            return;
        }

        float result;
        Tier attackerTier = TierManager.getTierManager().getTierByName(attackerModel.getTierName());
        Tier victimTier = TierManager.getTierManager().getTierByName(victimModel.getTierName());

        expression = adaptMessage.adaptPvpMessage(attacker, victim, expression);
        expression = expression.replaceAll("%karma_attacker_victim_tier_score%",
                String.valueOf(attackerTier.getTierScore(victimTier)));

        result = (float) ExpressionCalculator.eval(expression);
        if (configData.general.useWorldGuard) {
            float multi = (float) WGPreps.getWgPreps().checkMultipleKarmaFlags(attacker);
            result = result * multi;
        }

        float attackerNewKarma = attackerInitialKarma + result;

        if (ConfigData.getConfigData().wanted.wantedEnable) {
            WantedManager wantedManager = WantedManager.getWantedManager();
            doesKarmaChange = wantedManager.doKarmaChange(attackerModel, victimModel, result);
            wantedManager.wantedHandler(attacker, attackerNewKarma, victim, configData.pvp.wantedHitDurationExpression);
        }

        karmaChangeChecker(attacker, result, attackerModel, attackerInitialKarma, doesKarmaChange, attackerNewKarma);
    }

    public static void pvpKill(Player attacker, Player victim) {
        boolean doesKarmaChange = true;

        if (!TimeManager.getTimeManager().isPlayerInTime(attacker)) {
            attacker.sendMessage("not affected because of time period.");
            return;
        }

        PlayerModel victimModel = PlayerDataManager.getPlayerModelMap().get(victim.getName());
        PlayerModel attackerModel = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        float attackerInitialKarma = attackerModel.getKarma();
        String expression = configData.pvp.pvpKillRewardExpression;

        //Pvp kill commands when guarantee is FALSE
        PvpCommandHandler.getPvpCommandHandler().handle(attacker, victim, false);

        if (expression == null) {
            return;
        }

        float result;
        Tier attackerTier = TierManager.getTierManager().getTierByName(attackerModel.getTierName());
        Tier victimTier = TierManager.getTierManager().getTierByName(victimModel.getTierName());

        expression = adaptMessage.adaptPvpMessage(attacker, victim, expression);
        expression = expression.replaceAll("%karma_attacker_victim_tier_score%",
                String.valueOf(attackerTier.getTierScore(victimTier)));

        result = (float) ExpressionCalculator.eval(expression);
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
            attacker.sendMessage("No change");
        } else if (attackerNewKarma > ConfigData.getConfigData().karmaConfig.maxKarma) { //If new karma > max karma
            if (attackerInitialKarma > ConfigData.getConfigData().karmaConfig.maxKarma && attackerNewKarma >= attackerInitialKarma) { //Avoid changes if OOB driving away
                attacker.sendMessage("OOB KILL > MAX");
                doesKarmaChange = false;
            } else {
                attackerNewKarma = ConfigData.getConfigData().karmaConfig.maxKarma;
            }
        } else if (attackerNewKarma < ConfigData.getConfigData().karmaConfig.minKarma) { //if new karma < min karma
            if (attackerInitialKarma < ConfigData.getConfigData().karmaConfig.minKarma && attackerNewKarma <= attackerInitialKarma) { //Avoid changes if OOB driving away
                attacker.sendMessage("OOB KILL < MIN");
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

    public static void pveHit(Player attacker, Mob victim) {
        if (!TimeManager.getTimeManager().isPlayerInTime(attacker)) {
            attacker.sendMessage("not affected because of time period.");
            return;
        }

        String entityName = victim.getName();
        YamlConfiguration config = plugin.getCustomConfig();
        float reward = config.getInt("entities.list." + entityName + ".hit-karma-reward");
        CommandManager.commandsLauncher(attacker, config.getStringList("entities.list." + entityName + ".hit-commands"));

        rewardChecker(attacker, reward);

        adaptMessage.pveHitMessage(config.getString("entities.list." + entityName + ".hit-message"), attacker);
    }

    private static void rewardChecker(Player attacker, float reward) {
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        float attackerInitialKarma = model.getKarma();

        if (configData.general.useWorldGuard) {
            reward = reward * (float) WGPreps.getWgPreps().checkMultipleKarmaFlags(attacker);
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
        if (!TimeManager.getTimeManager().isPlayerInTime(attacker)) {
            attacker.sendMessage("not affected because of time period.");
            return;
        }

        String entityName = victim.getName();
        YamlConfiguration config = plugin.getCustomConfig();
        float reward = config.getInt("entities.list." + entityName + ".kill-karma-reward");
        CommandManager.commandsLauncher(attacker, config.getStringList("entities.list." + entityName + ".kill-commands"));

        rewardChecker(attacker, reward);

        adaptMessage.pveKillMessage(config.getString("entities.list." + entityName + ".kill-message"), attacker);
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
