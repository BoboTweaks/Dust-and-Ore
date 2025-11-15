package com.bobotweaks.dustandore.network;

import com.bobotweaks.dustandore.core.CrushingManager;
import com.bobotweaks.dustandore.core.CrushingManager.ClientSummary;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class FabricCrushingSummaryPacket {
    private final List<ClientSummary> summaries;

    public FabricCrushingSummaryPacket(List<ClientSummary> summaries) {
        this.summaries = summaries;
    }

    public List<ClientSummary> getSummaries() {
        return summaries;
    }

    // ENCODE
    public static void encode(FabricCrushingSummaryPacket msg, FriendlyByteBuf buf) {
        List<ClientSummary> list = msg.summaries;
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

    // DECODE
    public static FabricCrushingSummaryPacket decode(FriendlyByteBuf buf) {
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
        return new FabricCrushingSummaryPacket(list);
    }
}