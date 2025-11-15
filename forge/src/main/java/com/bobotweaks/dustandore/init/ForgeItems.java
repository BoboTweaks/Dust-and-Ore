package com.bobotweaks.dustandore.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.bobotweaks.dustandore.item.tools.*;
import com.bobotweaks.dustandore.item.materials.*;

public final class ForgeItems {
        private ForgeItems() {
        }

        public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS,
                        "dustandore");

        public static final RegistryObject<Item> COPPER_DUST = REGISTER.register("copper_dust",
                        () -> new CopperDustItem(new Item.Properties().setId(REGISTER.key("copper_dust"))));
        public static final RegistryObject<Item> IRON_DUST = REGISTER.register("iron_dust",
                        () -> new IronDustItem(new Item.Properties().setId(REGISTER.key("iron_dust"))));
        public static final RegistryObject<Item> GOLD_DUST = REGISTER.register("gold_dust",
                        () -> new GoldDustItem(new Item.Properties().setId(REGISTER.key("gold_dust"))));
        public static final RegistryObject<Item> ZINC_DUST = REGISTER.register("zinc_dust",
                        () -> new ZincDustItem(new Item.Properties().setId(REGISTER.key("zinc_dust"))));

        public static final RegistryObject<Item> COPPER_CRUSHING_HAMMER = REGISTER.register("copper_crushing_hammer",
                        () -> new CopperCrushingHammerItem(
                                        new Item.Properties().setId(REGISTER.key("copper_crushing_hammer"))));
        public static final RegistryObject<Item> IRON_CRUSHING_HAMMER = REGISTER.register("iron_crushing_hammer",
                        () -> new IronCrushingHammerItem(
                                        new Item.Properties().setId(REGISTER.key("iron_crushing_hammer"))));
        public static final RegistryObject<Item> GOLDEN_CRUSHING_HAMMER = REGISTER.register("golden_crushing_hammer",
                        () -> new GoldenCrushingHammerItem(
                                        new Item.Properties().setId(REGISTER.key("golden_crushing_hammer"))));
        public static final RegistryObject<Item> DIAMOND_CRUSHING_HAMMER = REGISTER.register("diamond_crushing_hammer",
                        () -> new DiamondCrushingHammerItem(
                                        new Item.Properties().setId(REGISTER.key("diamond_crushing_hammer"))));
        public static final RegistryObject<Item> NETHERITE_CRUSHING_HAMMER = REGISTER.register(
                        "netherite_crushing_hammer",
                        () -> new NetheriteCrushingHammerItem(
                                        new Item.Properties().setId(REGISTER.key("netherite_crushing_hammer"))));
}
