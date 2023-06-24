package com.rosstail.karma.events;

import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.DBInteractions;
import com.rosstail.karma.datas.storage.StorageManager;
import com.rosstail.karma.timemanagement.TimeManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;

public class MinecraftEventHandler implements Listener {

    public MinecraftEventHandler() {

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerModel model = StorageManager.getManager().selectPlayerModel(event.getPlayer().getUniqueId().toString());
        if (model == null) {
            model = new PlayerModel(event.getPlayer());
            StorageManager.getManager().insertPlayerModel(model);
        }
        PlayerDataManager.initPlayerModelToMap(model);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        StorageManager.getManager().updatePlayerModel(model);
        PlayerDataManager.removePlayerModelFromMap(player);
    }

    /**
     * Check and apply karma when a non-pLayer livingEntity is killed by a Player
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (!WorldFights.isFightEnabledInWorld(victim.getWorld())) {
            return;
        }

        if (!(killer == null || Fights.isFakePlayer(killer))) {
            if (!killer.hasPermission("karma.immune")) {
                Fights.pveHandler(killer, victim, event);
            }
        }
    }

    /**
     * Apply a new karma to the Player KILLER when he kills another player
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!WorldFights.isFightEnabledInWorld(victim.getWorld())) {
            return;
        }
        Player killer = victim.getKiller();
        if (!(killer == null || killer == victim || Fights.isFakePlayer(killer))) {
            if (!killer.hasPermission("karma.immune")) {
                Fights.pvpHandler(killer, victim, event);
            }
        }
    }

    /**
     * Changes karma when player attack another entity (animal or monster)
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityHurt(EntityDamageByEntityEvent event) {
        LivingEntity victimEntity;

        double damage = event.getFinalDamage();
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity && damage >= 1f && ((LivingEntity) entity).getHealth() - damage > 0f)) {
            return;
        }

        victimEntity = (LivingEntity) event.getEntity();
        if (!WorldFights.isFightEnabledInWorld(victimEntity.getWorld())) {
            return;
        }

        Player attacker = getFightAttacker(event);
        if (attacker != null) {
            if (Fights.isFakePlayer(attacker) || !TimeManager.getTimeManager().isPlayerInTime(attacker)) {
                return;
            }

            if (!attacker.hasPermission("karma.immune")) {
                if (victimEntity instanceof Player && !attacker.equals(victimEntity)) {
                    Fights.pvpHandler(attacker, (Player) victimEntity, event);
                } else {
                    Fights.pveHandler(attacker, victimEntity, event);
                }
            } else {
                attacker.sendMessage("You are immune to karma change");
            }
        }
    }

    private Player getFightAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        } else if (event.getDamager() instanceof Player) {
            return (Player) event.getDamager();
        }
        return null;
    }
}
