package com.rosstail.karma.customEvents;

import com.rosstail.karma.tiers.Tier;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerTierHasChangedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private final Player player;
    private final Tier tier;

    public PlayerTierHasChangedEvent(Player player, Tier tier) {
        this.player = player;
        this.tier = tier;
    }

    public Player getPlayer() {
        return player;
    }

    public Tier getTier() {
        return tier;
    }
}
