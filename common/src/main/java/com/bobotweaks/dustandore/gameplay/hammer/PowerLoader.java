package com.bobotweaks.dustandore.gameplay.hammer;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;

import com.bobotweaks.dustandore.gameplay.hammer.HammerUtilities;
import com.bobotweaks.dustandore.core.CrushingManager;

public class PowerLoader {
    private static final java.util.Map<String, Integer> HAMMER_POWER_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public static void clearCache() {
        try {
            HAMMER_POWER_CACHE.clear();
        } catch (Throwable ignored) {
        }
    }

    public static void putHammerPowerCache(String itemId, int power) {
        if (itemId == null || itemId.isEmpty())
            return;
        HAMMER_POWER_CACHE.put(itemId, Math.max(0, power));
    }

    public static int getCrushPower(LevelAccessor world, Item item) {
        String id = HammerUtilities.idOf(item);
        if (id == null)
            return 1;
        Integer cached = HAMMER_POWER_CACHE.get(id);
        if (cached != null)
            return cached;
        int def = 1;
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
                        if (obj.has("crush_power")) {
                            int val = Math.max(0, obj.get("crush_power").getAsInt());
                            HAMMER_POWER_CACHE.put(id, val);
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
                                if (obj2.has("crush_power")) {
                                    int val2 = Math.max(0, obj2.get("crush_power").getAsInt());
                                    HAMMER_POWER_CACHE.put(id, val2);
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
        HAMMER_POWER_CACHE.put(id, def);
        return def;
    }

    public static int getCrushPower(Item item) {
        String id = HammerUtilities.idOf(item);
        if (id == null)
            return 1;
        Integer cached = HAMMER_POWER_CACHE.get(id);
        if (cached != null)
            return cached;
        int def = 1;
        try {
            net.minecraft.client.Minecraft mc = null;
            try {
                mc = net.minecraft.client.Minecraft.getInstance();
            } catch (Throwable t) {
                mc = null;
            }
            if (mc != null && mc.getResourceManager() != null) {
                ResourceManager rm = mc.getResourceManager();
                net.minecraft.resources.ResourceLocation itemRL = net.minecraft.resources.ResourceLocation
                        .parse(id);
                String propsPath = itemRL.getNamespace() + ":" + "properties/" + itemRL.getPath() + ".json";
                net.minecraft.resources.ResourceLocation propsRL = net.minecraft.resources.ResourceLocation
                        .parse(propsPath);
                try {
                    java.util.Optional<net.minecraft.server.packs.resources.Resource> resOpt = rm
                            .getResource(propsRL);
                    if (resOpt.isPresent()) {
                        try (java.io.InputStream in = resOpt.get().open();
                                java.io.InputStreamReader rdr = new java.io.InputStreamReader(in,
                                        java.nio.charset.StandardCharsets.UTF_8)) {
                            com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseReader(rdr)
                                    .getAsJsonObject();
                            if (obj.has("crush_power")) {
                                int val = Math.max(0, obj.get("crush_power").getAsInt());
                                HAMMER_POWER_CACHE.put(id, val);
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
                                    if (obj2.has("crush_power")) {
                                        int val2 = Math.max(0, obj2.get("crush_power").getAsInt());
                                        HAMMER_POWER_CACHE.put(id, val2);
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
        } catch (Throwable ignored) {
        }
        HAMMER_POWER_CACHE.put(id, def);
        return def;
    }
}
