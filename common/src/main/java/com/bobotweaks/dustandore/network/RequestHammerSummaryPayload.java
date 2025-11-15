package com.bobotweaks.dustandore.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestHammerSummaryPayload() implements CustomPacketPayload {
    public static final Type<RequestHammerSummaryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("dustandore", "req_hammer_summary")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestHammerSummaryPayload> CODEC =
            StreamCodec.unit(new RequestHammerSummaryPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
