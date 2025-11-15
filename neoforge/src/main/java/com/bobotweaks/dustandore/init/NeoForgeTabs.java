package com.bobotweaks.dustandore.init;

import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.fml.ModList;

public final class NeoForgeTabs {
    private NeoForgeTabs() {}

    public static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            boolean createLoaded = false;
            try { createLoaded = ModList.get().isLoaded("create"); } catch (Throwable ignored) {}
            if (createLoaded) {
                event.accept(NeoForgeItems.ZINC_DUST.get());
            }
            event.accept(NeoForgeItems.COPPER_DUST.get());
            event.accept(NeoForgeItems.IRON_DUST.get());
            event.accept(NeoForgeItems.GOLD_DUST.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(NeoForgeItems.COPPER_CRUSHING_HAMMER.get());
            event.accept(NeoForgeItems.IRON_CRUSHING_HAMMER.get());
            event.accept(NeoForgeItems.GOLDEN_CRUSHING_HAMMER.get());
            event.accept(NeoForgeItems.DIAMOND_CRUSHING_HAMMER.get());
            event.accept(NeoForgeItems.NETHERITE_CRUSHING_HAMMER.get());
        }
    }
}
