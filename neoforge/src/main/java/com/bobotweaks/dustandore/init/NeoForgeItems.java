package com.bobotweaks.dustandore.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;

import java.util.function.Function;
import com.bobotweaks.dustandore.item.tools.*;
import com.bobotweaks.dustandore.item.materials.*;

public final class NeoForgeItems {
        private NeoForgeItems() {
        }

        public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems("dustandore");
        public static final DeferredItem<Item> COPPER_DUST = REGISTRY.registerItem("copper_dust", CopperDustItem::new,
                        new Item.Properties());
        public static final DeferredItem<Item> IRON_DUST = REGISTRY.registerItem("iron_dust", IronDustItem::new,
                        new Item.Properties());
        public static final DeferredItem<Item> GOLD_DUST = REGISTRY.registerItem("gold_dust", GoldDustItem::new,
                        new Item.Properties());

        public static final DeferredItem<Item> ZINC_DUST = (ModList.get().isLoaded("create")
                        ? REGISTRY.registerItem("zinc_dust", ZincDustItem::new, new Item.Properties())
                        : null);

        public static final DeferredItem<Item> COPPER_CRUSHING_HAMMER = REGISTRY.registerItem("copper_crushing_hammer",
                        CopperCrushingHammerItem::new, new Item.Properties());
        public static final DeferredItem<Item> IRON_CRUSHING_HAMMER = REGISTRY.registerItem("iron_crushing_hammer",
                        IronCrushingHammerItem::new, new Item.Properties());
        public static final DeferredItem<Item> GOLDEN_CRUSHING_HAMMER = REGISTRY.registerItem("golden_crushing_hammer",
                        GoldenCrushingHammerItem::new, new Item.Properties());
        public static final DeferredItem<Item> DIAMOND_CRUSHING_HAMMER = REGISTRY
                        .registerItem("diamond_crushing_hammer", DiamondCrushingHammerItem::new, new Item.Properties());
        public static final DeferredItem<Item> NETHERITE_CRUSHING_HAMMER = REGISTRY.registerItem(
                        "netherite_crushing_hammer", NetheriteCrushingHammerItem::new, new Item.Properties());
}
