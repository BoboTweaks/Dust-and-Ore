package com.bobotweaks.dustandore.gameplay.hammer;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

import com.bobotweaks.dustandore.core.CrushingManager;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;

public class HammerUtilities {
    private static final java.util.Map<String, Boolean> HAS_PROPS_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public static void clearCache() {
        try {
            HAS_PROPS_CACHE.clear();
        } catch (Throwable ignored) {
        }
    }

    public static boolean isCrushingHammer(Item item) {
        String id = idOf(item);
        if (id == null || id.isEmpty())
            return false;
        return hasHammerProperties(id);
    }

    private static boolean hasHammerProperties(String itemId) {
        Boolean cached = HAS_PROPS_CACHE.get(itemId);
        if (cached != null)
            return cached.booleanValue();
        boolean found = false;
        try {
            String path = "data/dustandore/properties/"
                    + net.minecraft.resources.ResourceLocation.parse(itemId).getPath() + ".json";
            try (java.io.InputStream in = CrushingManager.class.getClassLoader().getResourceAsStream(path)) {
                if (in != null) {
                    try (java.io.InputStreamReader rdr = new java.io.InputStreamReader(in,
                            java.nio.charset.StandardCharsets.UTF_8)) {
                        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseReader(rdr).getAsJsonObject();
                        String type = obj.has("type") ? obj.get("type").getAsString() : null;
                        if ("dustandore:righthand_properties".equals(type)) {
                            found = true;
                        }
                    }
                }
            } catch (Throwable ignored2) {

            }
            if (!found) {
                Minecraft mc = null;
                try {
                    mc = Minecraft.getInstance();
                } catch (Throwable t) {
                    mc = null;
                }
                if (mc != null && mc.getResourceManager() != null) {
                    ResourceManager rm = mc.getResourceManager();
                    net.minecraft.resources.ResourceLocation itemRL = net.minecraft.resources.ResourceLocation
                            .parse(itemId);
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
                                String type = obj.has("type") ? obj.get("type").getAsString() : null;
                                if ("dustandore:righthand_properties".equals(type)) {
                                    found = true;
                                }
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }
        } catch (Throwable ignored3) {
        }
        HAS_PROPS_CACHE.put(itemId, Boolean.valueOf(found));
        return found;
    }

    public static int canCrush(Item hand, Item offhand) {
        if (!isCrushingHammer(hand))
            return 0;
        if (offhand == null)
            return -10;
        return 1;
    }

    public static String idOf(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return id == null ? "" : id.toString();
    }
}
