package com.rosstail.karma.customEvents;

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

    private Player player;
    private Tier tier;

    public PlayerTierChangeEvent(Player player, Tier tier) {
        this.player = player;
        this.tier = tier;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setTier(Tier tier) {
        this.tier = tier;
    }

    public Player getPlayer() {
        return player;
    }

    public Tier getTier() {
        return tier;
    }
}
