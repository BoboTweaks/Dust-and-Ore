package com.bobotweaks.dustandore.network;

import com.bobotweaks.dustandore.core.CrushingManager;
import com.bobotweaks.dustandore.core.CrushingManager.ClientSummary;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record CrushingSummaryPayload(List<ClientSummary> summaries) implements CustomPacketPayload {
    public static final Type<CrushingSummaryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("dustandore", "crushing_summary"));

    public CrushingSummaryPayload(FriendlyByteBuf buf) {
        this(decode(buf));
    }

    private static List<ClientSummary> decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ClientSummary> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String ingredientId = buf.readUtf();
            int requiredPower = buf.readInt();
            String outputItemId = buf.readUtf();
            int[] min = new int[4];
            int[] max = new int[4];
            for (int j = 0; j < 4; j++)
                min[j] = buf.readVarInt();
            for (int j = 0; j < 4; j++)
                max[j] = buf.readVarInt();
            String requiredMod = null;
            if (buf.readBoolean())
                requiredMod = buf.readUtf();
            list.add(new ClientSummary(ingredientId, requiredPower, outputItemId, min, max, requiredMod));
        }
        return list;
    }

    public void encode(FriendlyByteBuf buf) {
        List<ClientSummary> list = this.summaries;
        buf.writeVarInt(list.size());
        for (ClientSummary s : list) {
            buf.writeUtf(s.ingredientId);
            buf.writeInt(s.requiredPower);
            buf.writeUtf(s.outputItemId);
            for (int i = 0; i < 4; i++)
                buf.writeVarInt(s.countMin[i]);
            for (int i = 0; i < 4; i++)
                buf.writeVarInt(s.countMax[i]);
            buf.writeBoolean(s.requiredMod != null);
            if (s.requiredMod != null)
                buf.writeUtf(s.requiredMod);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, CrushingSummaryPayload> STREAM_CODEC =

            new StreamCodec<FriendlyByteBuf, CrushingSummaryPayload>() {
                @Override
                public CrushingSummaryPayload decode(FriendlyByteBuf buf) {
                    return new CrushingSummaryPayload(buf);
                }

                @Override
                public void encode(FriendlyByteBuf buf, CrushingSummaryPayload payload) {
                    payload.encode(buf);
                }
            };
}