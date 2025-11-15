package com.bobotweaks.dustandore;

import com.bobotweaks.dustandore.Constants;
import com.bobotweaks.dustandore.events.FabricRightClickBlock;
import com.bobotweaks.dustandore.events.FabricRightClickItem;
import com.bobotweaks.dustandore.init.FabricItems;
import com.bobotweaks.dustandore.init.FabricTabs;
import com.bobotweaks.dustandore.init.RecipeSerializers;
import com.bobotweaks.dustandore.init.RecipeTypes;
import com.bobotweaks.dustandore.network.FabricCrushingSync;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public class FabricMod implements ModInitializer {
    public static final String MODID = "dustandore";

    @Override
    public void onInitialize() {
        Log("Initializing Dust &Ore Mod.", 0);

        com.bobotweaks.dustandore.network.FabricNetwork.registerPayloadTypes();

        try {
            FabricTabs.load();
        } catch (Throwable ignored) {
        }

        FabricItems.load();
        RecipeTypes.load();
        RecipeSerializers.load();

        FabricRightClickItem.register();
        FabricRightClickBlock.register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            FabricCrushingSync.sendSummaryToPlayer(player);
        });
    }

    public static void Log(String message, int type) {
        switch (type) {
            case 0:
                Constants.LOG.info(message);
                break;
            case 1:
                Constants.LOG.warn(message);
                break;
            case 2:
                Constants.LOG.error(message);
                break;
            default:
                Constants.LOG.info(message);
                break;
        }
    }
}