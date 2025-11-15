package com.bobotweaks.dustandore.network;

import com.bobotweaks.dustandore.core.CrushingManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record SyncHammerSummaryPayload(List<CrushingManager.ClientSummary> list) implements CustomPacketPayload {
    public static final Type<SyncHammerSummaryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("dustandore", "sync_hammer_summary")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncHammerSummaryPayload> CODEC = new StreamCodec<>() {
        @Override
        public SyncHammerSummaryPayload decode(RegistryFriendlyByteBuf buf) {
            int n = buf.readVarInt();
            List<CrushingManager.ClientSummary> out = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                String ing = buf.readUtf();
                int req = buf.readVarInt();
                String outId = buf.readUtf();
                int[] mins = new int[4];
                int[] maxs = new int[4];
                for (int k = 0; k < 4; k++) mins[k] = buf.readVarInt();
                for (int k = 0; k < 4; k++) maxs[k] = buf.readVarInt();
                String requiredMod = buf.readNullable(friendlyBuf -> friendlyBuf.readUtf(32767));
                out.add(new CrushingManager.ClientSummary(ing, req, outId, mins, maxs, requiredMod));
            }
            return new SyncHammerSummaryPayload(out);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, SyncHammerSummaryPayload value) {
            List<CrushingManager.ClientSummary> list = value.list();
            buf.writeVarInt(list.size());
            for (CrushingManager.ClientSummary s : list) {
                buf.writeUtf(s.ingredientId);
                buf.writeVarInt(s.requiredPower);
                buf.writeUtf(s.outputItemId);
                for (int i = 0; i < 4; i++) buf.writeVarInt(s.countMin[i]);
                for (int i = 0; i < 4; i++) buf.writeVarInt(s.countMax[i]);
                buf.writeNullable(s.requiredMod, (friendlyBuf, val) -> friendlyBuf.writeUtf(val));
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
