package com.rosstail.karma.events.karmaevents.karmafightevents;

import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerKillMobEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Player player;
    private final Mob mob;
    private boolean cancelled;

    public PlayerKillMobEvent(Player player, Mob mob) {
        this.player = player;
        this.mob = mob;
    }

    public Player getPlayer() throws NullPointerException {
        return player;
    }

    public Mob getMob() {
        return mob;
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
