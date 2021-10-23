package com.rosstail.karma.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerKarmaChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    private final Player player;
    private double value;
    private boolean overTimeReset;
    private boolean cancelled;
    private final Object cause;

    public PlayerKarmaChangeEvent(Player player, double value, boolean isOverTimeReset, Object cause) {
        this.player = player;
        this.value = value;
        this.overTimeReset = isOverTimeReset;
        this.cancelled = false;
        this.cause = cause;
    }

    public Player getPlayer() throws NullPointerException {
        return player;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public boolean isOverTimeReset() {
        return overTimeReset;
    }

    public void setOverTimeReset(boolean overTimeReset) {
        this.overTimeReset = overTimeReset;
    }

    public Object getCause() {
        return cause;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
