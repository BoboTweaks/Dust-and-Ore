package com.bobotweaks.dustandore.core;

import com.bobotweaks.dustandore.Constants;
import com.bobotweaks.dustandore.init.ModItems;
import com.bobotweaks.dustandore.init.RecipeTypes;
import com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe;
import com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe.RewardEntry;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.bobotweaks.dustandore.gameplay.hammer.HammerUtilities;
import com.bobotweaks.dustandore.gameplay.hammer.PowerLoader;
import com.bobotweaks.dustandore.gameplay.hammer.SpeedLoader;
import com.bobotweaks.dustandore.gameplay.EnchantmentUtilities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.client.Minecraft;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

public class CrushingManager {

    private static boolean recipeCacheLoaded = false;
    private static final Map<String, RecipeOverride> recipeOverrides = new HashMap<>();

    private static final class RecipeOverride {
        final String outputItemId;
        final int[] countMin; // size 4
        final int[] countMax; // size 4

        RecipeOverride(String outputItemId, int[] countMin, int[] countMax) {
            this.outputItemId = outputItemId;
            this.countMin = countMin;
            this.countMax = countMax;
        }

    }

    public static final class ClientSummary {
        public final String ingredientId;
        public final int requiredPower;
        public final String outputItemId;
        public final int[] countMin;
        public final int[] countMax;
        public final String requiredMod;

        public ClientSummary(String ingredientId, int requiredPower, String outputItemId, int[] countMin,
                int[] countMax, String requiredMod) {
            this.ingredientId = ingredientId;
            this.requiredPower = requiredPower;
            this.outputItemId = outputItemId;
            this.countMin = countMin;
            this.countMax = countMax;
            this.requiredMod = requiredMod;
        }
    }

    private static volatile java.util.List<ClientSummary> CLIENT_SUMMARY = java.util.Collections.emptyList();

    public static void setClientSummary(java.util.List<ClientSummary> list) {
        CLIENT_SUMMARY = list != null ? list : java.util.Collections.emptyList();
    }

    public static final class DropEntry {
        public final String ingredientId;
        public final String outputItemId;
        public final int min;
        public final int max;
        public final double chance;

        public DropEntry(String ingredientId, String outputItemId, int min, int max, double chance) {
            this.ingredientId = ingredientId;
            this.outputItemId = outputItemId;
            this.min = min;
            this.max = max;
            this.chance = chance;
        }
    }

    private static void ensureRecipeCache(LevelAccessor world) {
        if (recipeCacheLoaded)
            return;
        if (!(world instanceof ServerLevel serverLevel))
            return;
        loadRecipeFile(serverLevel, "raw_copper_crushing_recipe.json");
        loadRecipeFile(serverLevel, "raw_iron_crushing_recipe.json");
        loadRecipeFile(serverLevel, "raw_gold_crushing_recipe.json");
        recipeCacheLoaded = true;
    }

