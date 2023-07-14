package com.rosstail.karma.fight.teamfighthandlers;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreboardTeamFightHandler implements TeamFightHandler {
    String regex = "\\D+"; //matcher to avoid counting scoreboard on screen
    Pattern pattern = Pattern.compile(regex);

    @Override
    public boolean doTeamFightCancel(Player attacker, Player victim) {
        List<Team> attackerTeamList = new ArrayList<>(attacker.getScoreboard().getTeams());
        List<Team> victimTeamList = new ArrayList<>(attacker.getScoreboard().getTeams());

        List<Team> cloneAttackerTeamList = new ArrayList<>(attackerTeamList);
        cloneAttackerTeamList.retainAll(victimTeamList);
        if (cloneAttackerTeamList.size() > 0) {
            for (Team team : cloneAttackerTeamList) {
                Matcher matcher = pattern.matcher(team.getName());
                if (matcher.find()) {
                    attacker.sendMessage("Your karma is unchanged because " + victim.getName() + " is on the same team as you.");
                    attacker.sendMessage(" > " + team.getName() + " " + team.getDisplayName() + " " + team.getPrefix() + " / " + team.getSuffix());
                    return true;
                }
            }
        }

        return false;
    }
}
