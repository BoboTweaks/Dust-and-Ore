package com.bobotweaks.dustandore;

import com.bobotweaks.dustandore.init.ForgeItems;
import com.bobotweaks.dustandore.init.ForgeTabs;

import com.bobotweaks.dustandore.network.ForgeNetworkHandler;

import com.bobotweaks.dustandore.Constants;

import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class ForgeMod {

    public static final String MOD_ID = Constants.MOD_ID;

    public ForgeMod(FMLJavaModLoadingContext context) {
        Constants.LOG.info("Dust & Ores Mod initializing...");

        final var modBusGroup = context.getModBusGroup();
        ForgeItems.REGISTER.register(modBusGroup);
        ForgeNetworkHandler.register();
    }
}