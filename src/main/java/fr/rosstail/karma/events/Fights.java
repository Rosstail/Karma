package fr.rosstail.karma.events;

import fr.rosstail.karma.Karma;
import fr.rosstail.karma.apis.WGPreps;
import fr.rosstail.karma.configData.ConfigData;
import fr.rosstail.karma.datas.DataHandler;
import fr.rosstail.karma.datas.PlayerData;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.lang.LangMessage;
import fr.rosstail.karma.lang.PlayerType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Fights {

    private static final Karma plugin = Karma.getInstance();
    private static final AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();

    public static void pvpHandler(Player attacker, Player victim, String hitKill) {
        Object resultSE;
        double result = 0;

        PlayerData attackerData = PlayerData.gets(attacker, plugin);
        PlayerData victimData = PlayerData.gets(victim, plugin);

        PlayerData.tierCommandsLauncher(attacker, victim, victimData.getTier().getKilledCommands());;

        if (!DataHandler.getTime(attacker)) {
            return;
        }

        if (isPlayerNPC(attacker) || (isPlayerNPC(victim) && !doesPlayerNPCHaveKarma(victim))) {
            return;
        }

        double attackerInitialKarma = attackerData.getKarma();

        ConfigData configData = ConfigData.getConfigData();

        if (!victim.getName().equals(attacker.getName())) {

            String expression;
            if (hitKill.equals("hit")) {
                expression = configData.getPvpHitRewardExpression();
            } else {
                expression = configData.getPvpKillRewardExpression();
            }

            if (expression != null) {
                expression = adaptMessage.message(attacker, expression, PlayerType.attacker.getId());
                expression = adaptMessage.message(victim, expression, PlayerType.victim.getId());
                ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
                System.out.println("Hit expression " + expression);
                try {
                    // Evaluate the expression
                    resultSE = engine.eval(expression);
                    System.out.println("Expression value " + Double.parseDouble(resultSE.toString()));
                } catch (ScriptException e) {
                    // Something went wrong
                    e.printStackTrace();
                    resultSE = 0D;
                }
                if (configData.doesUseWorldGuard()) {
                    double multi = WGPreps.getWgPreps().checkMultipleKarmaFlags(attacker);
                    result = Double.parseDouble(resultSE.toString()) * multi;
                }

                double attackerNewKarma = attackerInitialKarma + result;

                System.out.println("New karma = " + attackerNewKarma);
                if (configData.isPvpCrimeTimeEnabled() && !(
                        attacker.hasMetadata("NPC") || victim.hasMetadata("NPC"))) {
                    long timeStamp = System.currentTimeMillis();
                    long delay = configData.getPvpCrimeTimeDelay();

                    double attackStart = attackerData.getLastAttack();
                    double victimStart = victimData.getLastAttack();
                    double attackEnd = attackerData.getLastAttack() + delay;
                    double victimEnd = victimData.getLastAttack() + delay;

                    if (attackStart != 0L && victimStart != 0L) {
                        if ((timeStamp >= attackStart && timeStamp <= attackEnd) || timeStamp > victimEnd) {
                            attackerData.setLastAttack();
                        } else {
                            if (doesDefendChangeKarma(attackerInitialKarma, attackerNewKarma)) {
                                attacker.sendMessage(adaptMessage.message(attacker, LangManager.getMessage(LangMessage.SELF_DEFENDING_OFF), PlayerType.attacker.getId()));
                                return;
                            }
                            attacker.sendMessage(adaptMessage.message(attacker, LangManager.getMessage(LangMessage.SELF_DEFENDING_ON), PlayerType.attacker.getId()));
                        }
                    } else if (victimStart == 0L) {
                        attackerData.setLastAttack();
                    } else if (victimStart != 0L) {
                        if (timeStamp >= victimStart && timeStamp <= victimEnd) {
                            if (doesDefendChangeKarma(attackerInitialKarma, attackerNewKarma)) {
                                attacker.sendMessage(adaptMessage.message(attacker, LangManager.getMessage(LangMessage.SELF_DEFENDING_OFF), PlayerType.attacker.getId()));
                                return;
                            }
                            attacker.sendMessage(adaptMessage.message(attacker, LangManager.getMessage(LangMessage.SELF_DEFENDING_ON), PlayerType.attacker.getId()));
                        } else {
                            attackerData.setLastAttack();
                        }
                    }

                }

                attackerData.setKarma(attackerNewKarma);
                attackerData.setOverTimerChange();

                String message = null;
                if (attackerNewKarma > attackerInitialKarma) {
                    if (hitKill.equals("hit")) {
                        message = configData.getPvpHitMessageKarmaIncrease();
                    } else {
                        message = configData.getPvpKillMessageKarmaIncrease();
                    }
                } else if (attackerNewKarma < attackerInitialKarma) {
                    if (hitKill.equals("hit")) {
                    message = configData.getPvpHitMessageKarmaDecrease();
                    } else {
                        message = configData.getPvpKillMessageKarmaDecrease();
                    }
                }
                if (message != null) {
                    adaptMessage.playerHitMessage(message, attacker, victim, hitKill);
                }

            }
        }
    }

    public static void pveHandler(Player attacker, LivingEntity entity, String hitKill) {
        String entityName = entity.getName();
        YamlConfiguration config = plugin.getCustomConfig();
        ConfigData configData = ConfigData.getConfigData();
        double reward = config.getInt("entities." + entityName + "." + hitKill  + "-karma-reward");
        if (reward == 0) {
            return;
        }

        PlayerData attackerData = PlayerData.gets(attacker, plugin);
        double killerKarma = attackerData.getKarma();

        if (configData.doesUseWorldGuard()) {
            reward = reward * WGPreps.getWgPreps().checkMultipleKarmaFlags(attacker);
        }

        attackerData.setKarma(killerKarma + reward);
        attackerData.setOverTimerChange();
        adaptMessage.entityHitMessage(config.getString("entities." + entityName + "." + hitKill + "-message"), attacker, hitKill);
    }

    public static boolean isPlayerNPC(Player player) {
        return player.hasMetadata("NPC");
    }

    public static boolean doesPlayerNPCHaveKarma(Player npc) {
        return npc.hasMetadata("Karma") && npc.getMetadata("Karma").get(0) != null;
    }

    public static boolean doesDefendChangeKarma(double attackerInitialKarma, double attackerNewKarma) {
        ConfigData configData = ConfigData.getConfigData();
        if (attackerNewKarma > attackerInitialKarma) {
            return !configData.isPvpCrimeTimeOnUp();
        } else if (attackerNewKarma == attackerInitialKarma) {
            return !configData.isPvpCrimeTimeOnStill();
        } else {
            return !configData.isPvpCrimeTimeOnDown();
        }
    }
}