    private static void ensureRecipeCacheAnySide() {
        if (recipeCacheLoaded)
            return;
        try {
            Minecraft mc = null;
            try {
                mc = Minecraft.getInstance();
            } catch (Throwable t) {
                mc = null;
            }
            if (mc != null && mc.getResourceManager() != null) {
                ResourceManager rm = mc.getResourceManager();
                int before = recipeOverrides.size();
                loadRecipeFile(rm, "raw_copper_crushing_recipe.json");
                loadRecipeFile(rm, "raw_iron_crushing_recipe.json");
                loadRecipeFile(rm, "raw_gold_crushing_recipe.json");
                if (recipeOverrides.size() > before) {
                    recipeCacheLoaded = true;
                }
            }

            if (!recipeCacheLoaded) {
                int before2 = recipeOverrides.size();
                loadRecipeFileFromClasspath("raw_copper_crushing_recipe.json");
                loadRecipeFileFromClasspath("raw_iron_crushing_recipe.json");
                loadRecipeFileFromClasspath("raw_gold_crushing_recipe.json");
                if (recipeOverrides.size() > before2) {
                    recipeCacheLoaded = true;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static void loadRecipeFile(ServerLevel level, String fileName) {
        try {
            MinecraftServer server = level.getServer();
            ResourceManager rm = server.getResourceManager();
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("dustandore", "recipe/" + fileName);
            java.util.Optional<Resource> resOpt = rm.getResource(rl);
            Resource res = resOpt.orElse(null);
            if (res == null)
                return;
            try (var in = res.open(); var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                String hammerId = root.has("hammer") ? root.get("hammer").getAsString() : "";
                boolean handled = false;
                JsonArray ingredients = root.has("ingredients") ? root.getAsJsonArray("ingredients") : null;
                if (ingredients != null) {
                    for (JsonElement el : ingredients) {
                        if (!el.isJsonObject())
                            continue;
                        JsonObject ing = el.getAsJsonObject();
                        if (!ing.has("item") || !ing.has("reward"))
                            continue;
                        String ingredientId = ing.get("item").getAsString();
                        JsonArray rewards = ing.getAsJsonArray("reward");
                        if (rewards.size() == 0)
                            continue;
                        JsonObject reward = rewards.get(0).getAsJsonObject();
                        if (!reward.has("item") || !reward.has("count_min") || !reward.has("count_max"))
                            continue;
                        String outputId = reward.get("item").getAsString();
                        int[] mins = readInt4(reward.get("count_min"));
                        int[] maxs = readInt4(reward.get("count_max"));
                        if (mins == null || maxs == null)
                            continue;
                        String key = hammerId + "|" + ingredientId;
                        recipeOverrides.put(key, new RecipeOverride(outputId, mins, maxs));
                        handled = true;
                    }
                }
                if (!handled) {
                    String t1 = root.has("type") ? root.get("type").getAsString() : null;
                    String t2 = root.has("fabric:type") ? root.get("fabric:type").getAsString() : null;
                    if ("dustandore:hammer_crush".equals(t1) || "dustandore:hammer_crush".equals(t2)) {
                        String ingredientId = root.has("left_hand_item") ? root.get("left_hand_item").getAsString()
                                : "";
                        if (!ingredientId.isEmpty() && root.has("reward") && root.get("reward").isJsonObject()) {
                            JsonObject reward = root.getAsJsonObject("reward");
                            if (reward.has("item") && reward.has("count_min") && reward.has("count_max")) {
                                String outputId = reward.get("item").getAsString();
                                int[] mins = readInt4(reward.get("count_min"));
                                int[] maxs = readInt4(reward.get("count_max"));
                                if (mins != null && maxs != null) {
                                    String key1 = hammerId + "|" + ingredientId;
                                    String key2 = "|" + ingredientId;
                                    recipeOverrides.put(key1, new RecipeOverride(outputId, mins, maxs));
                                    recipeOverrides.put(key2, new RecipeOverride(outputId, mins, maxs));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static void loadRecipeFile(ResourceManager rm, String fileName) {
        try {
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath("dustandore", "recipe/" + fileName);
            java.util.Optional<Resource> resOpt = rm.getResource(rl);
            Resource res = resOpt.orElse(null);
            if (res == null)
                return;
            try (var in = res.open(); var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                String hammerId = root.has("hammer") ? root.get("hammer").getAsString() : "";
                boolean handled = false;
                JsonArray ingredients = root.has("ingredients") ? root.getAsJsonArray("ingredients") : null;
                if (ingredients != null) {
                    for (JsonElement el : ingredients) {
                        if (!el.isJsonObject())
                            continue;
                        JsonObject ing = el.getAsJsonObject();
                        if (!ing.has("item") || !ing.has("reward"))
                            continue;
                        String ingredientId = ing.get("item").getAsString();
                        JsonArray rewards = ing.getAsJsonArray("reward");
                        if (rewards.size() == 0)
                            continue;
                        JsonObject reward = rewards.get(0).getAsJsonObject();
                        if (!reward.has("item") || !reward.has("count_min") || !reward.has("count_max"))
                            continue;
                        String outputId = reward.get("item").getAsString();
                        int[] mins = readInt4(reward.get("count_min"));
                        int[] maxs = readInt4(reward.get("count_max"));
                        if (mins == null || maxs == null)
                            continue;
                        String key = hammerId + "|" + ingredientId;
                        recipeOverrides.put(key, new RecipeOverride(outputId, mins, maxs));
                        handled = true;
                    }
                }
                if (!handled) {
                    String t1 = root.has("type") ? root.get("type").getAsString() : null;
                    String t2 = root.has("fabric:type") ? root.get("fabric:type").getAsString() : null;
                    if ("dustandore:hammer_crush".equals(t1) || "dustandore:hammer_crush".equals(t2)) {
                        String ingredientId = root.has("left_hand_item") ? root.get("left_hand_item").getAsString()
                                : "";
                        if (!ingredientId.isEmpty() && root.has("reward") && root.get("reward").isJsonObject()) {
                            JsonObject reward = root.getAsJsonObject("reward");
                            if (reward.has("item") && reward.has("count_min") && reward.has("count_max")) {
                                String outputId = reward.get("item").getAsString();
                                int[] mins = readInt4(reward.get("count_min"));
                                int[] maxs = readInt4(reward.get("count_max"));
                                if (mins != null && maxs != null) {
                                    String key1 = hammerId + "|" + ingredientId;
                                    String key2 = "|" + ingredientId;
                                    recipeOverrides.put(key1, new RecipeOverride(outputId, mins, maxs));
                                    recipeOverrides.put(key2, new RecipeOverride(outputId, mins, maxs));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static void loadRecipeFileFromClasspath(String fileName) {
        String path = "data/dustandore/recipe/" + fileName;
        try (java.io.InputStream in = CrushingManager.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null)
                return;
            try (var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                String hammerId = root.has("hammer") ? root.get("hammer").getAsString() : "";
                boolean handled = false;
                JsonArray ingredients = root.has("ingredients") ? root.getAsJsonArray("ingredients") : null;
                if (ingredients != null) {
                    for (JsonElement el : ingredients) {
                        if (!el.isJsonObject())
                            continue;
                        JsonObject ing = el.getAsJsonObject();
                        if (!ing.has("item") || !ing.has("reward"))
                            continue;
                        String ingredientId = ing.get("item").getAsString();
                        JsonArray rewards = ing.getAsJsonArray("reward");
                        if (rewards.size() == 0)
                            continue;
                        JsonObject reward = rewards.get(0).getAsJsonObject();
                        if (!reward.has("item") || !reward.has("count_min") || !reward.has("count_max"))
                            continue;
                        String outputId = reward.get("item").getAsString();
                        int[] mins = readInt4(reward.get("count_min"));
                        int[] maxs = readInt4(reward.get("count_max"));
                        if (mins == null || maxs == null)
                            continue;
                        String key = hammerId + "|" + ingredientId;
                        recipeOverrides.put(key, new RecipeOverride(outputId, mins, maxs));
                        handled = true;
                    }
                }
                if (!handled) {
                    String t1 = root.has("type") ? root.get("type").getAsString() : null;
                    String t2 = root.has("fabric:type") ? root.get("fabric:type").getAsString() : null;
                    if ("dustandore:hammer_crush".equals(t1) || "dustandore:hammer_crush".equals(t2)) {
                        String ingredientId = root.has("left_hand_item") ? root.get("left_hand_item").getAsString()
                                : "";
                        if (!ingredientId.isEmpty() && root.has("reward") && root.get("reward").isJsonObject()) {
                            JsonObject reward = root.getAsJsonObject("reward");
                            if (reward.has("item") && reward.has("count_min") && reward.has("count_max")) {
                                String outputId = reward.get("item").getAsString();
                                int[] mins = readInt4(reward.get("count_min"));
                                int[] maxs = readInt4(reward.get("count_max"));
                                if (mins != null && maxs != null) {
                                    String key1 = hammerId + "|" + ingredientId;
                                    String key2 = "|" + ingredientId;
                                    recipeOverrides.put(key1, new RecipeOverride(outputId, mins, maxs));
                                    recipeOverrides.put(key2, new RecipeOverride(outputId, mins, maxs));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static int[] readInt4(JsonElement arrEl) {
        if (arrEl == null || !arrEl.isJsonArray())
            return null;
        JsonArray arr = arrEl.getAsJsonArray();
        if (arr.size() < 4)
            return null;
        int[] out = new int[4];
        for (int i = 0; i < 4; i++)
            out[i] = arr.get(i).getAsInt();
        return out;
    }

    private static RecipeOverride findOverride(Item hammer, Item ingredient) {
        String k1 = HammerUtilities.idOf(hammer) + "|" + HammerUtilities.idOf(ingredient);
        RecipeOverride ov = recipeOverrides.get(k1);
        if (ov != null)
            return ov;
        String k2 = "|" + HammerUtilities.idOf(ingredient);
        return recipeOverrides.get(k2);
    }

    public static ItemStack getCrushingResult(LevelAccessor world, Entity entity) {
        if (!(entity instanceof Player player)) {
            return null;
        }
        Item hand = player.getMainHandItem().getItem();
        Item offhand = player.getOffhandItem().getItem();

        int canCrushResult = HammerUtilities.canCrush(hand, offhand);
        if (canCrushResult != 1) {
            return null;
        }

        if (!HammerUtilities.isCrushingHammer(hand)) {
            return null;
        }

        RecipeManager mgr = getRecipeManagerAnySide(world);
        if (mgr == null) {
            return null;
        }
        int fortuneLevel = EnchantmentUtilities.getLevel(world, player, "minecraft:fortune");
        int idx = Math.min(Math.max(fortuneLevel, 0), 3);
        String ingredientId = HammerUtilities.idOf(offhand);
        int hammerPower = PowerLoader.getCrushPower(world, hand);
        for (RecipeHolder<?> holderAny : mgr.getRecipes()) {
            Recipe<?> rcp = holderAny.value();
            if (rcp.getType() != RecipeTypes.HAMMER_CRUSH)
                continue;
            HammerCrushRecipe r = (HammerCrushRecipe) rcp;
            if (!ingredientId.equals(r.getLeftHandItemId()))
                continue;
            if (hammerPower < r.getCrushPowerRequired())
                continue;
            if (r.getRewards() == null || r.getRewards().isEmpty())
                continue;
            RewardEntry rw = r.getRewards().get(0);
            int min = rw.countMin[idx];
            int max = rw.countMax[idx];
            if (min <= 0 || max < min)
                continue;
            Holder.Reference<Item> outHolder = BuiltInRegistries.ITEM
                    .get(ResourceLocation.parse(rw.itemId))
                    .orElse(null);
            Item outItem = outHolder != null ? outHolder.value() : null;
            if (outItem == null)
                continue;
            int count = GetRandomNumber(min, max);
            if (count > 0) {
                return new ItemStack(outItem, count);
            }
        }
        return null;
    }

    public static int getJsonDropRange(Item hammer, String ingredientId, int fortuneLevel, String type) {
        ensureRecipeCacheAnySide();
        int idx = Math.min(Math.max(fortuneLevel, 0), 3);
        String hammerId = HammerUtilities.idOf(hammer);
        RecipeOverride ov = recipeOverrides.get(hammerId + "|" + ingredientId);
        if (ov == null)
            ov = recipeOverrides.get("|" + ingredientId);
        if (ov == null)
            return 0;
        return "min".equals(type) ? ov.countMin[idx] : ov.countMax[idx];
    }

    public static java.util.List<DropEntry> getJsonDropsForHammer(Item hammer, int fortuneLevel,
            boolean respectLoadedMods) {
        java.util.List<DropEntry> list = new java.util.ArrayList<>();
        int idx = Math.min(Math.max(fortuneLevel, 0), 3);
        try {
            try {
                Minecraft mc = null;
                try {
                    mc = Minecraft.getInstance();
                } catch (Throwable t0) {
                    mc = null;
                }
                if (mc != null) {
                    ResourceManager rm = null;
                    try {
                        java.lang.reflect.Method m2 = mc.getClass().getMethod("getSingleplayerServer");
                        Object srv = m2.invoke(mc);
                        if (srv != null) {
                            try {
                                java.lang.reflect.Method mRM = srv.getClass().getMethod("getResourceManager");
                                rm = (ResourceManager) mRM.invoke(srv);
                            } catch (Throwable ignoredRM) {
                            }
                        }
                    } catch (Throwable ignoredSS) {
                    }
                    if (rm == null) {
                        rm = mc.getResourceManager();
                    }
                    if (rm != null) {
                        java.util.Map<ResourceLocation, Resource> all = rm.listResources("recipes",
                                rl -> rl.getPath().endsWith(".json"));
                        int hammerPower2 = PowerLoader.getCrushPower(hammer);
                        for (java.util.Map.Entry<ResourceLocation, Resource> e : all.entrySet()) {
                            try (java.io.InputStream in = e.getValue().open();
                                    java.io.InputStreamReader rdr = new java.io.InputStreamReader(in,
                                            java.nio.charset.StandardCharsets.UTF_8)) {
                                JsonElement el = JsonParser.parseReader(rdr);
                                if (!el.isJsonObject())
                                    continue;
                                JsonObject obj = el.getAsJsonObject();
                                String t1 = obj.has("type") ? obj.get("type").getAsString() : null;
                                String t2 = obj.has("fabric:type") ? obj.get("fabric:type").getAsString() : null;
                                if (!"dustandore:hammer_crush".equals(t1) && !"dustandore:hammer_crush".equals(t2))
                                    continue;
                                DataResult<HammerCrushRecipe> dr = HammerCrushRecipe.CODEC
                                        .codec().parse(com.mojang.serialization.JsonOps.INSTANCE, obj);
                                HammerCrushRecipe rj = dr.result().orElse(null);
                                if (rj == null)
                                    continue;
                                if (hammerPower2 < rj.getCrushPowerRequired())
                                    continue;
                                String ingr = rj.getLeftHandItemId();
                                if (ingr == null || ingr.isEmpty())
                                    continue;
                                if (rj.getRewards() == null || rj.getRewards().isEmpty())
                                    continue;
                                HammerCrushRecipe.RewardEntry rwj = rj.getRewards().get(0);
                                net.minecraft.core.Holder.Reference<net.minecraft.world.item.Item> ingHolder = net.minecraft.core.registries.BuiltInRegistries.ITEM
                                        .get(net.minecraft.resources.ResourceLocation.parse(ingr)).orElse(null);
                                if (ingHolder == null)
                                    continue;
                                net.minecraft.core.Holder.Reference<net.minecraft.world.item.Item> outHolder = net.minecraft.core.registries.BuiltInRegistries.ITEM
                                        .get(net.minecraft.resources.ResourceLocation.parse(rwj.itemId)).orElse(null);
                                if (outHolder == null)
                                    continue;
                                int min2 = rwj.countMin[idx];
                                int max2 = rwj.countMax[idx];
                                double chance = rwj.getChanceForIndex(idx);
                                list.add(new DropEntry(ingr, rwj.itemId, min2, max2, chance));
                            } catch (Throwable ignoreOne) {
                            }
                        }
                    }
                }
            } catch (Throwable ignoredScan) {
            }

            if (list.isEmpty()) {
                RecipeManager mgr = getRecipeManagerClient();
                if (mgr != null) {
                    int hammerPower = PowerLoader.getCrushPower(hammer);
                    for (RecipeHolder<?> holderAny : mgr.getRecipes()) {
                        net.minecraft.world.item.crafting.Recipe<?> rcp = holderAny.value();
                        boolean isCrush = rcp.getType() == RecipeTypes.HAMMER_CRUSH;
                        if (!isCrush) {
                            ResourceLocation keyX = BuiltInRegistries.RECIPE_SERIALIZER.getKey(rcp.getSerializer());
                            isCrush = (keyX != null && "dustandore".equals(keyX.getNamespace())
                                    && "hammer_crush".equals(keyX.getPath()));
                        }
                        if (!isCrush)
                            continue;
                        HammerCrushRecipe r = (HammerCrushRecipe) rcp;
                        if (hammerPower < r.getCrushPowerRequired())
                            continue;
                        String ingredientId = r.getLeftHandItemId();
                        if (ingredientId == null || ingredientId.isEmpty())
                            continue;
                        if (r.getRewards() == null || r.getRewards().isEmpty())
                            continue;
                        HammerCrushRecipe.RewardEntry rw = r.getRewards().get(0);

                        net.minecraft.core.Holder.Reference<net.minecraft.world.item.Item> ingHolder = net.minecraft.core.registries.BuiltInRegistries.ITEM
                                .get(net.minecraft.resources.ResourceLocation.parse(ingredientId)).orElse(null);
                        if (ingHolder == null)
                            continue;
                        net.minecraft.core.Holder.Reference<net.minecraft.world.item.Item> outHolder = net.minecraft.core.registries.BuiltInRegistries.ITEM
                                .get(net.minecraft.resources.ResourceLocation.parse(rw.itemId)).orElse(null);
                        if (outHolder == null)
                            continue;
                        int min = rw.countMin[idx];
                        int max = rw.countMax[idx];
                        double chance = rw.getChanceForIndex(idx);
                        list.add(new DropEntry(ingredientId, rw.itemId, min, max, chance));
                    }
                }
            }

            if (list.isEmpty() && (CLIENT_SUMMARY != null && !CLIENT_SUMMARY.isEmpty())) {
                int hammerPower = PowerLoader.getCrushPower(hammer);
                for (ClientSummary s : CLIENT_SUMMARY) {
                    if (hammerPower < s.requiredPower)
                        continue;
                    String ingredientId = s.ingredientId;
                    if (ingredientId == null || ingredientId.isEmpty())
                        continue;

                    net.minecraft.core.Holder.Reference<net.minecraft.world.item.Item> ingHolder = net.minecraft.core.registries.BuiltInRegistries.ITEM
                            .get(net.minecraft.resources.ResourceLocation.parse(ingredientId)).orElse(null);
                    if (ingHolder == null)
                        continue;
                    net.minecraft.core.Holder.Reference<net.minecraft.world.item.Item> outHolder = net.minecraft.core.registries.BuiltInRegistries.ITEM
                            .get(net.minecraft.resources.ResourceLocation.parse(s.outputItemId)).orElse(null);
                    if (outHolder == null)
                        continue;
                    int min = s.countMin[idx];
                    int max = s.countMax[idx];
                    list.add(new DropEntry(ingredientId, s.outputItemId, min, max, 1.0d));
                }
            }
        } catch (Throwable ignored) {
        }

        if (!list.isEmpty())
            return list;

        ensureRecipeCacheAnySide();
        String hammerId = HammerUtilities.idOf(hammer);
        String prefix = hammerId + "|";
        for (java.util.Map.Entry<String, RecipeOverride> e : recipeOverrides.entrySet()) {
            if (!e.getKey().startsWith(prefix))
                continue;
            String ingr = e.getKey().substring(prefix.length());
            RecipeOverride ov = e.getValue();
            int min = ov.countMin[idx];
            int max = ov.countMax[idx];
            list.add(new DropEntry(ingr, ov.outputItemId, min, max, 1.0d));
        }
        RecipeOverride any;
        for (java.util.Map.Entry<String, RecipeOverride> e : recipeOverrides.entrySet()) {
            if (!e.getKey().startsWith("|"))
                continue;
            String ingr = e.getKey().substring(1);
            any = e.getValue();
            int min = any.countMin[idx];
            int max = any.countMax[idx];
            list.add(new DropEntry(ingr, any.outputItemId, min, max, 1.0d));
        }
        return list;
    }

    public static String getClientCrushDebug(Item hammer, int fortuneLevel, boolean respectLoadedMods) {
        try {
            net.minecraft.world.item.crafting.RecipeManager mgr = getRecipeManagerClient();
            int total = 0;
            int eligible = 0;
            int hammerPower = PowerLoader.getCrushPower(hammer);
            if (mgr != null) {
                for (RecipeHolder<?> holderAny : mgr.getRecipes()) {
                    net.minecraft.world.item.crafting.Recipe<?> rcp = holderAny.value();
                    boolean isCrush = rcp.getType() == RecipeTypes.HAMMER_CRUSH;
                    if (!isCrush) {
                        ResourceLocation keyX = BuiltInRegistries.RECIPE_SERIALIZER.getKey(rcp.getSerializer());
                        isCrush = (keyX != null && "dustandore".equals(keyX.getNamespace())
                                && "hammer_crush".equals(keyX.getPath()));
                    }
                    if (!isCrush)
                        continue;
                    total++;
                    HammerCrushRecipe r = (HammerCrushRecipe) rcp;
                    String ingredientId = r.getLeftHandItemId();
                    if (ingredientId == null || ingredientId.isEmpty())
                        continue;
                    if (hammerPower < r.getCrushPowerRequired())
                        continue;
                    if (r.getRewards() == null || r.getRewards().isEmpty())
                        continue;
                    eligible++;
                }

                int jsonTotal = 0;
                int jsonEligible = 0;
                try {
                    Minecraft mcDbg = Minecraft.getInstance();
                    if (mcDbg != null) {
                        ResourceManager rmDbg = null;
                        try {
                            java.lang.reflect.Method m2 = mcDbg.getClass().getMethod("getSingleplayerServer");
                            Object srv = m2.invoke(mcDbg);
                            if (srv != null) {
                                try {
                                    java.lang.reflect.Method mRM = srv.getClass().getMethod("getResourceManager");
                                    rmDbg = (ResourceManager) mRM.invoke(srv);
                                } catch (Throwable ignoredRM) {
                                }
                            }
                        } catch (Throwable ignoredSS) {
                        }
                        if (rmDbg == null) {
                            rmDbg = mcDbg.getResourceManager();
                        }
                        if (rmDbg != null) {
                            java.util.Map<ResourceLocation, Resource> allDbg = rmDbg.listResources("recipes",
                                    rl -> rl.getPath().endsWith(".json"));
                            for (java.util.Map.Entry<ResourceLocation, Resource> e : allDbg.entrySet()) {
                                try (java.io.InputStream in = e.getValue().open();
                                        java.io.InputStreamReader rdr = new java.io.InputStreamReader(in,
                                                java.nio.charset.StandardCharsets.UTF_8)) {
                                    JsonElement el = JsonParser.parseReader(rdr);
                                    if (!el.isJsonObject())
                                        continue;
                                    JsonObject obj = el.getAsJsonObject();
                                    String t1 = obj.has("type") ? obj.get("type").getAsString() : null;
                                    String t2 = obj.has("fabric:type") ? obj.get("fabric:type").getAsString() : null;
                                    if (!"dustandore:hammer_crush".equals(t1) && !"dustandore:hammer_crush".equals(t2))
                                        continue;
                                    jsonTotal++;
                                    com.mojang.serialization.DataResult<HammerCrushRecipe> dr = HammerCrushRecipe.CODEC
                                            .codec().parse(com.mojang.serialization.JsonOps.INSTANCE, obj);
                                    HammerCrushRecipe rj = dr.result().orElse(null);
                                    if (rj == null)
                                        continue;
                                    String ingredientId = rj.getLeftHandItemId();
                                    if (ingredientId == null || ingredientId.isEmpty())
                                        continue;
                                    if (hammerPower < rj.getCrushPowerRequired())
                                        continue;
                                    if (rj.getRewards() == null || rj.getRewards().isEmpty())
                                        continue;
                                    jsonEligible++;
                                } catch (Throwable ignoreOne) {
                                }
                            }
                        }
                    }
                } catch (Throwable ignoreDbg) {
                }

                if (eligible == 0) {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc != null && mc.level != null) {
                        net.minecraft.world.item.crafting.RecipeManager mgr2 = null;
                        try {
                            Object lvl = mc.level;
                            try {
                                java.lang.reflect.Method m = lvl.getClass().getMethod("getRecipeManager");
                                mgr2 = (net.minecraft.world.item.crafting.RecipeManager) m.invoke(lvl);
                            } catch (Throwable t1) {
                                try {
                                    java.lang.reflect.Field f = lvl.getClass().getDeclaredField("recipeManager");
                                    f.setAccessible(true);
                                    mgr2 = (net.minecraft.world.item.crafting.RecipeManager) f.get(lvl);
                                } catch (Throwable t2) {

                                }
                            }
                        } catch (Throwable ignored3) {
                        }
                        if (mgr2 != null) {
                            total = 0;
                            eligible = 0;
                            for (net.minecraft.world.item.crafting.RecipeHolder<?> holderAny : mgr2.getRecipes()) {
                                net.minecraft.world.item.crafting.Recipe<?> rcp = holderAny.value();
                                if (rcp.getType() != RecipeTypes.HAMMER_CRUSH)
                                    continue;
                                total++;
                                com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe r = (com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe) rcp;
                                String ingredientId = r.getLeftHandItemId();
                                if (ingredientId == null || ingredientId.isEmpty())
                                    continue;

                                if (hammerPower < r.getCrushPowerRequired())
                                    continue;
                                if (r.getRewards() == null || r.getRewards().isEmpty())
                                    continue;
                                eligible++;
                            }
                        }
                    }
                }
                return "mgr=ok,total=" + total + ",eligible=" + eligible + ",power=" + hammerPower + ",json="
                        + jsonTotal + ",jsonElig=" + jsonEligible;
            }
            try {
                Minecraft mc = Minecraft.getInstance();
                boolean hasConn = (mc != null && mc.getConnection() != null);
                boolean hasLevel = (mc != null && mc.level != null);
                int jsonTotal = 0;
                int jsonEligible = 0;
                try {
                    if (mc != null) {
                        ResourceManager rm = null;
                        try {
                            java.lang.reflect.Method m2 = mc.getClass().getMethod("getSingleplayerServer");
                            Object srv = m2.invoke(mc);
                            if (srv != null) {
                                try {
                                    java.lang.reflect.Method mRM = srv.getClass().getMethod("getResourceManager");
                                    rm = (ResourceManager) mRM.invoke(srv);
                                } catch (Throwable ignoredRM) {
                                }
                            }
                        } catch (Throwable ignoredSS) {
                        }
                        if (rm == null) {
                            rm = mc.getResourceManager();
                        }
                        if (rm != null) {
                            java.util.Map<ResourceLocation, Resource> all = rm.listResources("recipes",
                                    rl -> rl.getPath().endsWith(".json"));
                            for (java.util.Map.Entry<ResourceLocation, Resource> e : all.entrySet()) {
                                try (java.io.InputStream in = e.getValue().open();
                                        java.io.InputStreamReader rdr = new java.io.InputStreamReader(in,
                                                java.nio.charset.StandardCharsets.UTF_8)) {
                                    JsonElement el = JsonParser.parseReader(rdr);
                                    if (!el.isJsonObject())
                                        continue;
                                    JsonObject obj = el.getAsJsonObject();
                                    String t1 = obj.has("type") ? obj.get("type").getAsString() : null;
                                    String t2 = obj.has("fabric:type") ? obj.get("fabric:type").getAsString() : null;
                                    if (!"dustandore:hammer_crush".equals(t1) && !"dustandore:hammer_crush".equals(t2))
                                        continue;
                                    jsonTotal++;
                                    com.mojang.serialization.DataResult<HammerCrushRecipe> dr = HammerCrushRecipe.CODEC
                                            .codec().parse(com.mojang.serialization.JsonOps.INSTANCE, obj);
                                    HammerCrushRecipe r = dr.result().orElse(null);
                                    if (r == null)
                                        continue;
                                    String ingredientId = r.getLeftHandItemId();
                                    if (ingredientId == null || ingredientId.isEmpty())
                                        continue;
                                    if (hammerPower < r.getCrushPowerRequired())
                                        continue;
                                    if (r.getRewards() == null || r.getRewards().isEmpty())
                                        continue;
                                    jsonEligible++;
                                } catch (Throwable ignoreOne) {
                                }
                            }
                        }
                    }
                } catch (Throwable ignoredScan) {
                }
                return "mgr=null,conn=" + hasConn + ",level=" + hasLevel + ",power=" + hammerPower + ",json="
                        + jsonTotal + ",jsonElig=" + jsonEligible;
            } catch (Throwable t) {
                return "mgr=null";
            }
        } catch (Throwable t) {
            return "error:" + t.getClass().getSimpleName();
        }
    }

    public static RecipeManager getRecipeManagerAnySide(LevelAccessor world) {
        if (world instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            return serverLevel.getServer().getRecipeManager();
        }

        try {
            if (world instanceof net.minecraft.client.multiplayer.ClientLevel clientLevel) {
                try {
                    java.lang.reflect.Method m = clientLevel.getClass().getMethod("getRecipeManager");
                    Object mgr = m.invoke(clientLevel);
                    return (RecipeManager) mgr;
                } catch (Throwable t1) {
                    try {
                        java.lang.reflect.Field f = clientLevel.getClass().getDeclaredField("recipeManager");
                        f.setAccessible(true);
                        Object mgr = f.get(clientLevel);
                        return (RecipeManager) mgr;
                    } catch (Throwable t2) {

                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return getRecipeManagerClient();
    }

    private static RecipeManager getRecipeManagerClient() {
        try {
            Minecraft mc = null;
            try {
                mc = Minecraft.getInstance();
            } catch (Throwable t0) {
                mc = null;
            }
            if (mc == null)
                return null;

            try {
                Object levelObj = mc.level;
                if (levelObj != null) {
                    RecipeManager rm = reflectRecipeManager(levelObj);
                    if (rm != null)
                        return rm;
                }
            } catch (Throwable ignoredLevelRoot) {
            }
            Object conn = null;
            try {
                conn = mc.getConnection();
            } catch (Throwable t1) {
                conn = null;
            }
            if (conn != null) {
                if (conn instanceof net.minecraft.client.multiplayer.ClientPacketListener listener) {
                    RecipeManager rmListener = reflectRecipeManager(listener);
                    if (rmListener != null)
                        return rmListener;
                }
                RecipeManager rmConn = reflectRecipeManager(conn);
                if (rmConn != null)
                    return rmConn;
            }

            try {
                java.lang.reflect.Method m2 = mc.getClass().getMethod("getSingleplayerServer");
                Object srv = m2.invoke(mc);
                if (srv != null) {
                    RecipeManager rmSrv = reflectRecipeManager(srv);
                    if (rmSrv != null)
                        return rmSrv;
                }
            } catch (Throwable ignored1) {
            }
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static RecipeManager reflectRecipeManager(Object source) {
        if (source == null)
            return null;
        Class<?> cls = source.getClass();

        try {
            java.lang.reflect.Method m = cls.getMethod("getRecipeManager");
            Object rm = m.invoke(source);
            if (rm instanceof RecipeManager)
                return (RecipeManager) rm;
        } catch (Throwable ignored) {
        }

        try {
            for (java.lang.reflect.Method m : cls.getDeclaredMethods()) {
                if (m.getParameterCount() == 0 && RecipeManager.class.isAssignableFrom(m.getReturnType())) {
                    m.setAccessible(true);
                    Object rm = m.invoke(source);
                    if (rm instanceof RecipeManager)
                        return (RecipeManager) rm;
                }
            }
        } catch (Throwable ignoredAll) {
        }

        try {
            for (java.lang.reflect.Field f : cls.getDeclaredFields()) {
                if (RecipeManager.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    Object rm = f.get(source);
                    if (rm instanceof RecipeManager)
                        return (RecipeManager) rm;
                }
            }
        } catch (Throwable ignoredFields) {
        }
        return null;
    }

    public static void clearCaches() {
        HammerUtilities.clearCache();
        PowerLoader.clearCache();
        SpeedLoader.clearCache();
    }

    public static boolean shouldBlockPlacement(LevelAccessor world, Player player, ItemStack mainHand,
            ItemStack offhand) {
        if (player == null || world == null)
            return false;
        if (mainHand == null || offhand == null)
            return false;
        if (mainHand.isEmpty() || offhand.isEmpty())
            return false;
        if (!HammerUtilities.isCrushingHammer(mainHand.getItem()))
            return false;
        if (!(offhand.getItem() instanceof BlockItem))
            return false;

        if (Constants.LOG.isDebugEnabled()) {
            Constants.LOG.debug("shouldBlockPlacement: player={}, mainHand={}, offhand={}",
                    player.getName().getString(), mainHand, offhand);
        }

        RecipeManager recipeManager = getRecipeManagerAnySide(world);
        if (recipeManager == null)
            return false;

        int hammerPower = PowerLoader.getCrushPower(world, mainHand.getItem());
        if (hammerPower <= 0)
            return false;

        String ingredientId = HammerUtilities.idOf(offhand.getItem());
        if (ingredientId == null || ingredientId.isEmpty())
            return false;

        if (Constants.LOG.isDebugEnabled()) {
            Constants.LOG.debug("shouldBlockPlacement: hammerPower={}, ingredientId={}", hammerPower, ingredientId);
        }

        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            Recipe<?> recipe = holder.value();
            if (recipe.getType() != RecipeTypes.HAMMER_CRUSH)
                continue;
            HammerCrushRecipe hammerRecipe = (HammerCrushRecipe) recipe;
            if (!ingredientId.equals(hammerRecipe.getLeftHandItemId()))
                continue;
            if (hammerPower < hammerRecipe.getCrushPowerRequired())
                continue;
            if (Constants.LOG.isDebugEnabled()) {
                Constants.LOG.debug(
                        "shouldBlockPlacement: match found -> recipeId={}, requiredPower={}",
                        holder.id(), hammerRecipe.getCrushPowerRequired());
            }
            return true;
        }

        if (Constants.LOG.isDebugEnabled()) {
            Constants.LOG.debug("shouldBlockPlacement: no matching hammer recipe found");
        }

        return false;
    }

    private static int GetRandomNumber(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    public static java.util.List<ClientSummary> buildServerClientSummary(MinecraftServer server) {
        java.util.List<ClientSummary> summaries = new java.util.ArrayList<>();

        try {
            RecipeManager recipeManager = server.getRecipeManager();
            if (recipeManager == null) {
                Constants.LOG.warn("Recipe manager is null, cannot build server client summary");
                return summaries;
            }

            for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
                Recipe<?> recipe = holder.value();

                boolean isHammerCrush = false;
                if (recipe.getType() == RecipeTypes.HAMMER_CRUSH) {
                    isHammerCrush = true;
                } else {
                    ResourceLocation serializerKey = BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer());
                    isHammerCrush = (serializerKey != null && "dustandore".equals(serializerKey.getNamespace())
                            && "hammer_crush".equals(serializerKey.getPath()));
                }

                if (!isHammerCrush)
                    continue;

                HammerCrushRecipe hammerRecipe = (HammerCrushRecipe) recipe;

                String ingredientId = hammerRecipe.getLeftHandItemId();
                if (ingredientId == null || ingredientId.isEmpty())
                    continue;

                int requiredPower = hammerRecipe.getCrushPowerRequired();
                String requiredMod = hammerRecipe.getRequiredMod();

                if (requiredMod != null && !hammerRecipe.isRequiredModLoaded()) {
                    continue;
                }

                Holder.Reference<Item> ingHolder = BuiltInRegistries.ITEM
                        .get(ResourceLocation.parse(ingredientId)).orElse(null);
                if (ingHolder == null)
                    continue;

                List<RewardEntry> rewards = hammerRecipe.getRewards();
                if (rewards == null || rewards.isEmpty())
                    continue;

                for (RewardEntry reward : rewards) {
                    Holder.Reference<Item> outHolder = BuiltInRegistries.ITEM
                            .get(ResourceLocation.parse(reward.itemId)).orElse(null);
                    if (outHolder == null)
                        continue;

                    ClientSummary summary = new ClientSummary(
                            ingredientId,
                            requiredPower,
                            reward.itemId,
                            reward.countMin.clone(),
                            reward.countMax.clone(),
                            requiredMod);

                    summaries.add(summary);

                    if (Constants.LOG.isDebugEnabled()) {
                        Constants.LOG.debug("Built server client summary: {} -> {} (power: {})",
                                ingredientId, reward.itemId, requiredPower);
                    }
                }
            }

            Constants.LOG.info("Built {} client summaries from server recipes", summaries.size());

        } catch (Throwable t) {
            Constants.LOG.error("Failed to build server client summary", t);
        }

        return summaries;
    }

    public static String getDustItemIdForIngredient(net.minecraft.world.item.Item ingredient) {
        try {
            net.minecraft.world.item.crafting.RecipeManager mgr = getRecipeManagerClient();
            if (mgr == null)
                return null;

            String ingredientId = HammerUtilities.idOf(ingredient);
            if (ingredientId == null || ingredientId.isEmpty())
                return null;

            for (net.minecraft.world.item.crafting.RecipeHolder<?> holderAny : mgr.getRecipes()) {
                net.minecraft.world.item.crafting.Recipe<?> rcp = holderAny.value();

                boolean isCrush = rcp.getType() == RecipeTypes.HAMMER_CRUSH;
                if (!isCrush) {
                    net.minecraft.resources.ResourceLocation keyX = BuiltInRegistries.RECIPE_SERIALIZER
                            .getKey(rcp.getSerializer());
                    isCrush = (keyX != null
                            && "dustandore".equals(keyX.getNamespace())
                            && "hammer_crush".equals(keyX.getPath()));
                }
                if (!isCrush)
                    continue;

                HammerCrushRecipe r = (HammerCrushRecipe) rcp;

                if (!r.isRequiredModLoaded())
                    continue;

                if (!ingredientId.equals(r.getLeftHandItemId()))
                    continue;
                if (r.getRewards() == null || r.getRewards().isEmpty())
                    continue;

                HammerCrushRecipe.RewardEntry rw = r.getRewards().get(0);
                if (rw.itemId == null || rw.itemId.isEmpty())
                    continue;

                return rw.itemId;
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

}
