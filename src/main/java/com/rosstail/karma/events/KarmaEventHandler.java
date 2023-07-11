package com.rosstail.karma.events;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.events.karmaevents.*;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.overtime.OvertimeLoop;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.sql.Timestamp;
import java.util.List;

public class KarmaEventHandler implements Listener {
    private final AdaptMessage adaptMessage;

    public KarmaEventHandler() {
        this.adaptMessage = AdaptMessage.getAdaptMessage();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerKarmaChange(PlayerKarmaChangeEvent event) {
        PlayerModel model = event.getModel();
        model.setPreviousKarma(model.getKarma());
        model.setKarma(event.getValue());

        /*
            CHECK PLAYER TIER
             */
        TierManager tierManager = TierManager.getTierManager();
        Tier currentKarmaTier = tierManager.getTierByKarmaAmount(model.getKarma());
        Tier modelTier = tierManager.getTierByName(model.getTierName());
        if (!currentKarmaTier.equals(modelTier)) {
            PlayerTierChangeEvent tierChangeEvent = new PlayerTierChangeEvent(event.getPlayer(), model, currentKarmaTier.getName());
            Bukkit.getPluginManager().callEvent(tierChangeEvent);
        }
    }

    @EventHandler
    public void onPlayerTierChange(PlayerTierChangeEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = event.getModel();
        model.setPreviousTierName(model.getTierName());
        model.setTierName(event.getTierName());

        float karma = model.getKarma();
        float previousKarma = model.getPreviousKarma();

        Tier tier = TierManager.getTierManager().getTierByName(model.getTierName());
        Tier previousTier = TierManager.getTierManager().getTierByName(model.getPreviousTierName());

        PlayerDataManager.changePlayerTierMessage(player);
        CommandManager.commandsLauncher(player, tier.getJoinCommands());

        if (!previousTier.equals(TierManager.getNoTier())) {
            if (karma > previousKarma) {
                CommandManager.commandsLauncher(player, tier.getJoinOnUpCommands());
            } else if (karma < previousKarma) {
                CommandManager.commandsLauncher(player, tier.getJoinOnDownCommands());
            }
        }
    }

    /*
    @EventHandler
    public void onPlayerKarmaHasChanged(PlayerKarmaHasChangedEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = event.getModel();

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
     */

    @EventHandler(ignoreCancelled = true)
    public void onPlayerOverTimeTriggerEvent(PlayerOverTimeTriggerEvent event) {
        System.out.println("trigger " + event.getPlayer().getName() + " " + event.getOvertimeLoopName());
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        long nextDelay = event.getNextDelay();
        PlayerDataManager.triggerOverTime(player, model, event.getOvertimeLoopName(), event.getAmount());
        PlayerDataManager.setOverTimeStamp(model, event.getOvertimeLoopName(), nextDelay);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerOverTimeResetEvent(PlayerOverTimeResetEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        OvertimeLoop loop = ConfigData.getConfigData().overtimeLoopMap.get(event.getOvertimeLoopName());
        PlayerDataManager.setOverTimeStamp(model, event.getOvertimeLoopName(), (int) loop.firstTimer);
    }

    @EventHandler
    public void onPlayerWantedChangeEvent(PlayerWantedChangeEvent event) {
        PlayerModel model = event.getModel();
        Timestamp duration = event.getTimestamp();

        model.setWantedTimeStamp(duration);

        boolean isWanted = model.isWanted();
        boolean willBeWanted = PlayerDataManager.isWanted(model);

        if (!isWanted && willBeWanted) {
            Bukkit.getPluginManager().callEvent(new PlayerWantedPeriodStartEvent(event.getPlayer(), model));
        } else if (isWanted && willBeWanted) { //stay
            Bukkit.getPluginManager().callEvent(new PlayerWantedPeriodRefreshEvent(event.getPlayer(), model));
        } else if (isWanted) {
            Bukkit.getPluginManager().callEvent(new PlayerWantedPeriodEndEvent(event.getPlayer(), model));
        }
    }

    @EventHandler
    public void onPlayerWantedPeriodStartEvent(PlayerWantedPeriodStartEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        String message = LangManager.getMessage(LangMessage.WANTED_ENTER);

        CommandManager.commandsLauncher(player, ConfigData.getConfigData().enterWantedCommands);
        model.setWanted(true);
        if (message != null) {
            adaptMessage.sendToPlayer(player, AdaptMessage.getAdaptMessage().adapt(player, message, PlayerType.PLAYER.getText()));
        }
    }

    @EventHandler
    public void onPlayerWantedPeriodRefreshEvent(PlayerWantedPeriodRefreshEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        String message = LangManager.getMessage(LangMessage.WANTED_REFRESH);
        model.setWanted(true);
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
        PlayerModel model= event.getModel();

        model.setWanted(false);
        String message = LangManager.getMessage(LangMessage.WANTED_EXIT);
        CommandManager.commandsLauncher(player, ConfigData.getConfigData().leaveWantedCommands);
        if (message != null) {
            AdaptMessage.getAdaptMessage().sendToPlayer(player, AdaptMessage.getAdaptMessage().adapt(player, message, PlayerType.PLAYER.getText()));
        }
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

        float karma =  model.getKarma() + (float) section.getDouble("value");
        PlayerKarmaChangeEvent playerKarmaChangeEvent = new PlayerKarmaChangeEvent(player, model, karma);
        Bukkit.getPluginManager().callEvent(playerKarmaChangeEvent);

        if (section.getBoolean("reset-overtime", false)) {
            PlayerOverTimeResetEvent playerOverTimeResetEvent = new PlayerOverTimeResetEvent(player, "all");
            Bukkit.getPluginManager().callEvent(playerOverTimeResetEvent);
        }

    }
}
