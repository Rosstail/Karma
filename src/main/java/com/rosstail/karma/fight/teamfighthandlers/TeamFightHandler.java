package com.rosstail.karma.fight.teamfighthandlers;

import org.bukkit.entity.Player;

public interface TeamFightHandler {

    default boolean doTeamFightCancel(Player attacker, Player victim) {
        return false;
    }

    void init();
}

