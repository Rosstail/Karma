package com.rosstail.karma.events.karmaevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerWantedPunishEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player punishedPlayer;
    private final Player punisherPlayer;

    public PlayerWantedPunishEvent(Player punishedPlayer, Player punisherPlayer) {
        this.punishedPlayer = punishedPlayer;
        this.punisherPlayer = punisherPlayer;
    }

    public Player getPunishedPlayer() {
        return punishedPlayer;
    }

    public Player getPunisherPlayer() {
        return punisherPlayer;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
