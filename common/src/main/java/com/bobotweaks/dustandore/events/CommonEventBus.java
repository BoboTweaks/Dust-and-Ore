package com.bobotweaks.dustandore.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class CommonEventBus {
    private static final List<Consumer<HammerCrushEvent>> HAMMER_CRUSH_LISTENERS = new CopyOnWriteArrayList<>();

    private CommonEventBus() {}

    public static void registerHammerCrush(Consumer<HammerCrushEvent> listener) {
        if (listener != null) HAMMER_CRUSH_LISTENERS.add(listener);
    }

    public static boolean postHammerCrush(HammerCrushEvent event) {
        for (Consumer<HammerCrushEvent> l : HAMMER_CRUSH_LISTENERS) {
            try {
                l.accept(event);
                if (event.isCanceled()) return true;
            } catch (Throwable ignored) {}
        }
        return event.isCanceled();
    }
}
