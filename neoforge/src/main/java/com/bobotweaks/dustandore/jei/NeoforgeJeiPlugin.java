package com.bobotweaks.dustandore.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.ModList;
import mezz.jei.api.constants.VanillaTypes;
import net.minecraft.network.chat.Component;

import com.bobotweaks.dustandore.Constants;
import com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe;
import com.bobotweaks.dustandore.init.ModItems;
import com.bobotweaks.dustandore.init.RecipeTypes;

@JeiPlugin
public class NeoforgeJeiPlugin implements IModPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile IJeiRuntime RUNTIME = null;
    private static volatile boolean RECIPES_PUSHED = false;
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "jei_plugin");
    public static final RecipeType<HammerCrushRecipe> CRUSHING_TYPE = RecipeType.create(Constants.MOD_ID, "crushing",
            HammerCrushRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        reg.addRecipeCategories(new NeoforgeCrushingCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        RUNTIME = runtime;
        boolean createLoaded = false;
        try {
            createLoaded = ModList.get().isLoaded("create");
        } catch (Throwable ignored) {
        }
        if (!createLoaded) {
            try {
                if (com.bobotweaks.dustandore.init.ModItems.ZINC_DUST != null) {
                    RUNTIME.getIngredientManager().removeIngredientsAtRuntime(
                            VanillaTypes.ITEM_STACK,
                            java.util.List
                                    .of(new ItemStack(com.bobotweaks.dustandore.init.ModItems.ZINC_DUST)));
                }
            } catch (Throwable t) {
                LOGGER.error("[DustAndOre][JEI] onRuntimeAvailable: failed to hide zinc dust", t);
            }
        }
    }

    private static void onClientTickPost(ClientTickEvent.Post evt) {

    }

    private static void tryPushRecipesToRuntime() {
        if (true)
            return;
        net.minecraft.world.item.crafting.RecipeManager manager = getClientRecipeManager();
        if (manager == null) {
            return;
        }
        java.util.List<HammerCrushRecipe> list = new java.util.ArrayList<HammerCrushRecipe>();
        java.util.Collection<net.minecraft.world.item.crafting.RecipeHolder<?>> all = manager.getRecipes();
        for (net.minecraft.world.item.crafting.RecipeHolder<?> holder : all) {
            net.minecraft.world.item.crafting.Recipe<?> r = holder.value();
            boolean matchesType = (r.getType() == RecipeTypes.HAMMER_CRUSH);
            net.minecraft.resources.ResourceLocation sk = net.minecraft.core.registries.BuiltInRegistries.RECIPE_SERIALIZER
                    .getKey(r.getSerializer());
            boolean matchesSerializer = (sk != null && "dustandore:hammer_crush".equals(sk.toString()));
            if ((matchesType || matchesSerializer) && r instanceof HammerCrushRecipe) {
                list.add((HammerCrushRecipe) r);
            }
        }
        if (!list.isEmpty()) {
            RUNTIME.getRecipeManager().addRecipes(CRUSHING_TYPE, list);
            RECIPES_PUSHED = true;
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        net.minecraft.world.item.crafting.RecipeManager manager = getClientRecipeManager();
        java.util.List<HammerCrushRecipe> list = new java.util.ArrayList<HammerCrushRecipe>();
        if (manager != null) {
            java.util.Map<String, Integer> typeCounts = new java.util.HashMap<String, Integer>();
            int total = 0;
            for (net.minecraft.world.item.crafting.RecipeHolder<?> holder : manager.getRecipes()) {
                net.minecraft.world.item.crafting.Recipe<?> r = holder.value();
                total++;
                net.minecraft.resources.ResourceLocation typeId = net.minecraft.core.registries.BuiltInRegistries.RECIPE_TYPE
                        .getKey(r.getType());
                String key = typeId == null ? String.valueOf(r.getType()) : typeId.toString();
                typeCounts.put(key, typeCounts.getOrDefault(key, 0) + 1);
                boolean matchesType = (r.getType() == RecipeTypes.HAMMER_CRUSH);
                net.minecraft.resources.ResourceLocation sk = net.minecraft.core.registries.BuiltInRegistries.RECIPE_SERIALIZER
                        .getKey(r.getSerializer());
                boolean matchesSerializer = (sk != null && "dustandore:hammer_crush".equals(sk.toString()));
                if ((matchesType || matchesSerializer) && r instanceof HammerCrushRecipe) {
                    list.add((HammerCrushRecipe) r);
                }
            }
        }
        // Filter out zinc-dust recipes if Create is not loaded
        boolean createLoaded = false;
        try {
            createLoaded = ModList.get().isLoaded("create");
        } catch (Throwable ignored) {
        }
        if (!createLoaded && !list.isEmpty()) {
            java.util.Iterator<HammerCrushRecipe> it = list.iterator();
            while (it.hasNext()) {
                HammerCrushRecipe r = it.next();
                boolean yieldsZincDust = false;
                try {
                    if (r.getRewards() != null) {
                        for (com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe.RewardEntry rw : r
                                .getRewards()) {
                            if ("dustandore:zinc_dust".equals(rw.itemId)) {
                                yieldsZincDust = true;
                                break;
                            }
                        }
                    }
                } catch (Throwable ignored2) {
                }
                if (yieldsZincDust)
                    it.remove();
            }
        }
        if (!list.isEmpty()) {
            reg.addRecipes(CRUSHING_TYPE, list);
        }

        // Add an instruction page to hammers so players understand how to use crushing
        try {
            java.util.List<ItemStack> hammerInfos = new java.util.ArrayList<>();
            if (com.bobotweaks.dustandore.init.ModItems.COPPER_CRUSHING_HAMMER != null)
                hammerInfos
                        .add(new ItemStack(com.bobotweaks.dustandore.init.ModItems.COPPER_CRUSHING_HAMMER));
            if (com.bobotweaks.dustandore.init.ModItems.IRON_CRUSHING_HAMMER != null)
                hammerInfos.add(new ItemStack(com.bobotweaks.dustandore.init.ModItems.IRON_CRUSHING_HAMMER));
            if (com.bobotweaks.dustandore.init.ModItems.GOLDEN_CRUSHING_HAMMER != null)
                hammerInfos
                        .add(new ItemStack(com.bobotweaks.dustandore.init.ModItems.GOLDEN_CRUSHING_HAMMER));
            if (com.bobotweaks.dustandore.init.ModItems.DIAMOND_CRUSHING_HAMMER != null)
                hammerInfos
                        .add(new ItemStack(com.bobotweaks.dustandore.init.ModItems.DIAMOND_CRUSHING_HAMMER));
            if (com.bobotweaks.dustandore.init.ModItems.NETHERITE_CRUSHING_HAMMER != null)
                hammerInfos.add(
                        new ItemStack(com.bobotweaks.dustandore.init.ModItems.NETHERITE_CRUSHING_HAMMER));
            if (!hammerInfos.isEmpty()) {
                Component info = Component.literal(
                        "Hold a Crushing Hammer in your main hand and a raw ore or a crushable item in your offhand, then right-click to crush.");
                reg.addIngredientInfo(hammerInfos, VanillaTypes.ITEM_STACK, info);
            }
        } catch (Throwable t) {
            LOGGER.error("[DustAndOre][JEI] registerRecipes: failed to add ingredient info", t);
        }
    }

    private static net.minecraft.world.item.crafting.RecipeManager getClientRecipeManager() {
        try {
            Minecraft mc;
            try {
                mc = Minecraft.getInstance();
            } catch (Throwable t0) {
                mc = null;
            }
            if (mc == null)
                return null;
            // Try level first
            try {
                Object lvl = mc.level;
                if (lvl != null) {
                    try {
                        java.lang.reflect.Method m = lvl.getClass().getMethod("getRecipeManager");
                        Object rm = m.invoke(lvl);
                        if (rm instanceof net.minecraft.world.item.crafting.RecipeManager)
                            return (net.minecraft.world.item.crafting.RecipeManager) rm;
                    } catch (Throwable ignored) {
                        try {
                            java.lang.reflect.Field f = lvl.getClass().getDeclaredField("recipeManager");
                            f.setAccessible(true);
                            Object rm = f.get(lvl);
                            if (rm instanceof net.minecraft.world.item.crafting.RecipeManager)
                                return (net.minecraft.world.item.crafting.RecipeManager) rm;
                        } catch (Throwable ignored2) {
                        }
                    }
                }
            } catch (Throwable ignored3) {
            }
            // Try connection reflectively
            Object conn = null;
            try {
                conn = mc.getConnection();
            } catch (Throwable ignored4) {
                conn = null;
            }
            if (conn != null) {
                try {
                    java.lang.reflect.Method m = conn.getClass().getMethod("getRecipeManager");
                    Object rm = m.invoke(conn);
                    if (rm instanceof net.minecraft.world.item.crafting.RecipeManager)
                        return (net.minecraft.world.item.crafting.RecipeManager) rm;
                } catch (Throwable ignored5) {
                }
            }
            // Singleplayer server fallback
            try {
                java.lang.reflect.Method m2 = mc.getClass().getMethod("getSingleplayerServer");
                Object srv = m2.invoke(mc);
                if (srv != null) {
                    try {
                        java.lang.reflect.Method mRM = srv.getClass().getMethod("getRecipeManager");
                        Object rm2 = mRM.invoke(srv);
                        if (rm2 instanceof net.minecraft.world.item.crafting.RecipeManager)
                            return (net.minecraft.world.item.crafting.RecipeManager) rm2;
                    } catch (Throwable ignored6) {
                    }
                }
            } catch (Throwable ignored7) {
            }
        } catch (Throwable t) {
            // ignore
        }
        return null;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        if (ModItems.COPPER_CRUSHING_HAMMER != null)
            reg.addRecipeCatalyst(new ItemStack(ModItems.COPPER_CRUSHING_HAMMER), CRUSHING_TYPE);
        if (ModItems.IRON_CRUSHING_HAMMER != null)
            reg.addRecipeCatalyst(new ItemStack(ModItems.IRON_CRUSHING_HAMMER), CRUSHING_TYPE);
        if (ModItems.GOLDEN_CRUSHING_HAMMER != null)
            reg.addRecipeCatalyst(new ItemStack(ModItems.GOLDEN_CRUSHING_HAMMER), CRUSHING_TYPE);
        if (ModItems.DIAMOND_CRUSHING_HAMMER != null)
            reg.addRecipeCatalyst(new ItemStack(ModItems.DIAMOND_CRUSHING_HAMMER), CRUSHING_TYPE);
        if (ModItems.NETHERITE_CRUSHING_HAMMER != null)
            reg.addRecipeCatalyst(new ItemStack(ModItems.NETHERITE_CRUSHING_HAMMER), CRUSHING_TYPE);
    }
}