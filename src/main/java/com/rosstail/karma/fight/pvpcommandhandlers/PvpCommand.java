package com.rosstail.karma.fight.pvpcommandhandlers;

import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class PvpCommand {

    private final List<String> attackerTierListRequirement = new ArrayList<>();
    private final List<String> victimTierListRequirement = new ArrayList<>();

    private final String attackerStatusRequirement;
    private final String victimStatusRequirement;
    private final List<String> commands = new ArrayList<>();
    private final boolean guarantee;

    PvpCommand(ConfigurationSection section) {
        attackerTierListRequirement.addAll(section.getStringList("requirements.attacker-tier"));
        victimTierListRequirement.addAll(section.getStringList("requirements.victim-tier"));
        attackerStatusRequirement = section.getString("requirements.attacker-status");
        victimStatusRequirement = section.getString("requirements.victim-status");
        guarantee = section.getBoolean("guarantee", true);
        commands.addAll(section.getStringList("commands"));
    }

    public List<String> getAttackerTierListRequirement() {
        return attackerTierListRequirement;
    }

    public List<String> getVictimTierListRequirement() {
        return victimTierListRequirement;
    }

    public String getAttackerStatusRequirement() {
        return attackerStatusRequirement;
    }

    public String getVictimStatusRequirement() {
        return victimStatusRequirement;
    }

    public boolean isGuarantee() {
        return guarantee;
    }

    public List<String> getCommands() {
        return commands;
    }
}
