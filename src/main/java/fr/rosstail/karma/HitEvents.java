package fr.rosstail.karma;

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
        int reward = 0;
        int attackerKarma = 0;
        int attackerModifiedKarma = 0;
        LivingEntity livingEntity;
        String livingEntityName;
        attacker = null;

        if (event.getEntity() instanceof LivingEntity && event.getFinalDamage() >= 1d && ((LivingEntity) event.getEntity()).getHealth() - event.getFinalDamage() > 0)
        {
            livingEntity = (LivingEntity) event.getEntity();
            livingEntityName = livingEntity.toString().replaceAll("Craft", "");
            if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                if (projectile.getShooter() instanceof Player)
                    attacker = (Player) projectile.getShooter();
            }
            else if (event.getDamager() instanceof Player)
                attacker = (Player) event.getDamager();
            else
                return;
        }
        else
            return;

        if (livingEntity instanceof Player && attacker != null)
        {
            victim = ((Player) livingEntity).getPlayer();
            onPlayerHurt();
            return;
        }

        reward = karma.getConfig().getInt("entities." + livingEntityName + ".hit-karma-reward");

        if (!(reward == 0 || attacker == null)) {
            attackerKarma = getPlayerKarma(attacker);
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

        int attackerInitialKarma = getPlayerKarma(attacker);
        int victimKarma = getPlayerKarma(victim);

        if (!victim.getName().equals(attacker.getName()) && victim.getLastDamage() >= 1d) {
            int arg1 = karma.getConfig().getInt("pvp.hit-reward-variables.1");
            String arg2Str = karma.getConfig().getString("pvp.hit-reward-variables.2");
            int arg2 = 0;
            int arg3 = karma.getConfig().getInt("pvp.hit-reward-variables.3");
            int arg4 = karma.getConfig().getInt("pvp.hit-reward-variables.4");

            if (arg2Str != null) {
                if (arg2Str.equals("<victimKarma>")) {
                    arg2 = victimKarma;
                } else
                    arg2 = Integer.parseInt(arg2Str);
            }

            int attackerNewKarma = attackerInitialKarma + arg1 * (arg2 + arg3) / arg4;

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