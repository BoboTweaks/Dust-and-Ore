package com.bobotweaks.dustandore.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;

import com.bobotweaks.dustandore.Constants;
import com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe;
import com.bobotweaks.dustandore.init.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class NeoforgeCrushingCategory implements IRecipeCategory<HammerCrushRecipe> {
    private static final ResourceLocation BG_TEX = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "textures/gui/jei/crushing-jei-gui.png");

    private final IDrawable background;
    private final IDrawable icon;

    public NeoforgeCrushingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(BG_TEX, 0, 0, 140, 70);
        ItemStack iconStack = ModItems.COPPER_CRUSHING_HAMMER != null
                ? new ItemStack(ModItems.COPPER_CRUSHING_HAMMER)
                : ItemStack.EMPTY;
        this.icon = guiHelper.createDrawableItemStack(iconStack);
    }

    @Override
    public RecipeType<HammerCrushRecipe> getRecipeType() {
        return NeoforgeJeiPlugin.CRUSHING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei." + Constants.MOD_ID + ".category.crushing");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HammerCrushRecipe recipe,
            mezz.jei.api.recipe.IFocusGroup focuses) {
        String ingredientId = recipe.getLeftHandItemId();
        if (ingredientId != null && !ingredientId.isEmpty()) {
            Holder.Reference<Item> ingHolder = BuiltInRegistries.ITEM.get(ResourceLocation.parse(ingredientId))
                    .orElse(null);
            if (ingHolder != null) {
                builder.addInputSlot(38, 18).addItemStack(new ItemStack(ingHolder.value()));
            }
        }

        if (recipe.getRewards() != null && !recipe.getRewards().isEmpty()) {
            List<ItemStack> outputs = new ArrayList<>();
            for (com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe.RewardEntry rw : recipe.getRewards()) {
                Holder.Reference<Item> outHolder = BuiltInRegistries.ITEM.get(ResourceLocation.parse(rw.itemId))
                        .orElse(null);
                if (outHolder != null) {
                    outputs.add(new ItemStack(outHolder.value()));
                }
            }
            if (!outputs.isEmpty()) {
                builder.addOutputSlot(86, 18).addItemStacks(outputs);
            }
        }
    }

    @Override
    public void draw(HammerCrushRecipe recipe, IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc != null && mc.font != null) {
            guiGraphics.drawString(mc.font, "→", 68, 22, 0xFF404040, false);

            Component line1 = Component.literal("Required Crush Power: " + recipe.getCrushPowerRequired());
            int textWidth = mc.font.width(line1);
            int centeredX = (140 - textWidth) / 2;
            guiGraphics.drawString(mc.font, line1, centeredX, 50, 0xFF404040, false);
        }
    }
}