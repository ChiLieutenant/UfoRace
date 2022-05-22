package com.chilieutenant.uforace.events;

import com.chilieutenant.uforace.arena.Arena;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameLeaveEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter private final Player player;
    @Getter private final Arena arena;

    public GameLeaveEvent(Player player, Arena arena) {
        this.player = player;
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
