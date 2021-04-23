package fr.rosstail.karma.events;

import fr.rosstail.karma.configData.ConfigData;
import fr.rosstail.karma.datas.DataHandler;
import fr.rosstail.karma.Karma;
import fr.rosstail.karma.datas.PlayerData;
import fr.rosstail.karma.apis.WGPreps;
import fr.rosstail.karma.lang.AdaptMessage;
import fr.rosstail.karma.lang.LangManager;
import fr.rosstail.karma.lang.LangMessage;
import fr.rosstail.karma.lang.PlayerType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.graalvm.compiler.hotspot.stubs.DivisionByZeroExceptionStub;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


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
    @EventHandler
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
            Fights.pvpHandler(attacker, (Player) victimEntity, "hit");
            return;
        }
        Fights.pveHandler(attacker, victimEntity, "hit");
    }

    /**
     * Check and apply karma when a non-pLayer livingEntity is killed by a Player
     *
     * @param event
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (!CustomFightWorlds.isFightEnabledInWorld(victim.getWorld())) {
            return;
        }

        if (!(killer == null || Fights.isPlayerNPC(killer))) {
            Fights.pveHandler(killer, victim, "kill");
        }
    }

    /**
     * Apply a new karma to the Player KILLER when he kills another player
     *
     * @param event
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!CustomFightWorlds.isFightEnabledInWorld(victim.getWorld())) {
            return;
        }
        Player killer = victim.getKiller();
        if (!(killer == null || Fights.isPlayerNPC(killer))) {
            Fights.pvpHandler(killer, victim, "kill");
        }
    }
}
