package com.rosstail.karma.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerWantedPeriodRefreshEvent extends Event implements Cancellable {

    private final Player player;
    private final Object cause;
    private boolean timeExtend;
    private boolean cancelled = false;

    public PlayerWantedPeriodRefreshEvent (Player player, Object cause, boolean timeExtend) {
        this.player = player;
        this.cause = cause;
        this.timeExtend = timeExtend;
    }

    public Player getPlayer() {
        return player;
    }

    public Object getCause() {
        return cause;
    }

    public boolean isTimeExtend() {
        return timeExtend;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setTimeExtend(boolean timeExtend) {
        this.timeExtend = timeExtend;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
