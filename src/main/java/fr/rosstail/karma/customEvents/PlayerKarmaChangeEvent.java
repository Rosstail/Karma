package fr.rosstail.karma.customEvents;

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

    private Player player;
    private double value;
    private boolean overTimeChange;
    private boolean cancelled;

    public PlayerKarmaChangeEvent(Player player, double value, boolean isOverTimeChange) {
        this.player = player;
        this.value = value;
        this.overTimeChange = isOverTimeChange;
        this.cancelled = false;
    }


    public Player getPlayer() throws NullPointerException {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
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

    public void setOverTimeChange(boolean overTimeChange) {
        this.overTimeChange = overTimeChange;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
