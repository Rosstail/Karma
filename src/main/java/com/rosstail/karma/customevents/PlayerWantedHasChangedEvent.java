package com.rosstail.karma.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

public class PlayerWantedHasChangedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private Timestamp timestamp;
    private final Object cause;

    public PlayerWantedHasChangedEvent(Player player, Timestamp timestamp, Object cause) {
        this.player = player;
        this.timestamp = timestamp;
        this.cause = cause;
    }

    public Player getPlayer() {
        return player;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public Object getCause() {
        return cause;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
