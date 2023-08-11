package com.rosstail.karma.events.karmaevents;

import com.rosstail.karma.datas.PlayerModel;
import com.rosstail.karma.tiers.Tier;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerTierChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    private final Player player;
    private final PlayerModel model;
    private final String tierName;

    public PlayerTierChangeEvent(Player player, PlayerModel model, String tierName) {
        this.player = player;
        this.model = model;
        this.tierName = tierName;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerModel getModel() {
        return model;
    }

    public String getTierName() {
        return tierName;
    }
}
