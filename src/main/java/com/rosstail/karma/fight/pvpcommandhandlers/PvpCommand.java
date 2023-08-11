package com.rosstail.karma.fight.pvpcommandhandlers;

import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.tiers.TierManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class PvpCommand {

    private final String name;
    private final List<String> attackerTierListRequirement = new ArrayList<>();
    private final List<String> victimTierListRequirement = new ArrayList<>();

    private final boolean attackerMinimumKarmaRequirement;
    private final float attackerMinimumKarma;
    private final boolean attackerMaximumKarmaRequirement;
    private final float attackerMaximumKarma;

    private final boolean victimMinimumKarmaRequirement;
    private final float victimMinimumKarma;
    private final boolean victimMaximumKarmaRequirement;
    private final float victimMaximumKarma;


    private final String attackerStatusRequirement;
    private final String victimStatusRequirement;
    private final List<String> commands = new ArrayList<>();
    private final boolean guarantee;

    PvpCommand(ConfigurationSection section) {
        name = section.getName();
        attackerTierListRequirement.addAll(section.getStringList("requirements.attacker-tier"));
        victimTierListRequirement.addAll(section.getStringList("requirements.victim-tier"));

        attackerMinimumKarmaRequirement = section.getString("requirements.attacker-minimum-karma") != null;
        attackerMinimumKarma = (float) section.getDouble("requirements.attacker-minimum-karma");
        attackerMaximumKarmaRequirement = section.getString("requirements.attacker-maximum-karma") != null;
        attackerMaximumKarma = (float) section.getDouble("requirements.attacker-maximum-karma");

        victimMinimumKarmaRequirement = section.getString("requirements.victim-minimum-karma") != null;
        victimMinimumKarma = (float) section.getDouble("requirements.victim-minimum-karma");
        victimMaximumKarmaRequirement = section.getString("requirements.victim-maximum-karma") != null;
        victimMaximumKarma = (float) section.getDouble("requirements.victim-maximum-karma");


        attackerStatusRequirement = section.getString("requirements.attacker-status");
        victimStatusRequirement = section.getString("requirements.victim-status");
        guarantee = section.getBoolean("guarantee", true);
        commands.addAll(section.getStringList("commands"));
    }

    public String getName() {
        return name;
    }

    public List<String> getAttackerTierListRequirement() {
        return attackerTierListRequirement;
    }

    public List<String> getVictimTierListRequirement() {
        return victimTierListRequirement;
    }

    public boolean isAttackerMinimumKarmaRequirement() {
        return attackerMinimumKarmaRequirement;
    }

    public float getAttackerMinimumKarma() {
        return attackerMinimumKarma;
    }

    public boolean isAttackerMaximumKarmaRequirement() {
        return attackerMaximumKarmaRequirement;
    }

    public float getAttackerMaximumKarma() {
        return attackerMaximumKarma;
    }

    public boolean isVictimMinimumKarmaRequirement() {
        return victimMinimumKarmaRequirement;
    }

    public float getVictimMinimumKarma() {
        return victimMinimumKarma;
    }

    public boolean isVictimMaximumKarmaRequirement() {
        return victimMaximumKarmaRequirement;
    }

    public float getVictimMaximumKarma() {
        return victimMaximumKarma;
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
