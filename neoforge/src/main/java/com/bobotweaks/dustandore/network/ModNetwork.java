package com.bobotweaks.dustandore.network;

public final class ModNetwork {
    private static volatile boolean TYPES_REGISTERED = false;

    public static void registerPayloadTypes() {

        TYPES_REGISTERED = true;
    }

    public static void initServer() {

    }

    public static void initClient() {

    }
}
