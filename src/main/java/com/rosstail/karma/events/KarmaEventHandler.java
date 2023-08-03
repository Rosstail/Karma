package com.rosstail.karma.events;

import com.rosstail.karma.ConfigData;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.events.karmaevents.*;
import com.rosstail.karma.events.testevents.PlayerDamageMobEvent;
import com.rosstail.karma.events.testevents.PlayerDamagePlayerEvent;
import com.rosstail.karma.events.testevents.PlayerKillMobEvent;
import com.rosstail.karma.events.testevents.PlayerKillPlayerEvent;
import com.rosstail.karma.fight.FightHandler;
import com.rosstail.karma.fight.teamfighthandlers.TeamFightHandler;
import com.rosstail.karma.lang.AdaptMessage;
import com.rosstail.karma.lang.LangManager;
import com.rosstail.karma.lang.LangMessage;
import com.rosstail.karma.lang.PlayerType;
import com.rosstail.karma.overtime.OvertimeLoop;
import com.rosstail.karma.tiers.Tier;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Timestamp;

public class KarmaEventHandler implements Listener {
    private final AdaptMessage adaptMessage;

    public KarmaEventHandler() {
        this.adaptMessage = AdaptMessage.getAdaptMessage();
    }

    @EventHandler
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

    @EventHandler
    public void onPlayerOverTimeTriggerEvent(PlayerOverTimeTriggerEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        long nextDelay = event.getNextDelay();
        PlayerDataManager.triggerOverTime(player, model, event.getOvertimeLoopName(), event.getAmount());
        PlayerDataManager.setOverTimeStamp(model, event.getOvertimeLoopName(), nextDelay);
    }

    @EventHandler
    public void onPlayerOverTimeResetEvent(PlayerOverTimeResetEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        OvertimeLoop loop = ConfigData.getConfigData().overtime.overtimeLoopMap.get(event.getOvertimeLoopName());
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
        String message = LangManager.getMessage(LangMessage.WANTED_EVENT_ON_ENTER);

        CommandManager.commandsLauncher(player, ConfigData.getConfigData().wanted.enterWantedCommands);
        model.setWanted(true);
        if (message != null) {
            adaptMessage.sendToPlayer(player, adaptMessage.adaptMessage(
                    adaptMessage.adaptPlayerMessage(player, message, PlayerType.PLAYER.getText())
            ));
        }
    }

    @EventHandler
    public void onPlayerWantedPeriodRefreshEvent(PlayerWantedPeriodRefreshEvent event) {
        Player player = event.getPlayer();
        PlayerModel model = PlayerDataManager.getPlayerModelMap().get(player.getName());
        String message = LangManager.getMessage(LangMessage.WANTED_EVENT_ON_REFRESH);
        model.setWanted(true);
        CommandManager.commandsLauncher(player, ConfigData.getConfigData().wanted.refreshWantedCommands);
        if (message != null) {
            adaptMessage.sendToPlayer(player, adaptMessage.adaptMessage(
                    adaptMessage.adaptPlayerMessage(player, message, PlayerType.PLAYER.getText())
            ));
        }
    }

    @EventHandler
    public void onPlayerWantedPeriodEndEvent(PlayerWantedPeriodEndEvent event) {
        Player player = event.getPlayer();
        PlayerModel model= event.getModel();

        model.setWanted(false);
        String message = LangManager.getMessage(LangMessage.WANTED_EVENT_ON_EXIT);
        CommandManager.commandsLauncher(player, ConfigData.getConfigData().wanted.leaveWantedCommands);
        if (message != null) {
            adaptMessage.sendToPlayer(player, adaptMessage.adaptMessage(
                    adaptMessage.adaptPlayerMessage(player, message, PlayerType.PLAYER.getText())
                    ));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamagePlayerEvent(PlayerDamagePlayerEvent event) {
        for (TeamFightHandler teamFightHandler : FightHandler.getTeamFightHandlerList()) {
            if (teamFightHandler.doTeamFightCancel(event.getAttacker(), event.getVictim())) {
                event.setCancelled(true);
                break;
            }
        }

        Player attacker = event.getAttacker();
        Player victim = event.getVictim();
        PlayerModel attackerModel = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        float newKarma = attackerModel.getKarma();
        float previousKarma = attackerModel.getPreviousKarma();

        String message;
        if (newKarma > previousKarma) {
            message = LangManager.getMessage(LangMessage.FIGHT_PVP_HIT_ON_KARMA_GAIN);
        } else if (newKarma < previousKarma) {
            message = LangManager.getMessage(LangMessage.FIGHT_PVP_HIT_ON_KARMA_UNCHANGED);
        } else {
            message = LangManager.getMessage(LangMessage.FIGHT_PVP_HIT_ON_KARMA_LOSS);
        }
        attacker.sendMessage(AdaptMessage.getAdaptMessage().pvpHitMessage(message, attacker, victim));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerKillPlayerEvent(PlayerKillPlayerEvent event) {

        for (TeamFightHandler teamFightHandler : FightHandler.getTeamFightHandlerList()) {
            if (teamFightHandler.doTeamFightCancel(event.getAttacker(), event.getVictim())) {
                event.setCancelled(true);
                break;
            }
        }

        Player attacker = event.getAttacker();
        Player victim = event.getVictim();
        PlayerModel attackerModel = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        float newKarma = attackerModel.getKarma();
        float previousKarma = attackerModel.getPreviousKarma();

        String message;
        if (newKarma > previousKarma) {
            message = LangManager.getMessage(LangMessage.FIGHT_PVP_KILL_ON_KARMA_GAIN);
        } else if (newKarma < previousKarma) {
            message = LangManager.getMessage(LangMessage.FIGHT_PVP_KILL_ON_KARMA_UNCHANGED);
        } else {
            message = LangManager.getMessage(LangMessage.FIGHT_PVP_KILL_ON_KARMA_LOSS);
        }
        attacker.sendMessage(AdaptMessage.getAdaptMessage().pvpKillMessage(message, attacker, victim));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamageMobEvent(PlayerDamageMobEvent event) {
        Player attacker = event.getPlayer();
        PlayerModel attackerModel = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        float newKarma = attackerModel.getKarma();
        float previousKarma = attackerModel.getPreviousKarma();

        String message;
        if (newKarma > previousKarma) {
            message = LangManager.getMessage(LangMessage.FIGHT_PVE_HIT_ON_KARMA_GAIN);
        } else if (newKarma < previousKarma) {
            message = LangManager.getMessage(LangMessage.FIGHT_PVE_HIT_ON_KARMA_UNCHANGED);
        } else {
            message = LangManager.getMessage(LangMessage.FIGHT_PVE_HIT_ON_KARMA_LOSS);
        }
        AdaptMessage.getAdaptMessage().pveHitMessage(message, attacker, event.getMob());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerKillMobEvent(PlayerKillMobEvent event) {
        Player attacker = event.getPlayer();
        PlayerModel attackerModel = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        float newKarma = attackerModel.getKarma();
        float previousKarma = attackerModel.getPreviousKarma();

        String message;
        if (newKarma > previousKarma) {
            message = LangManager.getMessage(LangMessage.FIGHT_PVE_KILL_ON_KARMA_GAIN);
        } else if (newKarma < previousKarma) {
            message = LangManager.getMessage(LangMessage.FIGHT_PVE_KILL_ON_KARMA_UNCHANGED);
        } else {
            message = LangManager.getMessage(LangMessage.FIGHT_PVE_KILL_ON_KARMA_LOSS);
        }
        AdaptMessage.getAdaptMessage().pveKillMessage(message, attacker, event.getMob());
    }
}
