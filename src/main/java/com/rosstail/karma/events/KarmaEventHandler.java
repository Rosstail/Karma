package com.rosstail.karma.events;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.apis.WGPreps;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.customevents.*;
import com.rosstail.karma.datas.PlayerData;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.overtime.OvertimeLoop;
import com.rosstail.karma.tiers.Tier;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.sql.Timestamp;
import java.util.List;

public class KarmaEventHandler implements Listener {
    private final AdaptMessage adaptMessage;

    public KarmaEventHandler() {
        this.adaptMessage = AdaptMessage.getAdaptMessage();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerKarmaChange(PlayerKarmaChangeEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);

        playerData.setKarmaBetweenLimits(event.getValue());
        if (event.isOverTimeReset()) {
            ConfigData.getConfigData().overtimeLoopMap.forEach((s, overtimeLoop) -> {
                playerData.setOverTimeStamp(s, overtimeLoop.firstTimer);
            });
        }

        PlayerKarmaHasChangedEvent playerKarmaHasChangedEvent = new PlayerKarmaHasChangedEvent(player, playerData.getKarma(), event.isOverTimeReset(), event);
        Bukkit.getPluginManager().callEvent(playerKarmaHasChangedEvent);
    }

    @EventHandler
    public void onPlayerKarmaHasChanged(PlayerKarmaHasChangedEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);

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
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);
        playerData.setPreviousTier(playerData.getTier());
        playerData.setTier(event.getTier());

        PlayerTierHasChangedEvent playerTierHasChangedEvent = new PlayerTierHasChangedEvent(event.getPlayer(), event.getTier());
        Bukkit.getPluginManager().callEvent(playerTierHasChangedEvent);
    }

    @EventHandler
    public void onPlayerTierHasChanged(PlayerTierHasChangedEvent event) {
        Player player = event.getPlayer();
        Tier tier = event.getTier();
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);
        Tier previousTier = playerData.getPreviousTier();
        double karma = playerData.getKarma();
        double previousKarma = playerData.getPreviousKarma();

        PlayerDataManager.changePlayerTierMessage(player);
        CommandManager.commandsLauncher(player, tier.getJoinCommands());
        if (previousTier != null) {
            if (karma > previousKarma) {
                CommandManager.commandsLauncher(player, tier.getJoinOnUpCommands());
            } else if (karma < previousKarma) {
                CommandManager.commandsLauncher(player, tier.getJoinOnDownCommands());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerOverTimeTriggerEvent(PlayerOverTimeTriggerEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);
        long nextDelay = event.getNextDelay();
        PlayerDataManager.triggerOverTime(playerData, event.getOvertimeLoopName(), event.getAmount());
        playerData.setOverTimeStamp(event.getOvertimeLoopName(), nextDelay);
        PlayerOverTimeHasTriggeredEvent hasTriggeredEvent = new PlayerOverTimeHasTriggeredEvent(player);
        Bukkit.getPluginManager().callEvent(hasTriggeredEvent);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerOverTimeResetEvent(PlayerOverTimeResetEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);
        OvertimeLoop loop = ConfigData.getConfigData().overtimeLoopMap.get(event.getOvertimeLoopName());
        playerData.setOverTimeStamp(event.getOvertimeLoopName(), loop.firstTimer);
        PlayerOverTimeHasResetEvent hasResetEvent = new PlayerOverTimeHasResetEvent(player);
        Bukkit.getPluginManager().callEvent(hasResetEvent);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerWantedChangeEvent(PlayerWantedChangeEvent event) {
        Player player = event.getPlayer();
        Timestamp duration = event.getTimestamp();
        String wantedMaxDurationExp = ConfigData.getConfigData().wantedMaxDurationExpression;
        Timestamp durationMaxTimeStamp = new Timestamp(AdaptMessage.calculateDuration(player, wantedMaxDurationExp));
        if (duration.compareTo(durationMaxTimeStamp) > 0) {
            duration = durationMaxTimeStamp;
        }
        PlayerWantedHasChangedEvent playerWantedHasChangedEvent = new PlayerWantedHasChangedEvent(player, duration, event);
        Bukkit.getPluginManager().callEvent(playerWantedHasChangedEvent);
    }

    @EventHandler
    public void onPlayerWantedHasChangedEvent(PlayerWantedHasChangedEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);
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
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);
        String message = LangManager.getMessage(LangMessage.WANTED_ENTER);

        CommandManager.commandsLauncher(player, ConfigData.getConfigData().enterWantedCommands);
        playerData.setWantedToken(true);
        if (message != null) {
            adaptMessage.sendToPlayer(player, AdaptMessage.getAdaptMessage().adapt(player, message, PlayerType.PLAYER.getText()));
        }
    }

    @EventHandler
    public void onPlayerWantedPeriodRefreshEvent(PlayerWantedPeriodRefreshEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);
        String message = LangManager.getMessage(LangMessage.WANTED_REFRESH);
        playerData.setWantedToken(true);
        CommandManager.commandsLauncher(player, ConfigData.getConfigData().refreshWantedCommands);
        if (message != null) {
            AdaptMessage.getAdaptMessage().sendToPlayer(player, AdaptMessage.getAdaptMessage().adapt(player, message, PlayerType.PLAYER.getText()));
        }
    }

    @EventHandler
    public void onPlayerWantedPunishEvent(PlayerWantedPunishEvent event) {
        Player punishedPlayer = event.getPunishedPlayer();
        Player punisherPlayer = event.getPunisherPlayer();
        CommandManager.commandsLauncher(punisherPlayer, punishedPlayer, ConfigData.getConfigData().punishWantedCommands);
    }

    @EventHandler
    public void onPlayerWantedPeriodEndEvent(PlayerWantedPeriodEndEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerDataMap().get(player);

        playerData.setWantedToken(false);
        String message = LangManager.getMessage(LangMessage.WANTED_EXIT);
        CommandManager.commandsLauncher(player, ConfigData.getConfigData().leaveWantedCommands);
        if (message != null) {
            AdaptMessage.getAdaptMessage().sendToPlayer(player, AdaptMessage.getAdaptMessage().adapt(player, message, PlayerType.PLAYER.getText()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void OnPlayerPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getNoSet(player);
        Block placedBlock = event.getBlockPlaced();
        String blockName = placedBlock.getBlockData().getMaterial().name();
        ConfigurationSection section = ConfigData.getConfigData().config.getConfigurationSection("blocks.list." + blockName + ".place");
        if (section == null) {
            return;
        }

        boolean ageBlackList = section.getBoolean("data.age.blacklist", false);
        List<Integer> ages = section.getIntegerList("data.age.ages");
        if (!ages.isEmpty()) {
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

        float karma = (float) (playerData.getKarma() + section.getDouble("value"));
        boolean resetOvertime = section.getBoolean("reset-overtime", false);
        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player,
                karma,
                resetOvertime,
                Cause.OTHER);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
    }


    @EventHandler(ignoreCancelled = true)
    public void OnPlayerBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getNoSet(player);
        Block brokenBlock = event.getBlock();
        String blockName = brokenBlock.getBlockData().getMaterial().name();
        ConfigurationSection section = ConfigData.getConfigData().config.getConfigurationSection("blocks.list." + blockName + ".break");
        if (section == null) {
            return;
        }

        boolean ageBlackList = section.getBoolean("data.age.blacklist", false);
        List<Integer> ages = section.getIntegerList("data.age.ages");
        if (!ages.isEmpty()) {
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

        float karma = (float) (playerData.getKarma() + section.getDouble("value"));
        boolean resetOvertime = section.getBoolean("reset-overtime", false);
        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player,
                karma,
                resetOvertime,
                Cause.OTHER);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);
    }
}
