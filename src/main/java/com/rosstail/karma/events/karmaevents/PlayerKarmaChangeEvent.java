package com.rosstail.karma.events.karmaevents;

import com.rosstail.karma.players.PlayerDataModel;
import org.bukkit.entity.Player;
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
    private final PlayerDataModel model;
    private float value;
    private final boolean silent;

    public PlayerKarmaChangeEvent(Player player, PlayerDataModel model, float value, boolean silent) {
        this.player = player;
        this.model = model;
        this.value = value;
        this.silent = silent;
    }

    public Player getPlayer() throws NullPointerException {
        return player;
    }

    public PlayerDataModel getModel() {
        return model;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public boolean isSilent() {
        return silent;
    }
}
