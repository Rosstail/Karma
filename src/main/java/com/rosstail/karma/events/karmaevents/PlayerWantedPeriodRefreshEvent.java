package com.rosstail.karma.events.karmaevents;

import com.rosstail.karma.datas.PlayerModel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerWantedPeriodRefreshEvent extends Event {

    private final Player player;
    private final PlayerModel model;
    private boolean timeExtend;

    public PlayerWantedPeriodRefreshEvent (Player player, PlayerModel model, boolean timeExtend) {
        this.player = player;
        this.model = model;
        this.timeExtend = timeExtend;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerModel getModel() {
        return model;
    }

    public boolean isTimeExtend() {
        return timeExtend;
    }

    public void setTimeExtend(boolean timeExtend) {
        this.timeExtend = timeExtend;
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
