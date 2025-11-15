package com.bobotweaks.dustandore.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bobotweaks.dustandore.core.CrushingInteraction;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleUseItem", at = @At("TAIL"))
    private void dustandore$onUseItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
        if (player == null)
            return;
        InteractionHand hand = packet.getHand();
        if (hand != InteractionHand.MAIN_HAND)
            return;
        Level level = player.level();
        CrushingInteraction.execute(level, player.getX(), player.getY(), player.getZ(), player);
    }
}