package fr.rosstail.karma.events;

import fr.rosstail.karma.configData.ConfigData;
import fr.rosstail.karma.datas.DataHandler;
import fr.rosstail.karma.Karma;
import fr.rosstail.karma.datas.PlayerData;
import fr.rosstail.karma.apis.WGPreps;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.lang.LangMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


/**
 * Changes the attacker karma when attacking entities
 */
public class HitEvents implements Listener {
    private final Karma plugin;
    private final FileConfiguration config;
    private final AdaptMessage adaptMessage = AdaptMessage.getAdaptMessage();
    private  final ConfigData configData = ConfigData.getConfigData();

    private Player attacker;
    private Player victim;
    private double damage;
    private String message;

    public HitEvents(Karma plugin) {
        super();
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    /**
     * Changes karma when player attack another entity (animal or monster)
     *
     * @param event
     */
    @EventHandler
    public void onEntityHurt(EntityDamageByEntityEvent event) {
        double reward;
        double attackerKarma;
        double attackerModifiedKarma;
        LivingEntity victimEntity;
        String livingEntityName;
        attacker = null;
        if (!(event.getEntity() instanceof LivingEntity && event.getFinalDamage() >= 1d
            && ((LivingEntity) event.getEntity()).getHealth() - event.getFinalDamage() > 0)) {
            return;
        }

        damage = event.getFinalDamage();
        victimEntity = (LivingEntity) event.getEntity();
        if (!CustomFightWorlds.isFightEnabledInWorld(victimEntity.getWorld())) {
            return;
        }

        livingEntityName = victimEntity.toString().replaceAll("Craft", "");
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            } else {
                return;
            }
        } else if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else {
            return;
        }

        if (attacker.hasMetadata("NPC") || !DataHandler.getTime(attacker)) {
            return;
        }

        if (victimEntity instanceof Player && attacker != null) {
            victim = ((Player) victimEntity).getPlayer();
            onPlayerHurt();
            return;
        }
        PlayerData attackerData = PlayerData.gets(attacker, plugin);

        reward = config.getDouble("entities." + livingEntityName + ".hit-karma-reward");

        if (!(reward == 0 || attacker == null)) {
            attackerKarma = attackerData.getKarma();

            if (configData.doesUseWorldGuard()) {
                WGPreps wgPreps = new WGPreps();
                double mult = wgPreps.checkMultipleKarmaFlags(attacker);
                reward = reward * mult;
            }

            attackerModifiedKarma = attackerKarma + reward;

            attackerData.setKarma(attackerModifiedKarma);
            attackerData.setOverTimerChange();
        }

        message = config.getString("entities." + livingEntityName + ".hit-message");

        if (attacker != null) {
            adaptMessage.entityHitMessage(message, attacker, reward);
        }
    }

    /**
     * Launch When a player is hurt by another player.
     */
    public void onPlayerHurt() {
        PlayerData attackerData = PlayerData.gets(attacker, plugin);
        PlayerData victimData = PlayerData.gets(victim, plugin);
        Object resultSE = null;
        double result = 0;

        double attackerInitialKarma = attackerData.getKarma();
        double victimKarma = victimData.getKarma();

        if (!(!(victim.getName().equals(attacker.getName())) && damage >= 1d)) {
            return;
        }

        String expression = configData.getPvpHitRewardExpression();

        if (expression != null) {
            if (expression.contains("<VICTIM_KARMA>")) {
                if (!isVictimNPC()) {
                    expression = expression.replaceAll("<VICTIM_KARMA>", String.valueOf(victimKarma));
                } else if (isVictimNPCHaveKarma()){
                    expression = expression.replaceAll("<VICTIM_KARMA>", String.valueOf(victim.getMetadata("Karma").get(0).asDouble()));
                } else {
                    return;
                }
            }
            ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
            try {
                // Evaluate the expression
                resultSE = engine.eval(expression);
            }
            catch (ScriptException e) {
                // Something went wrong
                e.printStackTrace();
            }

            if (configData.doesUseWorldGuard()) {
                WGPreps wgPreps = new WGPreps();
                double mult = wgPreps.checkMultipleKarmaFlags(attacker);
                result = Double.parseDouble(resultSE.toString()) * mult;
            }

            double attackerNewKarma = attackerInitialKarma + result;

            if (configData.isPvpCrimeTimeEnabled() && !(attacker.hasMetadata("NPC")
                    || victim.hasMetadata("NPC"))) {
                long timeStamp = System.currentTimeMillis();
                long delay = configData.getPvpCrimeTimeDelay();

                double attackStart = attackerData.getLastAttack();
                double attackEnd = attackerData.getLastAttack() + delay * 1000;
                double victimStart = victimData.getLastAttack();
                double victimEnd = victimData.getLastAttack() + delay * 1000;

                if (attackStart != 0L
                        && victimStart != 0L) {
                    if ((timeStamp >= attackStart && timeStamp <= attackEnd)
                            || timeStamp > victimEnd) {
                        attackerData.setLastAttack();
                    } else {
                        if (doesDefendChangeKarma(attackerInitialKarma, attackerNewKarma)) {
                            attacker.sendMessage(adaptMessage.message(attacker, 0, LangManager.getMessage(LangMessage.SELF_DEFENDING_OFF)));
                            return;
                        }
                        attacker.sendMessage(adaptMessage.message(attacker, 0, LangManager.getMessage(LangMessage.SELF_DEFENDING_ON)));
                    }
                } else if (attackStart == 0L) {
                    attackerData.setLastAttack();
                } else if (victimStart != 0L) {
                    if (timeStamp >= victimStart && timeStamp <= victimEnd) {
                        if (doesDefendChangeKarma(attackerInitialKarma, attackerNewKarma)) {
                            attacker.sendMessage(adaptMessage.message(attacker, 0, LangManager.getMessage(LangMessage.SELF_DEFENDING_OFF)));
                            return;
                        }
                        attacker.sendMessage(adaptMessage.message(attacker, 0, LangManager.getMessage(LangMessage.SELF_DEFENDING_ON)));
                    } else {
                        attackerData.setLastAttack();
                    }
                }

            }

            attackerData.setKarma(attackerNewKarma);
            attackerData.setOverTimerChange();

            message = null;
            if (attackerNewKarma > attackerInitialKarma) {
                message = configData.getPvpHitMessageKarmaIncrease();
            } else if (attackerNewKarma < attackerInitialKarma) {
                message = configData.getPvpHitMessageKarmaDecrease();
            }
            if (message != null) {
                adaptMessage.playerHitMessage(message, attacker, victim, attackerInitialKarma);
            }
        }
    }

    private boolean isVictimNPC() {
        return victim.hasMetadata("NPC");
    }

    private boolean isVictimNPCHaveKarma() {
        return victim.hasMetadata("Karma") && victim.getMetadata("Karma").get(0) != null;
    }

    private boolean doesDefendChangeKarma(double attackerInitialKarma, double attackerNewKarma) {
        if (attackerNewKarma > attackerInitialKarma) {
            return !configData.isPvpCrimeTimeOnUp();
        } else if (attackerNewKarma == attackerInitialKarma) {
            return !configData.isPvpCrimeTimeOnStill();
        } else {
            return !configData.isPvpCrimeTimeOnDown();
        }
    }
}
