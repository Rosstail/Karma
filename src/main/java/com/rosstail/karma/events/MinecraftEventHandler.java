package com.rosstail.karma.events;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.events.karmaevents.*;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.datas.storage.StorageManager;
import com.rosstail.karma.events.testevents.PlayerDamageMobEvent;
import com.rosstail.karma.events.testevents.PlayerDamagePlayerEvent;
import com.rosstail.karma.events.testevents.PlayerKillMobEvent;
import com.rosstail.karma.events.testevents.PlayerKillPlayerEvent;
import com.rosstail.karma.fight.FightHandler;
import com.rosstail.karma.fight.WorldFights;
import com.rosstail.karma.fight.pvpcommandhandlers.PvpCommandHandler;
import com.rosstail.karma.overtime.OvertimeLoop;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.tiers.TierManager;
import com.rosstail.karma.timeperiod.TimeManager;
import com.rosstail.karma.wanted.WantedManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Map;

public class MinecraftEventHandler implements Listener {

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
            if (ConfigData.getConfigData().overtime.overtimeActive) {
                for (Map.Entry<String, OvertimeLoop> entry : ConfigData.getConfigData().overtime.overtimeLoopMap.entrySet()) {
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
            if (ConfigData.getConfigData().overtime.overtimeActive) {
                long lastUpdate = model.getLastUpdate();
                long deltaUpdates = System.currentTimeMillis() - lastUpdate;

                for (Map.Entry<String, OvertimeLoop> entry : ConfigData.getConfigData().overtime.overtimeLoopMap.entrySet()) {
                    String overtimeName = entry.getKey();
                    OvertimeLoop overtimeLoop = entry.getValue();
                    long loopDelta = deltaUpdates - overtimeLoop.firstTimer;

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
            if (ConfigData.getConfigData().wanted.wantedEnable) {
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

        if (killer == null || !(victim instanceof Mob)) {
            return;
        }
        if (!WorldFights.isFightEnabledInWorld(victim.getWorld())) {
            return;
        }
        if (killer.hasPermission("karma.immune")) {
            return;
        }

        if (!FightHandler.isFakePlayer(killer)) {
            PlayerKillMobEvent pveKillEvent = new PlayerKillMobEvent(killer, (Mob) victim);
            Bukkit.getPluginManager().callEvent(pveKillEvent);

            if (!pveKillEvent.isCancelled()) {
                FightHandler.pveKill(killer, (Mob) victim);
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
        Player killer = victim.getKiller();

        if (killer == null) {
            return;
        }
        if (!WorldFights.isFightEnabledInWorld(victim.getWorld())) {
            return;
        }
        if (killer.hasPermission("karma.immune")) {
            return;
        }

        if (killer != victim) {
            if (FightHandler.isFakePlayer(killer) || FightHandler.isFakePlayer(victim)) {
                killer.sendMessage("fake players are not compatible rn");
                victim.sendMessage("fake players are not compatible rn");
                return;
            }
            PlayerKillPlayerEvent pvpKillEvent = new PlayerKillPlayerEvent(killer, victim);
            Bukkit.getPluginManager().callEvent(pvpKillEvent);

            //Commands from pvp kill commands where guarantee is TRUE
            PvpCommandHandler.getPvpCommandHandler().handle(pvpKillEvent.getAttacker(), pvpKillEvent.getVictim(), true);

            if (!pvpKillEvent.isCancelled()) {
                FightHandler.pvpKill(killer, victim);
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
        Player attacker;

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        victimEntity = (LivingEntity) event.getEntity();

        if (victimEntity.getHealth() - event.getFinalDamage() <= 0f) {
            return;
        }

        if (!WorldFights.isFightEnabledInWorld(victimEntity.getWorld())) {
            return;
        }

        attacker = getPlayerDamager(event);

        if (attacker == null || FightHandler.isFakePlayer(attacker)) {
            return;
        }

        if (attacker.hasPermission("karma.immune")) {
            return;
        }

        if (victimEntity instanceof Player && !attacker.equals(victimEntity)) {
            PlayerDamagePlayerEvent pvpDamageEvent = new PlayerDamagePlayerEvent(attacker, (Player) victimEntity);
            Bukkit.getPluginManager().callEvent(pvpDamageEvent);

            if (!pvpDamageEvent.isCancelled()) {
                FightHandler.pvpHit(attacker, (Player) victimEntity);
            }
        } else if (victimEntity instanceof Mob) {
            PlayerDamageMobEvent pveDamageEvent = new PlayerDamageMobEvent(attacker, (Mob) victimEntity);
            Bukkit.getPluginManager().callEvent(pveDamageEvent);

            if (!pveDamageEvent.isCancelled()) {
                FightHandler.pveHit(attacker, (Mob) victimEntity);
            }
        }
    }

    private Player getPlayerDamager(EntityDamageByEntityEvent event) {
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

    @EventHandler(ignoreCancelled = true)
    public void OnPlayerPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        Block placedBlock = event.getBlockPlaced();
        String blockName = placedBlock.getBlockData().getMaterial().name();
        ConfigurationSection section = ConfigData.getConfigData().config.getConfigurationSection("blocks.list." + blockName + ".place");
        if (section == null) {
            return;
        }

        boolean ageBlackList = section.getBoolean("data.age.blacklist", false);
        List<Integer> ages = section.getIntegerList("data.age.ages");
        if (!ages.isEmpty() && placedBlock.getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) placedBlock.getBlockData();
            if (ageBlackList) {
                if (ages.contains(ageable.getAge())) {
                    return;
                }
            } else {
                if (!ages.contains(ageable.getAge())) {
                    return;
                }
            }
        }

        float karma = model.getKarma() + (float) section.getDouble("value");
        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, model, karma);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);


        if (section.getBoolean("reset-overtime", false)) {
            PlayerOverTimeResetEvent playerOverTimeResetEvent = new PlayerOverTimeResetEvent(player, "all");
            Bukkit.getPluginManager().callEvent(playerOverTimeResetEvent);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void OnPlayerBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        Block brokenBlock = event.getBlock();
        String blockName = brokenBlock.getBlockData().getMaterial().name();
        ConfigurationSection section = ConfigData.getConfigData().config.getConfigurationSection("blocks.list." + blockName + ".break");
        if (section == null) {
            return;
        }

        boolean ageBlackList = section.getBoolean("data.age.blacklist", false);
        List<Integer> ages = section.getIntegerList("data.age.ages");
        if (!ages.isEmpty() && brokenBlock.getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) brokenBlock.getBlockData();
            if (ageBlackList) {
                if (ages.contains(ageable.getAge())) {
                    return;
                }
            } else {
                if (!ages.contains(ageable.getAge())) {
                    return;
                }
            }
        }

        float karma = model.getKarma() + (float) section.getDouble("value");
        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, model, karma);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);

        if (section.getBoolean("reset-overtime", false)) {
            PlayerOverTimeResetEvent playerOverTimeResetEvent = new PlayerOverTimeResetEvent(player, "all");
            Bukkit.getPluginManager().callEvent(playerOverTimeResetEvent);
        }

    }
}
