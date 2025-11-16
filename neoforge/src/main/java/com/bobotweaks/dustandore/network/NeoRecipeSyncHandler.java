package com.bobotweaks.dustandore.events;

import com.bobotweaks.dustandore.core.CrushingManager;
import com.bobotweaks.dustandore.network.NeoNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.List;

@EventBusSubscriber
public class NeoRecipeSyncHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncRecipesToPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && !event.isEndConquered()) {
            syncRecipesToPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        syncRecipesToAllPlayers(event.getServer());
    }

    private static void syncRecipesToPlayer(ServerPlayer player) {
        try {
            net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) player
                    .level();
            net.minecraft.server.MinecraftServer server = serverLevel.getServer();

            List<CrushingManager.ClientSummary> summaries = CrushingManager.buildServerClientSummary(server);
            NeoNetworkHandler.sendToClient(player, summaries);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void syncRecipesToAllPlayers(net.minecraft.server.MinecraftServer server) {
        if (server != null) {
            List<CrushingManager.ClientSummary> summaries = CrushingManager.buildServerClientSummary(server);
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            NeoNetworkHandler.sendToAllClients(players, summaries);
        }
    }
}