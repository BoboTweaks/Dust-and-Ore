package com.bobotweaks.dustandore.init;

import com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe;
import com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipeType;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.Registry;

public final class RecipeTypes {
    public static RecipeType<HammerCrushRecipe> HAMMER_CRUSH;

    public static void load() {
        HAMMER_CRUSH = Registry.register(
                BuiltInRegistries.RECIPE_TYPE,
                ResourceLocation.fromNamespaceAndPath("dustandore", "hammer_crush"),
                HammerCrushRecipeType.INSTANCE);
    }

    private RecipeTypes() {
    }
}
