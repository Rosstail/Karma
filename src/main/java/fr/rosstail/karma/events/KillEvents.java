package fr.rosstail.karma.events;

import fr.rosstail.karma.datas.DataHandler;
import fr.rosstail.karma.Karma;
import fr.rosstail.karma.datas.PlayerData;
import fr.rosstail.karma.apis.WGPreps;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.lang.LangMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


/**
 * Changes the attacker karma when killing living entities
 */
public class KillEvents implements Listener {

    private final Karma plugin;

    Player killer = null;
    Player victim = null;
    private final AdaptMessage adaptMessage;

    public KillEvents(Karma plugin) {
        this.plugin = plugin;
        this.adaptMessage = AdaptMessage.getAdaptMessage();
    }

    /**
     * Check and apply karma when a non-pLayer livingEntity is killed by a Player
     *
     * @param event
     */
    @EventHandler public void onEntityDeath(EntityDeathEvent event) {

        double killerKarma;
        double reward;
        LivingEntity livingEntity = event.getEntity();

        killer = livingEntity.getKiller();
        String livingEntityName;
        PlayerData playerData;
        if (event.getEntity().getKiller() != null) {
            playerData = PlayerData.gets(killer, plugin);
            if (killer != null && DataHandler.getTime(killer)) {

                livingEntityName = livingEntity.toString().replaceAll("Craft", "");
            } else {
                return;
            }
        } else {
            return;
        }

        if (killer.hasMetadata("NPC")) {
            return;
        }

        reward = plugin.getConfig().getInt("entities." + livingEntityName + ".kill-karma-reward");
        if (reward == 0) {
            return;
        }

        killerKarma = playerData.getKarma();

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && plugin
            .getConfig().getBoolean("general.use-worldguard")) {

            WGPreps wgPreps = new WGPreps();
            double mult = wgPreps.checkMultipleKarmaFlags(killer);
            reward = reward * mult;
        }

        playerData.setKarma(killerKarma + reward);

        adaptMessage.entityKillMessage(plugin.getConfig().getString("entities." + livingEntityName + ".kill-message"), killer);
    }

    /**
     * Apply a new karma to the Player KILLER when he kills another player
     *
     * @param event
     */
    @EventHandler public void onPlayerDeath(PlayerDeathEvent event) {
        victim = event.getEntity();
        killer = victim.getKiller();
        Object resultSE = null;
        double result = 0;

        if (killer == null) {
            return;
        }

        PlayerData killerData = PlayerData.gets(killer, plugin);
        PlayerData victimData = PlayerData.gets(victim, plugin);

        if (!DataHandler.getTime(killer)) {
            return;
        }

        double killerInitialKarma = killerData.getKarma();
        double victimKarma = victimData.getKarma();

        if (killer.hasMetadata("NPC")) {
            return;
        }

        if (!victim.getName().equals(killer.getName())) {

            String expression = plugin.getConfig().getString("pvp.kill-reward-expression");

            if (expression != null) {
                if (expression.contains("<VICTIM_KARMA>")) {
                    if (!isVictimNPC()) {
                        expression = expression.replaceAll("<VICTIM_KARMA>", String.valueOf(victimKarma));
                    } else if (isVictimNPCHaveKarma()) {
                        expression = expression.replaceAll("<VICTIM_KARMA>", String.valueOf(victim.getMetadata("Karma").get(0).asDouble()));
                    } else {
                        return;
                    }
                }
                ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
                try {
                    // Evaluate the expression
                    resultSE = engine.eval(expression);
                } catch (ScriptException e) {
                    // Something went wrong
                    e.printStackTrace();
                }
                if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard") && plugin
                        .getConfig().getBoolean("general.use-worldguard")) {

                    WGPreps wgPreps = new WGPreps();
                    double multi = wgPreps.checkMultipleKarmaFlags(killer);
                    result = Double.parseDouble(resultSE.toString()) * multi;
                }

                double killerNewKarma = killerInitialKarma + result;

                if (plugin.getConfig().getBoolean("pvp.crime-time.enable") && !(
                        killer.hasMetadata("NPC") || victim.hasMetadata("NPC"))) {
                    long timeStamp = System.currentTimeMillis();
                    long delay = plugin.getConfig().getLong("pvp.crime-time.delay");

                    double attackStart = killerData.getLastAttack();
                    double victimStart = victimData.getLastAttack();
                    double attackEnd = killerData.getLastAttack() + delay * 1000;
                    double victimEnd = victimData.getLastAttack() + delay * 1000;

                    if (attackStart != 0L
                            && victimStart != 0L) {
                        if ((timeStamp >= attackStart && timeStamp <= attackEnd)
                                || timeStamp > victimEnd) {
                            killerData.setLastAttackToPlayer();
                        } else {
                            if (doesDefendChangeKarma(killerInitialKarma, killerNewKarma)) {
                                adaptMessage.message(null, killer, 0, LangManager.getMessage(LangMessage.SELF_DEFENDING_OFF));
                                return;
                            }
                            adaptMessage.message(null, killer, 0, LangManager.getMessage(LangMessage.SELF_DEFENDING_ON));
                        }
                    } else if (victimStart == 0L) {
                        killerData.setLastAttackToPlayer();
                    } else if (victimStart != 0L) {
                        if (timeStamp >= victimStart && timeStamp <= victimEnd) {
                            if (doesDefendChangeKarma(killerInitialKarma, killerNewKarma)) {
                                adaptMessage.message(null, killer, 0, LangManager.getMessage(LangMessage.SELF_DEFENDING_OFF));
                                return;
                            }
                            adaptMessage.message(null, killer, 0, LangManager.getMessage(LangMessage.SELF_DEFENDING_ON));
                        } else {
                            killerData.setLastAttackToPlayer();
                        }
                    }

                }

                killerData.setKarma(killerNewKarma);

                String message = null;
                if (killerNewKarma > killerInitialKarma) {
                    message = plugin.getConfig().getString("pvp.kill-message-on-karma-increase");
                } else if (killerNewKarma < killerInitialKarma) {
                    message = plugin.getConfig().getString("pvp.kill-message-on-karma-decrease");
                }
                if (message != null) {
                    adaptMessage.playerKillMessage(message, killer, victim, killerInitialKarma);
                }

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
            return !plugin.getConfig().getBoolean("pvp.crime-time.active-on-up");
        } else if (attackerNewKarma == attackerInitialKarma) {
            return !plugin.getConfig().getBoolean("pvp.crime-time.active-on-still");
        } else {
            return !plugin.getConfig().getBoolean("pvp.crime-time.active-on-down");
        }
    }
}
