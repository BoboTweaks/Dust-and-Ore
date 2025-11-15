package com.bobotweaks.dustandore.core;

import com.bobotweaks.dustandore.platform.Services;

public class Utilities {

    public enum ModLoader {
        FORGE,
        FABRIC,
        NEOFORGE,
        UNKNOWN
    }

    public static ModLoader getCurrentModLoader() {

        if (isNeoForge()) {
            return ModLoader.NEOFORGE;
        }

        if (isFabric()) {
            return ModLoader.FABRIC;
        }

        if (isForge()) {
            return ModLoader.FORGE;
        }

        return ModLoader.UNKNOWN;
    }

    public static boolean isNeoForge() {
        try {
            Class.forName("net.neoforged.fml.loading.FMLLoader");
            Class.forName("net.neoforged.neoforge.common.NeoForge");
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
    }

    public static boolean isFabric() {
        try {
            Class.forName("net.fabricmc.loader.api.FabricLoader");
            Class.forName("net.fabricmc.api.EnvType");
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
    }

    public static boolean isForge() {
        try {
            Class.forName("net.minecraftforge.fml.loading.FMLLoader");
            Class.forName("net.minecraftforge.common.MinecraftForge");
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
    }

    public static String getModLoaderName() {
        return getCurrentModLoader().name().toLowerCase();
    }

    public static boolean isDevelopmentEnvironment() {
        try {
            return Boolean.parseBoolean(System.getProperty("fabric.development", "false")) ||
                    Boolean.parseBoolean(System.getProperty("forge.enableGameTest", "false")) ||
                    Boolean.parseBoolean(System.getProperty("neoforge.enableGameTest", "false"));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isClientSide() {
        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            Object instance = minecraftClass.getMethod("getInstance").invoke(null);
            return instance != null;
        } catch (Exception e) {
            String threadName = Thread.currentThread().getName().toLowerCase();
            return threadName.contains("client") || threadName.contains("render");
        }
    }

    public static boolean isModLoaded(String modId) {
        try {
            return Services.PLATFORM.isModLoaded(modId);
        } catch (Throwable ignored) {
            return false;
        }
    }
}