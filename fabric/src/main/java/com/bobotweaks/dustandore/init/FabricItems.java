package com.bobotweaks.dustandore.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Function;

import com.bobotweaks.dustandore.item.tools.*;
import com.bobotweaks.dustandore.item.materials.*;

public final class FabricItems {
    private FabricItems() {
    }

    public static void load() {
        // Dusts
        Item COPPER_DUST = register("copper_dust", CopperDustItem::new);
        Item IRON_DUST = register("iron_dust", IronDustItem::new);
        Item GOLD_DUST = register("gold_dust", GoldDustItem::new);
        Item ZINC_DUST = register("zinc_dust", ZincDustItem::new);

        // Hammers
        Item COPPER_CRUSHING_HAMMER = register("copper_crushing_hammer", CopperCrushingHammerItem::new);
        Item IRON_CRUSHING_HAMMER = register("iron_crushing_hammer", IronCrushingHammerItem::new);
        Item GOLDEN_CRUSHING_HAMMER = register("golden_crushing_hammer", GoldenCrushingHammerItem::new);
        Item DIAMOND_CRUSHING_HAMMER = register("diamond_crushing_hammer", DiamondCrushingHammerItem::new);
        Item NETHERITE_CRUSHING_HAMMER = register("netherite_crushing_hammer", NetheriteCrushingHammerItem::new);

        // Assign to common holder
        ModItems.COPPER_DUST = COPPER_DUST;
        ModItems.IRON_DUST = IRON_DUST;
        ModItems.GOLD_DUST = GOLD_DUST;
        ModItems.ZINC_DUST = ZINC_DUST;

        ModItems.COPPER_CRUSHING_HAMMER = COPPER_CRUSHING_HAMMER;
        ModItems.IRON_CRUSHING_HAMMER = IRON_CRUSHING_HAMMER;
        ModItems.GOLDEN_CRUSHING_HAMMER = GOLDEN_CRUSHING_HAMMER;
        ModItems.DIAMOND_CRUSHING_HAMMER = DIAMOND_CRUSHING_HAMMER;
        ModItems.NETHERITE_CRUSHING_HAMMER = NETHERITE_CRUSHING_HAMMER;
    }

    private static <I extends Item> I register(String name, Function<Item.Properties, ? extends I> supplier) {
        return (I) Items.registerItem(
                ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("dustandore", name)),
                (Function<Item.Properties, Item>) supplier);
    }
}
