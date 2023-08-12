package com.rosstail.karma.fight.teamfighthandlers;

import com.rosstail.karma.ConfigData;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreboardTeamFightHandler implements TeamFightHandler {
    String regex = "\\D+"; //matcher to avoid counting scoreboard on screen
    Pattern pattern = Pattern.compile(regex);

    private boolean cancelOnSameTeam;
    private boolean cancelOnOtherTeam;

    @Override
    public void init() {
        ConfigData configData = ConfigData.getConfigData();
        cancelOnSameTeam = configData.pvp.scoreboardTeamSystemCancelSameTeam;
        cancelOnOtherTeam = configData.pvp.scoreboardTeamSystemCancelOtherTeam;
    }

    @Override
    public boolean doTeamFightCancel(Player attacker, Player victim) {
        List<Team> attackerTeamList = new ArrayList<>(attacker.getScoreboard().getTeams());
        List<Team> victimTeamList = new ArrayList<>(attacker.getScoreboard().getTeams());

        List<Team> cloneAttackerTeamList = new ArrayList<>(attackerTeamList);
        cloneAttackerTeamList.retainAll(victimTeamList);
        if (cancelOnSameTeam && cloneAttackerTeamList.size() > 0) {
            for (Team team : cloneAttackerTeamList) {
                Matcher matcher = pattern.matcher(team.getName());
                if (matcher.find()) {
                    return true;
                }
            }
        } else return cancelOnOtherTeam && cloneAttackerTeamList.size() == 0;

        return false;
    }
}
