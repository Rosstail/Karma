package com.rosstail.karma.events.karmaevents.karmafightevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerDamagePlayerEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public final Player attacker;
    public final Player victim;
    private boolean cancelled;

    public PlayerDamagePlayerEvent(Player attacker, Player victim) {
        this.attacker = attacker;
        this.victim = victim;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}