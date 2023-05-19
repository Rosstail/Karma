package com.rosstail.karma.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerOverTimeResetEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final String overtimeLoopName;
    private boolean cancelled;

    public PlayerOverTimeResetEvent(Player player, String overtimeLoopName) {
        this.player = player;
        this.overtimeLoopName = overtimeLoopName;
    }

    public Player getPlayer() {
        return player;
    }

    public String getOvertimeLoopName() {
        return overtimeLoopName;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
