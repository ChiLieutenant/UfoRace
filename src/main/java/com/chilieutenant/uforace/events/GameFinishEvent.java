package com.chilieutenant.uforace.events;

import com.chilieutenant.uforace.arena.Arena;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameFinishEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter private final Player winner;
    @Getter private final Arena arena;

    public GameFinishEvent(Player winner, Arena arena){
        this.winner = winner;
        this.arena = arena;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
