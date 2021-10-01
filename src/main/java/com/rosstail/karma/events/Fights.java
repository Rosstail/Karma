package com.rosstail.karma.events;

import com.rosstail.karma.Karma;
import com.rosstail.karma.apis.ExpressionCalculator;
import com.rosstail.karma.apis.WGPreps;
import com.rosstail.karma.configdata.ConfigData;
import com.rosstail.karma.customevents.PlayerKarmaChangeEvent;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.timemanagement.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Fights {

    private static final Karma plugin = Karma.getInstance();
    private static ConfigData configData = ConfigData.getConfigData();
    private static final AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();

    public static void pvpHandler(Player attacker, Player victim, Reasons reason, Object cause) {
        boolean isPlayerInTime = TimeManager.getTimeManager().isPlayerInTime(attacker);
        if (!isPlayerInTime || isPlayerNPC(attacker) || (isPlayerNPC(victim) && !doesPlayerNPCHaveKarma(victim))) {
            return;
        }

        PlayerData victimData = PlayerData.gets(victim);
        PlayerData attackerData = PlayerData.gets(attacker);
        double attackerInitialKarma = attackerData.getKarma();

        if (reason.equals(Reasons.KILL)) {
            PlayerData.commandsLauncher(attacker, victim, victimData.getTier().getKilledCommands());

            String path = configData.getKilledByTierPath();

            path = adaptMessage.message(victim, path, PlayerType.victim.getId());
            path = adaptMessage.message(attacker, path, PlayerType.attacker.getId());
            PlayerData.commandsLauncher(attacker, victim, plugin.getCustomConfig().getStringList(path));
        }

        ConfigData configData = ConfigData.getConfigData();
        String expression;

        if (reason.equals(Reasons.KILL)) {
            expression = configData.getPvpKillRewardExpression();
        } else {
            expression = configData.getPvpHitRewardExpression();
        }

        if (expression == null) {
            return;
        }

        double result;

        expression = adaptMessage.message(attacker, expression, PlayerType.attacker.getId());
        expression = adaptMessage.message(victim, expression, PlayerType.victim.getId());

        expression = expression.replaceAll("%karma_attacker_victim_tier_score%",
                String.valueOf(PlayerData.gets(attacker).getTier().getTierScore(PlayerData.gets(victim).getTier())));

        result = ExpressionCalculator.eval(expression);
        if (configData.doesUseWorldGuard()) {
            double multi = WGPreps.getWgPreps().checkMultipleKarmaFlags(attacker);
            result = result * multi;
        }
        double attackerNewKarma = attackerInitialKarma + result;

        if (configData.isPvpCrimeTimeEnabled() && !(
                attacker.hasMetadata("NPC") || victim.hasMetadata("NPC"))) {
            long timeStamp = System.currentTimeMillis();
            long delay = configData.getPvpCrimeTimeDelay();

            float attackStart = attackerData.getLastAttack().getTime();
            float victimStart = attackerData.getLastAttack().getTime();
            float attackEnd = attackStart + delay;
            float victimEnd = victimStart + delay;

            if (attackStart != 0L && victimStart != 0L) {
                if ((timeStamp >= attackStart && timeStamp <= attackEnd) || timeStamp > victimEnd) {
                    attackerData.setLastAttack();
                } else {
                    if (doesDefendChangeKarma(attackerInitialKarma, attackerNewKarma)) {
                        attacker.sendMessage(adaptMessage.message(attacker,
                                LangManager.getMessage(LangMessage.SELF_DEFENDING_OFF), PlayerType.attacker.getId()));
                        return;
                    }
                    attacker.sendMessage(adaptMessage.message(attacker,
                            LangManager.getMessage(LangMessage.SELF_DEFENDING_ON), PlayerType.attacker.getId()));
                }
            } else if (victimStart == 0L) {
                attackerData.setLastAttack();
            } else if (victimStart != 0L) {
                if (timeStamp >= victimStart && timeStamp <= victimEnd) {
                    if (doesDefendChangeKarma(attackerInitialKarma, attackerNewKarma)) {
                        attacker.sendMessage(adaptMessage.message(attacker,
                                LangManager.getMessage(LangMessage.SELF_DEFENDING_OFF), PlayerType.attacker.getId()));
                        return;
                    }
                    attacker.sendMessage(adaptMessage.message(attacker,
                            LangManager.getMessage(LangMessage.SELF_DEFENDING_ON), PlayerType.attacker.getId()));
                } else {
                    attackerData.setLastAttack();
                }
            }

        }

        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(attacker, attackerNewKarma, true, cause);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
        if (!playerKarmaChangeEvent.isCancelled()) {
            String message = null;
            if (attackerNewKarma > attackerInitialKarma) {
                if (reason.equals(Reasons.HIT)) {
                    message = configData.getPvpHitMessageKarmaIncrease();
                } else {
                    message = configData.getPvpKillMessageKarmaIncrease();
                }
            } else if (attackerNewKarma < attackerInitialKarma) {
                if (reason.equals(Reasons.HIT)) {
                    message = configData.getPvpHitMessageKarmaDecrease();
                } else {
                    message = configData.getPvpKillMessageKarmaDecrease();
                }
            }
            if (message != null) {
                adaptMessage.playerHitMessage(message, attacker, victim, reason.getText());
            }
        }
    }

    public static void pveHandler(Player attacker, LivingEntity entity, Reasons reason, Object cause) {
        String entityName = entity.getName();
        System.out.println(entityName);
        YamlConfiguration config = plugin.getCustomConfig();
        double reward = config.getInt("entities." + entityName + "." + reason.getText()  + "-karma-reward");
        if (reward == 0) {
            return;
        }

        PlayerData attackerData = PlayerData.gets(attacker);
        double attackerKarma = attackerData.getKarma();

        if (configData.doesUseWorldGuard()) {
            reward = reward * WGPreps.getWgPreps().checkMultipleKarmaFlags(attacker);
        }

        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(attacker, attackerKarma + reward, true, cause);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);

        if (!playerKarmaChangeEvent.isCancelled()) {
            adaptMessage.entityHitMessage(config.getString("entities." + entityName + "." + reason.getText() + "-message"), attacker, reason);
        }
    }

    public static boolean isPlayerNPC(Player player) {
        return player.hasMetadata("NPC");
    }

    public static boolean doesPlayerNPCHaveKarma(Player npc) {
        return npc.hasMetadata("Karma") && npc.getMetadata("Karma").get(0) != null;
    }

    public static boolean doesDefendChangeKarma(double attackerInitialKarma, double attackerNewKarma) {
        if (attackerNewKarma > attackerInitialKarma) {
            return !configData.isPvpCrimeTimeOnUp();
        } else if (attackerNewKarma == attackerInitialKarma) {
            return !configData.isPvpCrimeTimeOnStill();
        } else {
            return !configData.isPvpCrimeTimeOnDown();
        }
    }

    public static void setConfigData(ConfigData configData) {
        Fights.configData = configData;
    }
}
