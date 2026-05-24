package com.pathmind.util;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.function.Consumer;

public final class UseItemCallbackCompat {
    private UseItemCallbackCompat() {
    }

    public static void register(Consumer<String> eventSink, String eventName) {
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickItem event) -> {
            if (event.getLevel().isClientSide()) {
                eventSink.accept(eventName);
            }
        });
    }
}
