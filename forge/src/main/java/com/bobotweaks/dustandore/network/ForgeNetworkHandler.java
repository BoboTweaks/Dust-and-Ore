package com.bobotweaks.dustandore.network;

import com.bobotweaks.dustandore.core.CrushingManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.List;

public class ForgeNetworkHandler {
    private static final String PROTOCOL_VERSION = "1.0.0";
    private static SimpleChannel CHANNEL;

    public static void register() {
        CHANNEL = ChannelBuilder.named(
                ResourceLocation.fromNamespaceAndPath("dustandore", "main"))
                .networkProtocolVersion(1)
                .clientAcceptedVersions((status, version) -> true)
                .serverAcceptedVersions((status, version) -> true)
                .simpleChannel();

        CHANNEL.messageBuilder(CrushingSummaryPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CrushingSummaryPayload::encode)
                .decoder(CrushingSummaryPayload::new)
                .consumerMainThread(ForgeNetworkHandler::handle)
                .add();
    }

    private static void handle(CrushingSummaryPayload packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            CrushingManager.setClientSummary(packet.summaries());
        });
        context.setPacketHandled(true);
    }

    public static void sendToClient(ServerPlayer player, List<CrushingManager.ClientSummary> summaries) {
        CHANNEL.send(new CrushingSummaryPayload(summaries), PacketDistributor.PLAYER.with(player));
    }

    public static void sendToAllClients(List<ServerPlayer> players, List<CrushingManager.ClientSummary> summaries) {
        CrushingSummaryPayload payload = new CrushingSummaryPayload(summaries);
        for (ServerPlayer player : players) {
            CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
        }
    }
}