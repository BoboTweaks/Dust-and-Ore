package com.bobotweaks.dustandore.network;

import com.bobotweaks.dustandore.core.CrushingManager;
import com.bobotweaks.dustandore.core.CrushingManager.ClientSummary;
import com.bobotweaks.dustandore.Constants;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class FabricCrushingSync {
    public static final ResourceLocation CLIENT_SUMMARY_PACKET = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
            "crushing_summary");

    public static void sendSummaryToPlayer(ServerPlayer player) {
        net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) player.level();
        MinecraftServer server = serverLevel.getServer();

        List<ClientSummary> summaries = CrushingManager.buildServerClientSummary(server);

        ServerPlayNetworking.send(player, new CrushingSummaryPayload(summaries));
    }
}