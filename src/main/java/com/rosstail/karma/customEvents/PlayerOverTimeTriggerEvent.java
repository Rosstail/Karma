package com.rosstail.karma.customEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerOverTimeTriggerEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private int overtimeScheduler;
    private boolean cancelled;

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlayerOverTimeTriggerEvent(Player player) {
        this.player = player;
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public Player getPlayer() {
        return player;
    }

    public int getOvertimeScheduler() {
        return overtimeScheduler;
    }

    public void setOvertimeScheduler(int overtimeScheduler) {
        this.overtimeScheduler = overtimeScheduler;
    }
}
