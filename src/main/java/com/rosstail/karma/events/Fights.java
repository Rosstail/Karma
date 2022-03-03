package com.rosstail.karma.events;

import com.rosstail.karma.Karma;
import com.rosstail.karma.apis.ExpressionCalculator;
import com.rosstail.karma.apis.WGPreps;
import com.rosstail.karma.commands.KarmaCommand;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.customevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.customevents.PlayerWantedChangeEvent;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.PlayerType;
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
        if (isPlayerNPC(attacker) || (isPlayerNPC(victim) && !doesPlayerNPCHaveKarma(victim) || !isPlayerInTime)) {
            return;
        }

        PlayerData victimData = PlayerDataManager.getPlayerDataMap().get(victim);
        PlayerData attackerData = PlayerDataManager.getPlayerDataMap().get(attacker);
        double attackerInitialKarma = attackerData.getKarma();
        ConfigData configData = ConfigData.getConfigData();
        String expression;

        if (event instanceof PlayerDeathEvent) {
            KarmaCommand.commandsLauncher(attacker, victim, victimData.getTier().getKilledCommands());

            String path = configData.killedByTierPath;

            path = adaptMessage.adapt(victim, path, PlayerType.VICTIM.getText());
            path = adaptMessage.adapt(attacker, path, PlayerType.ATTACKER.getText());
            KarmaCommand.commandsLauncher(attacker, victim, plugin.getCustomConfig().getStringList(path));

            expression = configData.pvpKillRewardExpression;
        } else {
            expression = configData.pvpHitRewardExpression;
        }

        if (expression == null) {
            return;
        }

        double result;


        expression = adaptMessage.adapt(attacker, expression, PlayerType.ATTACKER.getText());
        expression = adaptMessage.adapt(victim, expression, PlayerType.VICTIM.getText());
        expression = expression.replaceAll("%karma_attacker_victim_tier_score%",
                String.valueOf(PlayerDataManager.getPlayerDataMap().get(attacker).getTier().getTierScore(PlayerDataManager.getPlayerDataMap().get(victim).getTier())));

        result = ExpressionCalculator.eval(expression);
        if (configData.useWorldGuard) {
            double multi = WGPreps.getWgPreps().checkMultipleKarmaFlags(attacker);
            result = result * multi;
        }

        if (!attackerData.isWanted() && victimData.isWanted()) {
            if ((result > 0 && configData.cancelInnocentKarmaGain) || (result < 0 && configData.cancelInnocentKarmaLoss)) {
                doesKarmaChange = false;
            }
        } else if (attackerData.isWanted()) {
            if ((result > 0 && configData.cancelWantedKarmaGain) || (result < 0 && configData.cancelWantedKarmaLoss)) {
                doesKarmaChange = false;
            }
        }

        double attackerNewKarma = attackerInitialKarma + result;
        if (configData.wantedEnable && !(attacker.hasMetadata("NPC") || victim.hasMetadata("NPC"))) {
            wantedHandler(attacker, attackerNewKarma, victim, event);
        }

        if (doesKarmaChange) {
            PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(attacker, attackerNewKarma, true, event);
            Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        }
    }

    public static void pveHandler(Player attacker, LivingEntity entity, Event event) {
        String entityName = entity.getName();
        YamlConfiguration config = plugin.getCustomConfig();
        double reward;
        if (event instanceof EntityDeathEvent) {
            reward = config.getInt("entities." + entityName + ".kill-karma-reward");
        } else if (event instanceof EntityDamageByEntityEvent) {
            reward = config.getInt("entities." + entityName + ".hit-karma-reward");
        } else {
            return;
        }

        PlayerData attackerData = PlayerDataManager.getPlayerDataMap().get(attacker);
        double attackerKarma = attackerData.getKarma();

        if (configData.useWorldGuard) {
            reward = reward * WGPreps.getWgPreps().checkMultipleKarmaFlags(attacker);
        }

        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(attacker, attackerKarma + reward, true, event);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);

        if (!playerKarmaChangeEvent.isCancelled()) {
            if (event instanceof EntityDeathEvent) {
                adaptMessage.entityHitMessage(config.getString("entities." + entityName + ".kill-message"), attacker, event);
            } else {
                adaptMessage.entityHitMessage(config.getString("entities." + entityName + ".hit-message"), attacker, event);
            }
        }
    }

    public static boolean isPlayerNPC(Player player) {
        return player.hasMetadata("NPC");
    }

    public static boolean doesPlayerNPCHaveKarma(Player npc) {
        return npc.hasMetadata("Karma") && npc.getMetadata("Karma").get(0) != null;
    }

    public static boolean doesAttackerRisksGuilt(double attackerInitialKarma, double attackerNewKarma) {
        if (attackerNewKarma > attackerInitialKarma) {
            return configData.wantedOnKarmaGain;
        } else if (attackerNewKarma == attackerInitialKarma) {
            return configData.wantedOnKarmaUnchanged;
        } else {
            return configData.wantedOnKarmaLoss;
        }
    }

    private static void wantedHandler(Player attacker, double newKarma, Player victim, Object cause) {
        PlayerData attackerData = PlayerDataManager.getPlayerDataMap().get(attacker);

        double attackerInitialKarma = attackerData.getKarma();
        long attackerLastWanted = attackerData.getWantedTimeStamp().getTime();
        long victimLastWanted = PlayerDataManager.getPlayerDataMap().get(victim).getWantedTimeStamp().getTime();

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
            String expression = configData.wantedDurationExpression;
            if (attackerLastWanted < System.currentTimeMillis()) {
                expression = expression.replaceAll("%karma_player_wanted_time%", "%timestamp%");
            }
            expression = AdaptMessage.getAdaptMessage().adapt(attacker, expression, PlayerType.PLAYER.getText());
            Timestamp timestamp = new Timestamp((long) ExpressionCalculator.eval(expression));
            PlayerWantedChangeEvent playerWantedChangeEvent = new PlayerWantedChangeEvent(attacker, timestamp, cause);
            Bukkit.getPluginManager().callEvent(playerWantedChangeEvent);
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
