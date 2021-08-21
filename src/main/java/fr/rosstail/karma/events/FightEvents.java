package fr.rosstail.karma.events;

import fr.rosstail.karma.datas.DataHandler;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;


/**
 * Changes the attacker karma when attacking entities
 */
public class FightEvents implements Listener {

    public FightEvents() {
        super();
    }

    /**
     * Changes karma when player attack another entity (animal or monster)
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityHurt(EntityDamageByEntityEvent event) {
        LivingEntity victimEntity;

        double damage = event.getFinalDamage();
        if (!(event.getEntity() instanceof LivingEntity && damage >= 1d
            && ((LivingEntity) event.getEntity()).getHealth() - damage > 0)) {
            return;
        }

        victimEntity = (LivingEntity) event.getEntity();
        if (!CustomFightWorlds.isFightEnabledInWorld(victimEntity.getWorld())) {
            return;
        }

        Player attacker;

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

        if (Fights.isPlayerNPC(attacker) || !DataHandler.getTime(attacker)) {
            return;
        }

        if (victimEntity instanceof Player) {
            Fights.pvpHandler(attacker, (Player) victimEntity, Reasons.HIT);
            return;
        }
        Fights.pveHandler(attacker, victimEntity, Reasons.HIT);
    }

    /**
     * Check and apply karma when a non-pLayer livingEntity is killed by a Player
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (!CustomFightWorlds.isFightEnabledInWorld(victim.getWorld())) {
            return;
        }

        if (!(killer == null || Fights.isPlayerNPC(killer))) {
            Fights.pveHandler(killer, victim, Reasons.KILL);
        }
    }

    /**
     * Apply a new karma to the Player KILLER when he kills another player
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!CustomFightWorlds.isFightEnabledInWorld(victim.getWorld())) {
            return;
        }
        Player killer = victim.getKiller();
        if (!(killer == null || Fights.isPlayerNPC(killer))) {
            Fights.pvpHandler(killer, victim, Reasons.KILL);
        }
    }
}
