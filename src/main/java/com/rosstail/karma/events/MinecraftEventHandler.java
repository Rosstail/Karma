package com.rosstail.karma.events;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.events.karmaevents.*;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.StorageManager;
import com.rosstail.karma.overtime.OvertimeLoop;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.tiers.TierManager;
import com.rosstail.karma.timemanagement.TimeManager;
import org.bukkit.Bukkit;
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

import java.sql.Timestamp;
import java.util.Map;

public class MinecraftEventHandler implements Listener {

    public MinecraftEventHandler() {

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = StorageManager.getManager().selectPlayerModel(event.getPlayer().getUniqueId().toString());
        if (model == null) {
            model = new PlayerModel(event.getPlayer());
            StorageManager.getManager().insertPlayerModel(model);
            PlayerDataManager.initPlayerModelToMap(model);

            //Event join tier by default
            PlayerTierChangeEvent defaultTierJoinEvent = new PlayerTierChangeEvent(event.getPlayer(), model, TierManager.getTierManager().getTierByKarmaAmount(model.getKarma()).getName());
            Bukkit.getPluginManager().callEvent(defaultTierJoinEvent);

            /*
            Overtime setup with initial timer
             */
            if (ConfigData.getConfigData().overtimeActive) {
                for (Map.Entry<String, OvertimeLoop> entry : ConfigData.getConfigData().overtimeLoopMap.entrySet()) {
                    String overtimeName = entry.getKey();
                    OvertimeLoop overtimeLoop = entry.getValue();
                    PlayerDataManager.setOverTimeStamp(model, overtimeName, overtimeLoop.firstTimer);
                }
            }
        } else {
            PlayerDataManager.initPlayerModelToMap(model);

            /*
            Overtime setup WITH/OUT check
             */
            if (ConfigData.getConfigData().overtimeActive) {
                long lastUpdate = model.getLastUpdate();
                long deltaUpdates = System.currentTimeMillis() - lastUpdate;

                for (Map.Entry<String, OvertimeLoop> entry : ConfigData.getConfigData().overtimeLoopMap.entrySet()) {
                    String overtimeName = entry.getKey();
                    OvertimeLoop overtimeLoop = entry.getValue();
                    long loopDelta =  deltaUpdates - overtimeLoop.firstTimer;

                    long delay = overtimeLoop.firstTimer; //default online only overtime timer
                    if (overtimeLoop.offline) {
                        int occurrenceAmount = (int) (Math.floorDiv(loopDelta, overtimeLoop.nextTimer) + 1);
                        delay = loopDelta % overtimeLoop.nextTimer;

                        if (occurrenceAmount > 0) {
                            Bukkit.getPluginManager().callEvent(new PlayerOverTimeTriggerEvent(player, overtimeName, occurrenceAmount, delay));
                        } else {
                            delay = -loopDelta;
                        }
                    }

                    PlayerDataManager.setOverTimeStamp(model, overtimeName, delay);
                }
            }

            /*
            CHECK PLAYER TIER
             */
            TierManager tierManager = TierManager.getTierManager();
            Tier currentKarmaTier = tierManager.getTierByKarmaAmount(model.getKarma());
            Tier modelTier = tierManager.getTierByName(model.getTierName());
            if (!currentKarmaTier.equals(modelTier)) {
                PlayerTierChangeEvent tierChangeEvent = new PlayerTierChangeEvent(player, model, currentKarmaTier.getName());
                Bukkit.getPluginManager().callEvent(tierChangeEvent);
            }

            /*
            CHECK PLAYER WANTED STATUS
             */
            if (ConfigData.getConfigData().wantedEnable) {
                if (model.isWanted()) {
                    if (PlayerDataManager.getWantedTimeLeft(model) > 0L) {
                        PlayerWantedPeriodRefreshEvent playerWantedPeriodRefreshEvent = new PlayerWantedPeriodRefreshEvent(player, model);
                        Bukkit.getPluginManager().callEvent(playerWantedPeriodRefreshEvent);
                    } else {
                        PlayerWantedPeriodEndEvent playerWantedPeriodEndEvent = new PlayerWantedPeriodEndEvent(player, model);
                        Bukkit.getPluginManager().callEvent(playerWantedPeriodEndEvent);
                    }
                } else if (PlayerDataManager.getWantedTimeLeft(model) > 0L) {
                    PlayerWantedPeriodStartEvent playerWantedPeriodStartEvent = new PlayerWantedPeriodStartEvent(player, model);
                    Bukkit.getPluginManager().callEvent(playerWantedPeriodStartEvent);
                }
            }
        }
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
