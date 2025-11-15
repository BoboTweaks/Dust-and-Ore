package com.bobotweaks.dustandore.network;

import com.bobotweaks.dustandore.core.CrushingManager.ClientSummary;
import com.bobotweaks.dustandore.Constants;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record FabricCrushingSummaryPayload(List<ClientSummary> summaries)
        implements CustomPacketPayload {

    public static final Type<FabricCrushingSummaryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "crushing_summary"));

    public static final StreamCodec<FriendlyByteBuf, FabricCrushingSummaryPayload> STREAM_CODEC = CustomPacketPayload
            .codec(
                    (payload, buf) -> {
                        List<ClientSummary> list = payload.summaries();
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
                    },
                    buf -> {
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
                            list.add(new ClientSummary(ingredientId, requiredPower, outputItemId, min, max,
                                    requiredMod));
                        }
                        return new FabricCrushingSummaryPayload(list);
                    });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}