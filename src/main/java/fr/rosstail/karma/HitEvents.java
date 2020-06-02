package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.io.File;


/**
 * Changes the attacker karma when attacking entities
 */
public class HitEvents extends GetSet implements Listener {
    private Karma karma = Karma.get();
    AdaptMessage adaptMessage = new AdaptMessage();

    File lang = new File(this.karma.getDataFolder(), "lang/" + karma.getConfig().getString("general.lang") + ".yml");
    YamlConfiguration configurationLang = YamlConfiguration.loadConfiguration(lang);

    Player attacker = null;
    Player victim = null;
    Double damage = 0D;
    String message;

    /**
     * Changes karma when player attack another entity (animal or monster)
     * @param event
     */
    @EventHandler
    public void onEntityHurt(EntityDamageByEntityEvent event) {
        double reward = 0F;
        double attackerKarma = 0F;
        double attackerModifiedKarma = 0F;
        LivingEntity livingEntity;
        String livingEntityName;
        attacker = null;

        if (event.getEntity() instanceof LivingEntity && event.getFinalDamage() >= 1d && ((LivingEntity) event.getEntity()).getHealth() - event.getFinalDamage() > 0)
        {
            damage = event.getFinalDamage();
            livingEntity = (LivingEntity) event.getEntity();
            livingEntityName = livingEntity.toString().replaceAll("Craft", "");
            if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                if (projectile.getShooter() instanceof Player) {
                    attacker = (Player) projectile.getShooter();
                } else {
                    return;
                }
            }
            else if (event.getDamager() instanceof Player) {
                attacker = (Player) event.getDamager();
            } else
                return;
        } else
            return;

        if (attacker.hasMetadata("NPC")) {
            return;
        }

        if ( !(getTime(attacker)) ) {
            return;
        }

        if (livingEntity instanceof Player && attacker != null)
        {
            victim = ((Player) livingEntity).getPlayer();
            onPlayerHurt();
            return;
        }

        reward = karma.getConfig().getDouble("entities." + livingEntityName + ".hit-karma-reward");

        if (!(reward == 0 || attacker == null)) {
            attackerKarma = getPlayerKarma(attacker);

            if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")
                    && karma.getConfig().getBoolean("general.use-worldguard")) {

                WGPreps wgPreps = new WGPreps();
                double mult = wgPreps.chekMulKarmFlag(attacker);
                reward = reward * mult;
            }

            attackerModifiedKarma = attackerKarma + reward;

            setKarmaToPlayer(attacker, attackerModifiedKarma);
            setTierToPlayer(attacker);
        }

        message = karma.getConfig().getString("entities." + livingEntityName + ".hit-message");

        if (attacker != null) {
            adaptMessage.entityHitMessage(message, attacker, reward);
        }
    }

    /**
     * Launch When a player is hurt by another player.
     */
    public void onPlayerHurt() {

        double attackerInitialKarma = getPlayerKarma(attacker);
        double victimKarma = getPlayerKarma(victim);

        if (!(victim.getName().equals(attacker.getName())) && damage >= 1d) {
            double arg1 = karma.getConfig().getDouble("pvp.hit-reward-variables.1");
            String arg2Str = karma.getConfig().getString("pvp.hit-reward-variables.2");
            double arg2 = 0;
            double arg3 = karma.getConfig().getDouble("pvp.hit-reward-variables.3");
            double arg4 = karma.getConfig().getDouble("pvp.hit-reward-variables.4");

            if (arg2Str != null) {
                if (arg2Str.equalsIgnoreCase("<VICTIM_KARMA>")) {
                    if (!victim.hasMetadata("NPC")) {
                        arg2 = victimKarma;
                    }
                    else if (victim.hasMetadata("Karma")) {
                        if (victim.getMetadata("Karma").get(0) != null) {
                            arg2 = victim.getMetadata("Karma").get(0).asDouble();
                        } else {
                            return;
                        }
                    }
                } else {
                    arg2 = Double.parseDouble(arg2Str);
                }
            }

            double formula = arg1 * (arg2 + arg3) / arg4;

            if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")
                    && karma.getConfig().getBoolean("general.use-worldguard")) {

                WGPreps wgPreps = new WGPreps();
                double mult = wgPreps.chekMulKarmFlag(attacker);
                formula = formula * mult;
            }

            double attackerNewKarma = attackerInitialKarma + formula;

            if (karma.getConfig().getBoolean("pvp.crime-time.enable") && !(attacker.hasMetadata("NPC") || victim.hasMetadata("NPC"))) {
                Long timeStamp = System.currentTimeMillis();
                Long delay = karma.getConfig().getLong("pvp.crime-time.delay");

                Long attackEnd = getPlayerLastAttack(attacker) + delay * 1000;
                Long victimEnd = getPlayerLastAttack(victim) + delay * 1000;

                if (getPlayerLastAttack(attacker) != 0L && getPlayerLastAttack(victim) != 0L) {
                    if ( (timeStamp >= getPlayerLastAttack(attacker) && timeStamp <= attackEnd)
                            || timeStamp > victimEnd ) {
                        setLastAttackToPlayer(attacker);
                    } else {
                        if (!doesDefendChangeKarma(attackerInitialKarma, attackerNewKarma)) {
                            message = configurationLang.getString("self-defending-off");
                            adaptMessage.message(null, attacker, 0, message);
                            return;
                        }
                        message = configurationLang.getString("self-defending-on");
                        adaptMessage.message(null, attacker, 0, message);
                    }
                } else if (getPlayerLastAttack(victim) == 0L) {
                    setLastAttackToPlayer(attacker);
                } else if (getPlayerLastAttack(victim) != 0L) {
                    if (timeStamp >= getPlayerLastAttack(victim) && timeStamp <= victimEnd) {
                        if (!doesDefendChangeKarma(attackerInitialKarma, attackerNewKarma)) {
                            message = configurationLang.getString("self-defending-off");
                            adaptMessage.message(null, attacker, 0, message);
                            return;
                        }
                        message = configurationLang.getString("self-defending-on");
                        adaptMessage.message(null, attacker, 0, message);
                    } else {
                        setLastAttackToPlayer(attacker);
                    }
                }

            }

            setKarmaToPlayer(attacker, attackerNewKarma);
            setTierToPlayer(attacker);

            message = null;
            if (attackerNewKarma > attackerInitialKarma) {
                message = karma.getConfig().getString("pvp.hit-message-on-karma-increase");
            }
            else if (attackerNewKarma < attackerInitialKarma) {
                message = karma.getConfig().getString("pvp.hit-message-on-karma-decrease");
            }
            if (message != null) {
                adaptMessage.playerHitMessage(message, attacker, victim, attackerInitialKarma);
            }
        }
    }

    private boolean doesDefendChangeKarma(double attackerInitialKarma, double attackerNewKarma) {
        if (attackerNewKarma > attackerInitialKarma) {
            return karma.getConfig().getBoolean("pvp.crime-time.active-on-up");
        } else if (attackerNewKarma == attackerInitialKarma) {
            return karma.getConfig().getBoolean("pvp.crime-time.active-on-still");
        } else {
            return karma.getConfig().getBoolean("pvp.crime-time.active-on-down");
        }
    }
}