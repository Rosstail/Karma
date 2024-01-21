package com.rosstail.karma.events.karmaevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerOverTimeTriggerEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final String overtimeLoopName;
    private final int amount;
    private final long nextDelay;

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlayerOverTimeTriggerEvent(Player player, String loopName, int amount, long nextDelay) {
        this.player = player;
        this.overtimeLoopName = loopName;
        this.amount = amount;
        this.nextDelay = nextDelay;
    }

    public Player getPlayer() {
        return player;
    }

    public String getOvertimeLoopName() {
        return overtimeLoopName;
    }

    public int getAmount() {
        return amount;
    }

    public long getNextDelay() {
        return nextDelay;
    }
}
