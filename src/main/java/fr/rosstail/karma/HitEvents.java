package fr.rosstail.karma;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;


/**
 * Changes the attacker karma when attacking entities
 */
public class HitEvents extends GetSet implements Listener {
    private Karma karma = Karma.get();
    AdaptMessage adaptMessage = new AdaptMessage();

    Player attacker = null;
    Player victim = null;
    String message;

    /**
     * Changes karma when player attack another entity (animal or monster)
     * @param event
     */
    @EventHandler
    public void onEntityHurt(EntityDamageByEntityEvent event) {
        double reward = 0;
        double attackerKarma = 0;
        double attackerModifiedKarma = 0;
        LivingEntity livingEntity;
        String livingEntityName;
        attacker = null;

        if (event.getEntity() instanceof LivingEntity && event.getFinalDamage() >= 1d && ((LivingEntity) event.getEntity()).getHealth() - event.getFinalDamage() > 0)
        {
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
            else if (event.getDamager() instanceof Player)
                attacker = (Player) event.getDamager();
            else
                return;
        }
        else
            return;

        if (!getTime(attacker)) {
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

        if (!(message == null || attacker == null)) {
            adaptMessage.getEntityHitMessage(message, attacker, reward);
        }
    }

    /**
     * Launch When a player is hurt by another player.
     */
    public void onPlayerHurt() {

        double attackerInitialKarma = getPlayerKarma(attacker);
        double victimKarma = getPlayerKarma(victim);

        if (!victim.getName().equals(attacker.getName()) && victim.getLastDamage() >= 1d) {
            double arg1 = karma.getConfig().getDouble("pvp.hit-reward-variables.1");
            String arg2Str = karma.getConfig().getString("pvp.hit-reward-variables.2");
            double arg2 = 0;
            double arg3 = karma.getConfig().getDouble("pvp.hit-reward-variables.3");
            double arg4 = karma.getConfig().getDouble("pvp.hit-reward-variables.4");

            if (arg2Str != null) {
                if (arg2Str.equals("<victimKarma>")) {
                    arg2 = victimKarma;
                } else
                    arg2 = Double.parseDouble(arg2Str);
            }

            double formula = arg1 * (arg2 + arg3) / arg4;

            if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")
                    && karma.getConfig().getBoolean("general.use-worldguard")) {

                WGPreps wgPreps = new WGPreps();
                double mult = wgPreps.chekMulKarmFlag(attacker);
                formula = formula * mult;
            }

            double attackerNewKarma = attackerInitialKarma + formula;

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
                adaptMessage.getPlayerHitMessage(message, attacker, attackerInitialKarma);
            }
        }
    }
}