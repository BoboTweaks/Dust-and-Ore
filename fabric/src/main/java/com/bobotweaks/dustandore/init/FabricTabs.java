package com.bobotweaks.dustandore.init;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;

/**
 * Fabric-only creative tab population. Keeps common module loader-agnostic.
 */
public final class FabricTabs {
    private FabricTabs() {
    }

    public static void load() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(tab -> {
            tab.accept(ModItems.ZINC_DUST);
            tab.accept(ModItems.COPPER_DUST);
            tab.accept(ModItems.IRON_DUST);
            tab.accept(ModItems.GOLD_DUST);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(tab -> {
            tab.accept(ModItems.COPPER_CRUSHING_HAMMER);
            tab.accept(ModItems.IRON_CRUSHING_HAMMER);
            tab.accept(ModItems.GOLDEN_CRUSHING_HAMMER);
            tab.accept(ModItems.DIAMOND_CRUSHING_HAMMER);
            tab.accept(ModItems.NETHERITE_CRUSHING_HAMMER);
        });
    }
}
