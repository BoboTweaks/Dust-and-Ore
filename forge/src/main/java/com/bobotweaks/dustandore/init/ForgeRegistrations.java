package com.bobotweaks.dustandore.init;

import com.bobotweaks.dustandore.ForgeMod;
import com.bobotweaks.dustandore.init.RecipeSerializers;
import com.bobotweaks.dustandore.init.RecipeTypes;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = ForgeMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ForgeRegistrations {

    private ForgeRegistrations() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.RECIPE_TYPE) {
            RecipeTypes.load();
        } else if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
            RecipeSerializers.load();
        }
    }
}