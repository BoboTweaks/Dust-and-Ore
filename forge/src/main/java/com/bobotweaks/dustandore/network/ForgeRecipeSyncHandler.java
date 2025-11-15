package com.bobotweaks.dustandore.events;

import com.bobotweaks.dustandore.core.CrushingManager;
import com.bobotweaks.dustandore.network.ForgeNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeRecipeSyncHandler {

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
            List<CrushingManager.ClientSummary> summaries = CrushingManager
                    .buildServerClientSummary(player.getServer());
            ForgeNetworkHandler.sendToClient(player, summaries);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void syncRecipesToAllPlayers(net.minecraft.server.MinecraftServer server) {
        if (server != null) {
            List<CrushingManager.ClientSummary> summaries = CrushingManager.buildServerClientSummary(server);
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            ForgeNetworkHandler.sendToAllClients(players, summaries);
        }
    }
}