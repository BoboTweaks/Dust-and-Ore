package com.bobotweaks.dustandore.gameplay.hammer;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
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

import com.bobotweaks.dustandore.core.CrushingManager;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;

import com.bobotweaks.dustandore.gameplay.hammer.HammerUtilities;

public class SpeedLoader {
    private static final java.util.Map<String, Float> HAMMER_SPEED_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public static void clearCache() {
        try {
            HAMMER_SPEED_CACHE.clear();
        } catch (Throwable ignored) {
        }
    }

    public static float effectiveSpeed(float speedBase, int efficincyLevel) {
        return Math.max(0.001f, speedBase * (1.0f + efficincyLevel * 0.20f));
    }

    public static float getHammerSpeedBase(LevelAccessor world, Item item) {
        String id = HammerUtilities.idOf(item);
        if (id == null)
            return 1.0f;
        Float cached = HAMMER_SPEED_CACHE.get(id);
        if (cached != null)
            return cached;
        float def = 1.0f;
        if (world instanceof ServerLevel serverLevel) {
            net.minecraft.server.packs.resources.ResourceManager rm = serverLevel.getServer().getResourceManager();
            net.minecraft.resources.ResourceLocation itemRL = net.minecraft.resources.ResourceLocation.parse(id);
            String propsPath = itemRL.getNamespace() + ":" + "properties/" + itemRL.getPath() + ".json";
            net.minecraft.resources.ResourceLocation propsRL = net.minecraft.resources.ResourceLocation
                    .parse(propsPath);
            try {
                java.util.Optional<net.minecraft.server.packs.resources.Resource> resOpt = rm.getResource(propsRL);
                if (resOpt.isPresent()) {
                    try (java.io.InputStream in = resOpt.get().open();
                            java.io.InputStreamReader rdr = new java.io.InputStreamReader(in,
                                    java.nio.charset.StandardCharsets.UTF_8)) {
                        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseReader(rdr).getAsJsonObject();
                        if (obj.has("speed_base")) {
                            float val = Math.max(0.0f, obj.get("speed_base").getAsFloat());
                            HAMMER_SPEED_CACHE.put(id, val);
                            return val;
                        }
                    }
                }

                if (!resOpt.isPresent()) {
                    String dataPath = "data/" + itemRL.getNamespace() + "/properties/" + itemRL.getPath() + ".json";
                    try (java.io.InputStream in2 = CrushingManager.class.getClassLoader()
                            .getResourceAsStream(dataPath)) {
                        if (in2 != null) {
                            try (java.io.InputStreamReader rdr2 = new java.io.InputStreamReader(in2,
                                    java.nio.charset.StandardCharsets.UTF_8)) {
                                com.google.gson.JsonObject obj2 = com.google.gson.JsonParser.parseReader(rdr2)
                                        .getAsJsonObject();
                                if (obj2.has("speed_base")) {
                                    float val2 = Math.max(0.0f, obj2.get("speed_base").getAsFloat());
                                    HAMMER_SPEED_CACHE.put(id, val2);
                                    return val2;
                                }
                            }
                        }
                    } catch (Exception ignored2) {
                    }
                }
            } catch (Exception ignored) {
            }
        }
        HAMMER_SPEED_CACHE.put(id, def);
        return def;
    }
}
