package com.bobotweaks.dustandore;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import com.bobotweaks.dustandore.NeoforgeClient;
import com.bobotweaks.dustandore.events.NeoforgeRightClickBlock;
import com.bobotweaks.dustandore.events.NeoforgeRightClickItem;
import com.bobotweaks.dustandore.init.ModItems;
import com.bobotweaks.dustandore.init.RecipeTypes;

import com.bobotweaks.dustandore.network.NeoNetworkHandler;

import com.bobotweaks.dustandore.init.RecipeSerializers;
import com.bobotweaks.dustandore.init.NeoForgeTabs;
import com.bobotweaks.dustandore.init.NeoForgeItems;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import com.bobotweaks.dustandore.core.CrushingManager;
import com.bobotweaks.dustandore.core.CrushingInteraction;
import com.bobotweaks.dustandore.Constants;

@Mod(Constants.MOD_ID)
public class NeoforgeMod {

    public static final String MOD_ID = Constants.MOD_ID;

    public NeoforgeMod(IEventBus modEventBus) {
        NeoForgeItems.REGISTRY.register(modEventBus);
        modEventBus.addListener(this::onCommonSetup);

        modEventBus.addListener((RegisterEvent evt) -> {
            if (evt.getRegistryKey() == Registries.RECIPE_TYPE) {
                RecipeTypes.load();
            } else if (evt.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
                RecipeSerializers.load();
            }
        });

        modEventBus.addListener(NeoForgeTabs::onBuildCreativeTabs);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            NeoForge.EVENT_BUS.addListener(NeoforgeClient::onItemTooltip);
        }

        NeoforgeRightClickItem.register();
        NeoforgeRightClickBlock.register();
        modEventBus.addListener(NeoNetworkHandler::register);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        ModItems.COPPER_DUST = NeoForgeItems.COPPER_DUST.get();
        ModItems.IRON_DUST = NeoForgeItems.IRON_DUST.get();
        ModItems.GOLD_DUST = NeoForgeItems.GOLD_DUST.get();

        if (NeoForgeItems.ZINC_DUST != null) {
            ModItems.ZINC_DUST = NeoForgeItems.ZINC_DUST.get();
        } else {
            ModItems.ZINC_DUST = null;
        }

        ModItems.COPPER_CRUSHING_HAMMER = NeoForgeItems.COPPER_CRUSHING_HAMMER.get();
        ModItems.IRON_CRUSHING_HAMMER = NeoForgeItems.IRON_CRUSHING_HAMMER.get();
        ModItems.GOLDEN_CRUSHING_HAMMER = NeoForgeItems.GOLDEN_CRUSHING_HAMMER.get();
        ModItems.DIAMOND_CRUSHING_HAMMER = NeoForgeItems.DIAMOND_CRUSHING_HAMMER.get();
        ModItems.NETHERITE_CRUSHING_HAMMER = NeoForgeItems.NETHERITE_CRUSHING_HAMMER.get();
    }

}