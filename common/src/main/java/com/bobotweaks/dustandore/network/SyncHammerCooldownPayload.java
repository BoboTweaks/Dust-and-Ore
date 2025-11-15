package com.bobotweaks.dustandore.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncHammerCooldownPayload(int slotIndex, int durationTicks, long endTick) implements CustomPacketPayload {
    public static final Type<SyncHammerCooldownPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("dustandore", "sync_hammer_cooldown")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncHammerCooldownPayload> CODEC = new StreamCodec<>() {
        @Override
        public SyncHammerCooldownPayload decode(RegistryFriendlyByteBuf buf) {
            int slot = buf.readVarInt();
            int dur = buf.readVarInt();
            long end = buf.readVarLong();
            return new SyncHammerCooldownPayload(slot, dur, end);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, SyncHammerCooldownPayload value) {
            buf.writeVarInt(value.slotIndex());
            buf.writeVarInt(value.durationTicks());
            buf.writeVarLong(value.endTick());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
