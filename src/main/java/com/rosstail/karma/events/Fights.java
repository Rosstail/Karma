package com.rosstail.karma.events;

import com.rosstail.karma.Karma;
import com.rosstail.karma.apis.ExpressionCalculator;
import com.rosstail.karma.apis.WGPreps;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.events.karmaevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.events.karmaevents.PlayerWantedChangeEvent;
import com.rosstail.karma.events.karmaevents.PlayerWantedPunishEvent;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.tiers.TierManager;
import com.rosstail.karma.timemanagement.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.sql.Timestamp;

public class Fights {

    private static final Karma plugin = Karma.getInstance();
    private static ConfigData configData = ConfigData.getConfigData();
    private static final AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
    private static final boolean doesWantedTimeRefresh = configData.wantedRefresh;

    public static void pvpHandler(Player attacker, Player victim, Event event) {
        boolean isPlayerInTime = TimeManager.getTimeManager().isPlayerInTime(attacker);
        boolean doesKarmaChange = true;
        if (isFakePlayer(attacker) || (isFakePlayer(victim) && !doesPlayerNPCHaveKarma(victim) || !isPlayerInTime)) {
            return;
        }

        PlayerModel victimModel = PlayerDataManager.getPlayerModelMap().get(victim.getName());
        PlayerModel attackerModel = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        float attackerInitialKarma = attackerModel.getKarma();
        ConfigData configData = ConfigData.getConfigData();
        String expression;

        if (event instanceof PlayerDeathEvent) {
            CommandManager.commandsLauncher(attacker, victim, TierManager.getTierManager().getTierByName(victimModel.getTierName()).getKilledCommands());

            String path = configData.killedByTierPath;

            path = adaptMessage.adapt(victim, path, PlayerType.VICTIM.getText());
            path = adaptMessage.adapt(attacker, path, PlayerType.ATTACKER.getText());
            CommandManager.commandsLauncher(attacker, victim, plugin.getCustomConfig().getStringList(path));

            expression = configData.pvpKillRewardExpression;
        } else {
            expression = configData.pvpHitRewardExpression;
        }

        if (expression == null) {
            return;
        }

        float result;
        Tier attackerTier = TierManager.getTierManager().getTierByName(attackerModel.getTierName());
        Tier victimTier = TierManager.getTierManager().getTierByName(victimModel.getTierName());

        expression = adaptMessage.adapt(attacker, expression, PlayerType.ATTACKER.getText());
        expression = adaptMessage.adapt(victim, expression, PlayerType.VICTIM.getText());
        expression = expression.replaceAll("%karma_attacker_victim_tier_score%",
                String.valueOf(attackerTier.getTierScore(victimTier)));

        result = (float) ExpressionCalculator.eval(expression);
        if (configData.useWorldGuard) {
            float multi = (float) WGPreps.getWgPreps().checkMultipleKarmaFlags(attacker);
            result = result * multi;
        }

        if (!attackerModel.isWanted() && victimModel.isWanted()) {
            if ((result > 0 && configData.cancelInnocentKarmaGain) || (result < 0 && configData.cancelInnocentKarmaLoss)) {
                doesKarmaChange = false;
            }
        } else if (attackerModel.isWanted()) {
            if ((result > 0 && configData.cancelWantedKarmaGain) || (result < 0 && configData.cancelWantedKarmaLoss)) {
                doesKarmaChange = false;
            }
        }

        float attackerNewKarma = attackerInitialKarma + result;
        if (configData.wantedEnable && !(attacker.hasMetadata("NPC") || victim.hasMetadata("NPC"))) {
            wantedHandler(attacker, attackerNewKarma, victim, event);
        }

        if (doesKarmaChange) {
            PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(attacker, attackerModel, result);
            Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        }
    }

    public static void pveHandler(Player attacker, LivingEntity entity, Event event) {
        String entityName = entity.getName();
        YamlConfiguration config = plugin.getCustomConfig();
        float reward;
        if (event instanceof EntityDeathEvent) {
            reward = config.getInt("entities.list." + entityName + ".kill-karma-reward");
            CommandManager.commandsLauncher(attacker, config.getStringList("entities.list." + entityName + ".kill-commands"));
        } else if (event instanceof EntityDamageByEntityEvent) {
            reward = config.getInt("entities.list." + entityName + ".hit-karma-reward");
            CommandManager.commandsLauncher(attacker, config.getStringList("entities.list." + entityName + ".hit-commands"));
        } else {
            return;
        }

        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        float attackerKarma = model.getKarma();

        if (configData.useWorldGuard) {
            reward = reward * (float) WGPreps.getWgPreps().checkMultipleKarmaFlags(attacker);
        }

        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(attacker, model, attackerKarma + reward);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);

        if (event instanceof EntityDeathEvent) {
            adaptMessage.entityHitMessage(config.getString("entities.list." + entityName + ".kill-message"), attacker, event);
        } else {
            adaptMessage.entityHitMessage(config.getString("entities.list." + entityName + ".hit-message"), attacker, event);
        }
    }

    public static boolean isFakePlayer(Player player) {
        return player.hasMetadata("NPC");
    }

    public static boolean doesPlayerNPCHaveKarma(Player npc) {
        return npc.hasMetadata("Karma") && npc.getMetadata("Karma").get(0) != null;
    }

    public static boolean doesAttackerRisksGuilt(float attackerInitialKarma, float attackerNewKarma) {
        if (attackerNewKarma > attackerInitialKarma) {
            return configData.wantedOnKarmaGain;
        } else if (attackerNewKarma == attackerInitialKarma) {
            return configData.wantedOnKarmaUnchanged;
        } else {
            return configData.wantedOnKarmaLoss;
        }
    }

    private static void wantedHandler(Player attacker, float newKarma, Player victim, Object cause) {
        PlayerModel attackerModel = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        PlayerModel victimModel = PlayerDataManager.getPlayerModelMap().get(victim.getName());

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
            } else { //If both have been wanted in the past
                if (!isVictimWanted || isAttackerWanted) {
                    isGuilty = isGuilty(isAttackerWanted);
                }
            }
        }

        if (isGuilty) {
            String expression = "(" + configData.wantedDurationExpression + ")";
            expression = AdaptMessage.getAdaptMessage().adapt(attacker, expression, PlayerType.PLAYER.getText());
            Timestamp timestamp = new Timestamp(AdaptMessage.calculateDuration(PlayerDataManager.getWantedTimeLeft(attackerModel), expression));
            PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(attacker, attackerModel, timestamp);
            Bukkit.getPluginManager().callEvent(playerWantedChangeEvent);
        } else {
            boolean doPunishWanted = TierManager.getTierManager().getTierByName(attackerModel.getTierName()).doPunishWanted();
            if (isVictimWanted && doPunishWanted) {
                PlayerWantedPunishEvent playerWantedPunishEvent = new PlayerWantedPunishEvent(victim, attacker);
                Bukkit.getPluginManager().callEvent(playerWantedPunishEvent);
            }
        }

    }

    private static boolean hasBeenWantedOnce(long wantedTime) {
        return wantedTime != 0;
    }

    public static boolean isPlayerWanted(long wantedTime) {
        return (wantedTime >= System.currentTimeMillis());
    }

    private static boolean isGuilty(boolean isAttackerWanted) {
        return !isAttackerWanted || doesWantedTimeRefresh;
    }

    public static void setConfigData(ConfigData configData) {
        Fights.configData = configData;
    }
}
