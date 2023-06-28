package com.rosstail.karma.events.karmaevents;

import com.rosstail.karma.datas.PlayerModel;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerKarmaChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Player player;
    private final PlayerModel model;
    private float value;

    public PlayerKarmaChangeEvent(Player player, PlayerModel model, float value) {
        this.player = player;
        this.model = model;
        this.value = value;
    }

    public Player getPlayer() throws NullPointerException {
        return player;
    }

    public PlayerModel getModel() {
        return model;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

}
