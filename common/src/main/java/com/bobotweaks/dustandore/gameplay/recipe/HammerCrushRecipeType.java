package com.bobotweaks.dustandore.gameplay.recipe;

import net.minecraft.world.item.crafting.RecipeType;

public class HammerCrushRecipeType implements RecipeType<HammerCrushRecipe> {
    public static final HammerCrushRecipeType INSTANCE = new HammerCrushRecipeType();

    private HammerCrushRecipeType() {}

    @Override
    public String toString() {
        return "dustandore:hammer_crush";
    }
}
