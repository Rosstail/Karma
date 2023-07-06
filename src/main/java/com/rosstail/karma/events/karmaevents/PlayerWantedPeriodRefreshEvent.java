package com.rosstail.karma.events.karmaevents;

import com.rosstail.karma.datas.PlayerModel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerWantedPeriodRefreshEvent extends Event {

    private final Player player;
    private final PlayerModel model;

    public PlayerWantedPeriodRefreshEvent (Player player, PlayerModel model) {
        this.player = player;
        this.model = model;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerModel getModel() {
        return model;
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
