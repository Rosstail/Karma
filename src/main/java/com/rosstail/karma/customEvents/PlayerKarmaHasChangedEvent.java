package com.rosstail.karma.customEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerKarmaHasChangedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    private final Player player;
    private double value;
    private boolean overTimeChange;

    public PlayerKarmaHasChangedEvent(Player player, double value, boolean isOverTimeChange) {
        this.player = player;
        this.value = value;
        this.overTimeChange = isOverTimeChange;
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

    public boolean isOverTimeChange() {
        return overTimeChange;
    }
}
