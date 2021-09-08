package com.rosstail.karma.customEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerOverTimeHasResetEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final int overtimeScheduler;

    public PlayerOverTimeHasResetEvent(Player player, int overtimeScheduler) {
        this.player = player;
        this.overtimeScheduler = overtimeScheduler;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public int getOvertimeScheduler() {
        return overtimeScheduler;
    }
}
