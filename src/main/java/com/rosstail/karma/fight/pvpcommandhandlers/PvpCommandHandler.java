package com.rosstail.karma.fight.pvpcommandhandlers;

import com.rosstail.karma.Karma;
import com.rosstail.karma.commands.CommandManager;
import com.rosstail.karma.datas.PlayerDataManager;
import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PvpCommandHandler {

    private static PvpCommandHandler pvpCommandHandler;
    private final Karma plugin;
    private final List<PvpCommand> pvpCommandList = new ArrayList<>();

    public static void init(Karma plugin) {
        if (pvpCommandHandler == null) {
            pvpCommandHandler = new PvpCommandHandler(plugin);
        }
    }
    PvpCommandHandler(Karma plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        plugin.getCustomConfig().getConfigurationSection("pvp.commands.kill-commands").getKeys(false).forEach(s -> {
            pvpCommandList.add(new PvpCommand(plugin.getCustomConfig().getConfigurationSection("pvp.commands.kill-commands." + s)));
        });
    }

    public static PvpCommandHandler getPvpCommandHandler() {
        return pvpCommandHandler;
    }

    public List<PvpCommand> getPvpCommandList() {
        return pvpCommandList;
    }

    public void handle(Player attacker, Player victim, boolean guarantee) {
        PlayerModel attackerModel = PlayerDataManager.getPlayerModelMap().get(attacker.getName());
        PlayerModel victimModel = PlayerDataManager.getPlayerModelMap().get(victim.getName());
        pvpCommandList.forEach(pvpCommand -> {
            if (checkRequirement(attackerModel, victimModel, pvpCommand, guarantee)) {
                CommandManager.commandsLauncher(attacker, victim, pvpCommand.getCommands());
            }
        });
    }

    public boolean checkRequirement(PlayerModel attackerModel, PlayerModel victimModel, PvpCommand pvpCommand, boolean guarantee) {
        if (pvpCommand.isGuarantee() != guarantee) {
            return false;
        }
        String attackerTierName = attackerModel.getTierName();
        String victimTierName = victimModel.getTierName();
        if (doesNotCheckTierListRequirement(attackerTierName, pvpCommand.getAttackerTierListRequirement())) {
            return false;
        }
        if (doesNotCheckTierListRequirement(victimTierName, pvpCommand.getVictimTierListRequirement())) {
            return false;
        }

        String attackerStatusRequirement = pvpCommand.getAttackerStatusRequirement();
        if (attackerStatusRequirement != null) {
            if (attackerModel.isWanted() && attackerStatusRequirement.equalsIgnoreCase("INNOCENT")) {
                return false;
            }
            if (!attackerModel.isWanted() && attackerStatusRequirement.equalsIgnoreCase("WANTED")) {
                return false;
            }
        }

        String victimStatusRequirement = pvpCommand.getVictimStatusRequirement();
        if (victimStatusRequirement != null) {
            if (victimModel.isWanted() && victimStatusRequirement.equalsIgnoreCase("INNOCENT")) {
                return false;
            }
            if (!victimModel.isWanted() && victimStatusRequirement.equalsIgnoreCase("WANTED")) {
                return false;
            }
        }

        return true;
    }

    private boolean doesNotCheckTierListRequirement(String victimTierName, List<String> victimTierListRequirement) {
        if (!victimTierListRequirement.isEmpty()) {
            if (victimTierListRequirement.get(0).startsWith("!")) {
                List<String> tierListName = new ArrayList<>(TierManager.getTierManager().getTiers().keySet());
                for (String s : victimTierListRequirement) {
                    if (s.startsWith("!") && tierListName.contains(s.replace("!", ""))) {
                        return true;
                    }
                }
            } else return !victimTierListRequirement.contains(victimTierName);

        }
        return false;
    }

}
