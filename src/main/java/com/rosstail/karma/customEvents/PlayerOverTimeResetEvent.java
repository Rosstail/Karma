package com.rosstail.karma.customEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerOverTimeResetEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private int triggerID;
    private boolean cancelled;

    public PlayerOverTimeResetEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public int getTriggerID() {
        return triggerID;
    }

    public void setTriggerID(int triggerID) {
        this.triggerID = triggerID;
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
