package com.rosstail.karma.events;

import com.rosstail.karma.configdata.ConfigData;
import com.rosstail.karma.customevents.*;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.tiers.Tier;
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

public class CustomEventHandler implements Listener {
    private static ConfigData configData = ConfigData.getConfigData();

    public CustomEventHandler() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerKarmaChange(PlayerKarmaChangeEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getPlayerList().get(player);

        playerData.setKarma(event.getValue());

        if (event.isOverTimeReset()) {
            PlayerData.setOverTimerChange(player);
        }
        PlayerKarmaHasChangedEvent playerKarmaHasChangedEvent = new PlayerKarmaHasChangedEvent(player, playerData.getKarma(), event.isOverTimeReset(), event);
        Bukkit.getPluginManager().callEvent(playerKarmaHasChangedEvent);
    }

    @EventHandler
    public void onPlayerKarmaHasChanged(PlayerKarmaHasChangedEvent event) {
        PlayerData playerData = PlayerData.getPlayerList().get(event.getPlayer());
        playerData.checkTier();
    }

    @EventHandler
    public void onPlayerTierChange(PlayerTierChangeEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getPlayerList().get(player);
        playerData.setPreviousTier(playerData.getTier());
        playerData.setTier(event.getTier());

        PlayerTierHasChangedEvent playerTierHasChangedEvent = new PlayerTierHasChangedEvent(event.getPlayer(), event.getTier());
        Bukkit.getPluginManager().callEvent(playerTierHasChangedEvent);
    }

    @EventHandler
    public void onPlayerTierHasChanged(PlayerTierHasChangedEvent event) {
        Player player = event.getPlayer();
        Tier tier = event.getTier();
        PlayerData playerData = PlayerData.getPlayerList().get(player);
        Tier previousTier = playerData.getPreviousTier();
        double karma = playerData.getKarma();
        double previousKarma = playerData.getPreviousKarma();

        PlayerData.changePlayerTierMessage(player);
        PlayerData.commandsLauncher(player, tier.getJoinCommands());
        if (previousTier != null) {
            if (karma > previousKarma) {
                PlayerData.commandsLauncher(player, tier.getJoinOnUpCommands());
            } else {
                PlayerData.commandsLauncher(player, tier.getJoinOnDownCommands());
            }
        }
    }

    @EventHandler
    public void onPlayerOverTimeTriggerEvent(PlayerOverTimeTriggerEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            PlayerData.triggerOverTime(player);
            PlayerOverTimeHasTriggeredEvent hasTriggeredEvent = new PlayerOverTimeHasTriggeredEvent(player);
            Bukkit.getPluginManager().callEvent(hasTriggeredEvent);
        }
    }

    @EventHandler
    public void onPlayerOverTimeHasTriggeredEvent(PlayerOverTimeHasTriggeredEvent event) {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerOverTimeHighestResetEvent(PlayerOverTimeResetEvent event) {
        event.setTriggerID(PlayerData.gets(event.getPlayer()).getOverTimerScheduler());
    }

    @EventHandler
    public void onPlayerOverTimeResetEvent(PlayerOverTimeResetEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            PlayerData playerData = PlayerData.gets(player);
            PlayerData.setOverTimerChange(player);

            PlayerOverTimeHasResetEvent hasResetEvent = new PlayerOverTimeHasResetEvent(player, playerData.getOverTimerScheduler());
            Bukkit.getPluginManager().callEvent(hasResetEvent);
        }
    }

    @EventHandler
    public void onPlayerOverTimeHasResetEvent(PlayerOverTimeHasResetEvent event) {
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int delay = configData.getSaveDelay();
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.gets(player);
        playerData.initPlayerData();
        playerData.setUpdateDataTimer(delay);
        PlayerData.setOverTimerChange(player);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.gets(player);
        playerData.getUpdateDataTimer().cancel();
        playerData.updateData();
        PlayerData.stopOverTimer(player);
    }

    /**
     * Check and apply karma when a non-pLayer livingEntity is killed by a Player
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Player killer = victim.getKiller();
        if (!WorldFights.isFightEnabledInWorld(victim.getWorld())) {
            return;
        }

        if (!(killer == null || Fights.isPlayerNPC(killer))) {
            Fights.pveHandler(killer, victim, Reasons.KILL, event);
        }
    }

    /**
     * Apply a new karma to the Player KILLER when he kills another player
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!WorldFights.isFightEnabledInWorld(victim.getWorld())) {
            return;
        }
        Player killer = victim.getKiller();
        if (!(killer == null || killer == victim || Fights.isPlayerNPC(killer))) {
            Fights.pvpHandler(killer, victim, Reasons.KILL, event);
        }
    }

    /**
     * Changes karma when player attack another entity (animal or monster)
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityHurt(EntityDamageByEntityEvent event) {
        LivingEntity victimEntity;

        double damage = event.getFinalDamage();
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity && damage >= 1d && ((LivingEntity) entity).getHealth() - damage > 0)) {
            return;
        }

        victimEntity = (LivingEntity) event.getEntity();
        if (!WorldFights.isFightEnabledInWorld(victimEntity.getWorld())) {
            return;
        }

        Player attacker = getFightAttacker(event);
        if (attacker != null) {
            if (Fights.isPlayerNPC(attacker) || !TimeManager.getTimeManager().isPlayerInTime(attacker)) {
                return;
            }

            if (victimEntity instanceof Player && attacker != victimEntity) {
                Fights.pvpHandler(attacker, (Player) victimEntity, Reasons.HIT, event);
            } else {
                Fights.pveHandler(attacker, victimEntity, Reasons.HIT, event);
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

    public static void setConfigData(ConfigData configData) {
        CustomEventHandler.configData = configData;
    }
}
