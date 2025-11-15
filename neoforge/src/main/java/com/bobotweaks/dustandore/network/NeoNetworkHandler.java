package com.bobotweaks.dustandore.network;

import com.bobotweaks.dustandore.core.CrushingManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import com.bobotweaks.dustandore.network.CrushingSummaryPayload;

import java.util.List;

public class NeoNetworkHandler {

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0.0");

        registrar.playToClient(
                CrushingSummaryPayload.TYPE,
                CrushingSummaryPayload.STREAM_CODEC,
                NeoNetworkHandler::handle);
    }

    private static void handle(CrushingSummaryPayload payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            CrushingManager.setClientSummary(payload.summaries());
        });
    }

    public static void sendToClient(ServerPlayer player, List<CrushingManager.ClientSummary> summaries) {
        PacketDistributor.sendToPlayer(player, new CrushingSummaryPayload(summaries));
    }

    public static void sendToAllClients(List<ServerPlayer> players, List<CrushingManager.ClientSummary> summaries) {
        CrushingSummaryPayload payload = new CrushingSummaryPayload(summaries);
        for (ServerPlayer player : players) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }
}