package com.bobotweaks.dustandore.init;

import com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipeSerializer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class RecipeSerializers {
    public static RecipeSerializer<?> HAMMER_CRUSH;

    public static void load() {
        HAMMER_CRUSH = Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                ResourceLocation.fromNamespaceAndPath("dustandore", "hammer_crush"),
                HammerCrushRecipeSerializer.INSTANCE);
    }

    private RecipeSerializers() {
    }
}
