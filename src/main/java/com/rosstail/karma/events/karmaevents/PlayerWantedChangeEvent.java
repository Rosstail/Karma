package com.rosstail.karma.events.karmaevents;

import com.rosstail.karma.players.PlayerModel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

public class PlayerWantedChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final PlayerModel model;
    private Timestamp timestamp;

    private final boolean silent;

    public PlayerWantedChangeEvent(Player player, PlayerModel model, Timestamp timestamp, boolean silent) {
        this.player = player;
        this.model = model;
        this.timestamp = timestamp;
        this.silent = silent;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerModel getModel() {
        return model;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public boolean isSilent() {
        return silent;
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
