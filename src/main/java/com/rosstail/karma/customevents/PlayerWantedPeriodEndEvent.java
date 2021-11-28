package com.rosstail.karma.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerWantedPeriodEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Object cause;

    public PlayerWantedPeriodEndEvent(Player player, Object cause) {
        this.player = player;
        this.cause = cause;
    }

    public Player getPlayer() {
        return player;
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
}
