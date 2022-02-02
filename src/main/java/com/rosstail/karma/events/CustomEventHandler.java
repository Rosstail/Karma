package com.rosstail.karma.events;

import com.rosstail.karma.Karma;
import com.rosstail.karma.apis.ExpressionCalculator;
import com.rosstail.karma.apis.WGPreps;
import com.rosstail.karma.commands.KarmaCommand;
import com.rosstail.karma.ConfigData;
import com.rosstail.karma.customevents.*;
import com.rosstail.karma.datas.DBInteractions;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.timemanagement.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;

public class CustomEventHandler implements Listener {
    private static ConfigData configData = ConfigData.getConfigData();
    private final AdaptMessage adaptMessage;
    private final Karma plugin = Karma.getInstance();

    public CustomEventHandler() {
        this.adaptMessage = AdaptMessage.getAdaptMessage();
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
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.getPlayerList().get(player);

        Object cause = event.getCause().getCause();
        if (cause instanceof EntityDamageByEntityEvent || cause instanceof PlayerDeathEvent) {
            if (((EntityEvent) cause).getEntity() instanceof Player) {
                double newKarma = event.getValue();
                double previousKarma = playerData.getPreviousKarma();

                String message;
                if (newKarma > previousKarma) {
                    if (cause instanceof EntityDamageByEntityEvent) {
                        message = LangManager.getMessage(LangMessage.PVP_HIT_KARMA_INCREASE);
                    } else {
                        message = LangManager.getMessage(LangMessage.PVP_KILL_KARMA_INCREASE);
                    }
                } else if (newKarma < previousKarma) {
                    if (cause instanceof EntityDamageByEntityEvent) {
                        message = LangManager.getMessage(LangMessage.PVP_HIT_KARMA_DECREASE);
                    } else {
                        message = LangManager.getMessage(LangMessage.PVP_KILL_KARMA_DECREASE);
                    }
                } else {
                    if (cause instanceof EntityDamageByEntityEvent) {
                        message = LangManager.getMessage(LangMessage.PVP_HIT_KARMA_UNCHANGED);
                    } else {
                        message = LangManager.getMessage(LangMessage.PVP_KILL_KARMA_UNCHANGED);
                    }
                }
                if (message != null) {
                    Player victim;
                    if (cause instanceof EntityDamageByEntityEvent) {
                        victim = (Player) ((EntityDamageByEntityEvent) cause).getEntity();
                    } else {
                        victim = ((PlayerDeathEvent) cause).getEntity();
                    }

                    message = adaptMessage.playerHitAdapt(message, player, victim, cause);
                    adaptMessage.sendToPlayer(player, message);
                }
            }
        }


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
        KarmaCommand.commandsLauncher(player, tier.getJoinCommands());
        if (previousTier != null) {
            if (karma > previousKarma) {
                KarmaCommand.commandsLauncher(player, tier.getJoinOnUpCommands());
            } else {
                KarmaCommand.commandsLauncher(player, tier.getJoinOnDownCommands());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerOverTimeTriggerEvent(PlayerOverTimeTriggerEvent event) {
        Player player = event.getPlayer();
        PlayerData.triggerOverTime(player);
        PlayerOverTimeHasTriggeredEvent hasTriggeredEvent = new PlayerOverTimeHasTriggeredEvent(player);
        Bukkit.getPluginManager().callEvent(hasTriggeredEvent);
    }

    @EventHandler
    public void onPlayerOverTimeHasTriggeredEvent(PlayerOverTimeHasTriggeredEvent event) {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerOverTimeHighestResetEvent(PlayerOverTimeResetEvent event) {
        event.setTriggerID(PlayerData.gets(event.getPlayer()).getOverTimerScheduler());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerOverTimeResetEvent(PlayerOverTimeResetEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.gets(player);
        PlayerData.setOverTimerChange(player);

        PlayerOverTimeHasResetEvent hasResetEvent = new PlayerOverTimeHasResetEvent(player, playerData.getOverTimerScheduler());
        Bukkit.getPluginManager().callEvent(hasResetEvent);
    }

    @EventHandler
    public void onPlayerOverTimeHasResetEvent(PlayerOverTimeHasResetEvent event) {
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerWantedChangeEvent(PlayerWantedChangeEvent event) {
        Player player = event.getPlayer();
        Timestamp duration = event.getTimestamp();
        String wantedMaxDurationExp = configData.wantedMaxDurationExpression;
        Timestamp durationMaxTimeStamp = new Timestamp((long) ExpressionCalculator.eval(
                AdaptMessage.getAdaptMessage().adapt(player, wantedMaxDurationExp, PlayerType.PLAYER.getText())));
        if (duration.compareTo(durationMaxTimeStamp) > 0) {
            duration = durationMaxTimeStamp;
        }
        PlayerWantedHasChangedEvent playerWantedHasChangedEvent = new PlayerWantedHasChangedEvent(player, duration, event);
        Bukkit.getPluginManager().callEvent(playerWantedHasChangedEvent);
    }

    @EventHandler
    public void onPlayerWantedHasChangedEvent(PlayerWantedHasChangedEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.gets(player);
        playerData.setWantedTimeStamp(event.getTimestamp());
        boolean hasWantedToken = playerData.isWantedToken();
        boolean isWanted = playerData.isWanted();

        Event newEvent = null;

        if (isWanted && !hasWantedToken) {
            newEvent = new PlayerWantedPeriodStartEvent(player, event);
        } else if (isWanted) {
            newEvent = new PlayerWantedPeriodRefreshEvent(player, event, true);
        } else if (hasWantedToken) {
            newEvent = new PlayerWantedPeriodEndEvent(player, event);
        }
        if (newEvent != null) {
            Bukkit.getPluginManager().callEvent(newEvent);
        }
    }

    @EventHandler
    public void onPlayerWantedPeriodStartEvent(PlayerWantedPeriodStartEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.gets(player);
        String message = LangManager.getMessage(LangMessage.WANTED_ENTER);

        playerData.setWantedToken(true);
        PlayerData.replaceWantedScheduler(player);
        if (message != null) {
            adaptMessage.sendToPlayer(player, AdaptMessage.getAdaptMessage().adapt(player, message, PlayerType.PLAYER.getText()));
        }
    }

    @EventHandler
    public void onPlayerWantedPeriodRefreshEvent(PlayerWantedPeriodRefreshEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.gets(player);
        String message = LangManager.getMessage(LangMessage.WANTED_REFRESH);
        playerData.setWantedToken(true);
        PlayerData.replaceWantedScheduler(player);
        if (message != null) {
            AdaptMessage.getAdaptMessage().sendToPlayer(player, AdaptMessage.getAdaptMessage().adapt(player, message, PlayerType.PLAYER.getText()));
        }
    }

    @EventHandler
    public void onPlayerWantedPeriodEndEvent(PlayerWantedPeriodEndEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.gets(player);

        playerData.setWantedToken(false);
        PlayerData.stopTimer(playerData.getWantedScheduler());
        String message = LangManager.getMessage(LangMessage.WANTED_EXIT);
        if (message != null) {
            AdaptMessage.getAdaptMessage().sendToPlayer(player, AdaptMessage.getAdaptMessage().adapt(player, message, PlayerType.PLAYER.getText()));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.gets(player);
        playerData.initPlayerData();
        PlayerData.setOverTimerChange(player);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.gets(player);
        PlayerData.stopTimer(playerData.getOverTimerScheduler());
        PlayerData.stopTimer(playerData.getWantedScheduler());
        plugin.saveData(DBInteractions.reasons.DISCONNECT, Collections.singletonMap(player, playerData));
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

        if (!(killer == null || Fights.isPlayerNPC(killer))) {
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
        if (!(killer == null || killer == victim || Fights.isPlayerNPC(killer))) {
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
            if (Fights.isPlayerNPC(attacker) || !TimeManager.getTimeManager().isPlayerInTime(attacker)) {
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

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!ConfigData.getConfigData().useWorldGuard) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasMetadata("NPC")) {
            if (!WGPreps.getWgPreps().checkRequiredKarmaFlags(player)) {
                event.setCancelled(true);
                player.sendMessage("[TEST] Karma restricted access.");
            }
        }
    }

    public static void setConfigData(ConfigData configData) {
        CustomEventHandler.configData = configData;
    }
}
