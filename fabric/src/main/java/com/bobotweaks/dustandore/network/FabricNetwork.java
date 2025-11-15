package com.bobotweaks.dustandore.network;

import com.bobotweaks.dustandore.network.CrushingSummaryPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class FabricNetwork {
    private static volatile boolean TYPES_REGISTERED = false;

    public static void registerPayloadTypes() {
        if (TYPES_REGISTERED)
            return;
        TYPES_REGISTERED = true;

        PayloadTypeRegistry.playS2C().register(
                CrushingSummaryPayload.TYPE,
                CrushingSummaryPayload.STREAM_CODEC);
    }

    public static void initServer() {
        // No-op
    }

    public static void initClient() {
        // No-op
    }
}
