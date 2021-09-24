package com.rosstail.karma.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerOverTimeHasTriggeredEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlayerOverTimeHasTriggeredEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
