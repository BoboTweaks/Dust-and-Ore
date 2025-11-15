package com.bobotweaks.dustandore.init;

import com.bobotweaks.dustandore.ForgeMod;
import com.bobotweaks.dustandore.init.ForgeItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ForgeMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ForgeTabs {

    private ForgeTabs() {
    }

    @SubscribeEvent
    public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ForgeItems.ZINC_DUST.get());
            event.accept(ForgeItems.COPPER_DUST.get());
            event.accept(ForgeItems.IRON_DUST.get());
            event.accept(ForgeItems.GOLD_DUST.get());
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ForgeItems.COPPER_CRUSHING_HAMMER.get());
            event.accept(ForgeItems.IRON_CRUSHING_HAMMER.get());
            event.accept(ForgeItems.GOLDEN_CRUSHING_HAMMER.get());
            event.accept(ForgeItems.DIAMOND_CRUSHING_HAMMER.get());
            event.accept(ForgeItems.NETHERITE_CRUSHING_HAMMER.get());
        }
    }
}