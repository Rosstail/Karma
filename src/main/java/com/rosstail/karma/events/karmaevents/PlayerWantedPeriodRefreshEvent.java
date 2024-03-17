package com.rosstail.karma.events.karmaevents;

import com.rosstail.karma.players.PlayerModel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerWantedPeriodRefreshEvent extends Event {

    private final Player player;
    private final PlayerModel model;
    private final boolean silent;

    public PlayerWantedPeriodRefreshEvent (Player player, PlayerModel model, boolean silent) {
        this.player = player;
        this.model = model;
        this.silent = silent;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerModel getModel() {
        return model;
    }

    public boolean isSilent() {
        return silent;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
