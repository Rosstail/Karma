package com.rosstail.karma.events;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.apis.WGPreps;
import com.rosstail.karma.blocks.BlocksManager;
import com.rosstail.karma.events.karmaevents.*;
import com.rosstail.karma.players.PlayerDataManager;
import com.rosstail.karma.players.PlayerDataModel;
import com.rosstail.karma.storage.StorageManager;
import com.rosstail.karma.events.karmaevents.karmafightevents.PlayerDamageMobEvent;
import com.rosstail.karma.events.karmaevents.karmafightevents.PlayerDamagePlayerEvent;
import com.rosstail.karma.events.karmaevents.karmafightevents.PlayerKillMobEvent;
import com.rosstail.karma.events.karmaevents.karmafightevents.PlayerKillPlayerEvent;
import com.rosstail.karma.fight.FightHandler;
import com.rosstail.karma.fight.WorldFights;
import com.rosstail.karma.fight.pvpcommandhandlers.PvpCommandHandler;
import com.rosstail.karma.overtime.OvertimeLoop;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

public class MinecraftEventHandler implements Listener {

    private boolean isClosing = false;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerDataModel model = StorageManager.getManager().selectPlayerModel(
                Bukkit.getOnlineMode() ? event.getPlayer().getUniqueId().toString() : player.getName()
        );
        if (model == null) {
            model = new PlayerDataModel(event.getPlayer());
            StorageManager.getManager().uploadPlayerModel(model);
            PlayerDataManager.initPlayerModelToMap(model);

            //Event join tier by default
            PlayerTierChangeEvent defaultTierJoinEvent = new PlayerTierChangeEvent(event.getPlayer(), model, TierManager.getTierManager().getTierByKarmaAmount(model.getKarma()).getName(), !ConfigData.getConfigData().pvp.sendMessageOnTierChange);
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

                for (Map.Entry<String, OvertimeLoop> stringOvertimeLoopEntry : ConfigData.getConfigData().overtime.overtimeLoopMap.entrySet()) {
                    String overtimeName = stringOvertimeLoopEntry.getKey();
                    OvertimeLoop overtimeLoop = stringOvertimeLoopEntry.getValue();

                    long loopDelta = deltaUpdates - overtimeLoop.firstTimer;
                    long delay = overtimeLoop.firstTimer;

                    if (overtimeLoop.offline) {
                        if (loopDelta >= 0) { //If first occurrence happened
                            int occurrences = 1 + (int) (Math.floorDiv(loopDelta, overtimeLoop.nextTimer));
                            delay = loopDelta % overtimeLoop.nextTimer;

                            if (occurrences > 0) {
                                Bukkit.getPluginManager().callEvent(new PlayerOverTimeTriggerEvent(player, overtimeName, occurrences, delay));
                            }
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
                PlayerTierChangeEvent tierChangeEvent = new PlayerTierChangeEvent(player, model, currentKarmaTier.getName(), !ConfigData.getConfigData().pvp.sendMessageOnTierChange);
                Bukkit.getPluginManager().callEvent(tierChangeEvent);
            }

            /*
            CHECK PLAYER WANTED STATUS
             */
            if (ConfigData.getConfigData().wanted.wantedEnable) {
                if (model.isWanted()) {
                    if (PlayerDataManager.getWantedTimeLeft(model) > 0L) {
                        PlayerWantedPeriodRefreshEvent playerWantedPeriodRefreshEvent = new PlayerWantedPeriodRefreshEvent(player, model, !ConfigData.getConfigData().pvp.sendMessageOnWantedChange);
                        Bukkit.getPluginManager().callEvent(playerWantedPeriodRefreshEvent);
                    } else {
                        PlayerWantedPeriodEndEvent playerWantedPeriodEndEvent = new PlayerWantedPeriodEndEvent(player, model, !ConfigData.getConfigData().pvp.sendMessageOnWantedChange);
                        Bukkit.getPluginManager().callEvent(playerWantedPeriodEndEvent);
                    }
                } else if (PlayerDataManager.getWantedTimeLeft(model) > 0L) {
                    PlayerWantedPeriodStartEvent playerWantedPeriodStartEvent = new PlayerWantedPeriodStartEvent(player, model, !ConfigData.getConfigData().pvp.sendMessageOnWantedChange);
                    Bukkit.getPluginManager().callEvent(playerWantedPeriodStartEvent);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerDataModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        if (!isClosing) {
            StorageManager.getManager().asyncUploadPlayerModel(model);
            PlayerDataManager.removePlayerModelFromMap(player);
        }
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
        PlayerDataModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        Block placedBlock = event.getBlockPlaced();

        if (!WGPreps.getWgPreps().checkBlockPlaceChangeKarmaFlag(player, placedBlock.getLocation())) {
            return;
        }
        BlocksManager.getBlocksManager().placeHandler(player, model, placedBlock);
    }


    @EventHandler(ignoreCancelled = true)
    public void OnPlayerBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerDataModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        Block brokenBlock = event.getBlock();

        if (!WGPreps.getWgPreps().checkBlockBreakChangeKarmaFlag(player, brokenBlock.getLocation())) {
            return;
        }
        BlocksManager.getBlocksManager().breakHandler(player, model, brokenBlock);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJumpOnRootsEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            Player player = event.getPlayer();
            PlayerDataModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
            Block brokenBlock = event.getClickedBlock();

            if (brokenBlock != null) {
                if (!WGPreps.getWgPreps().checkBlockBreakChangeKarmaFlag(player, brokenBlock.getLocation())) {
                    return;
                }
                BlocksManager.getBlocksManager().breakHandler(player, model, brokenBlock);
                Block trampledBlock = player.getWorld().getBlockAt(brokenBlock.getLocation().add(0, 1, 0));
                if (!trampledBlock.getType().isAir()) {
                    BlocksManager.getBlocksManager().breakHandler(player, model, trampledBlock);
                }
            }
        }
    }

    public boolean isClosing() {
        return isClosing;
    }

    public void setClosing(boolean closing) {
        isClosing = closing;
    }
}
