package com.bobotweaks.dustandore.events;

import com.bobotweaks.dustandore.core.CrushingInteraction;
import com.bobotweaks.dustandore.events.RightClickBlockEvent;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

public final class FabricRightClickBlock {

    private FabricRightClickBlock() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player == null)
                return InteractionResult.PASS;
            InteractionResult result = RightClickBlockEvent.onRightClickBlock(
                player, 
                hand, 
                hitResult.getBlockPos(),
                hitResult
            );

            if (result == InteractionResult.FAIL) {
                if (!world.isClientSide()) {
                    CrushingInteraction.execute(world, player.getX(), player.getY(), player.getZ(), player);
                } else {
                    ItemStack stackInHand = player.getItemInHand(hand);
                    if (!stackInHand.isEmpty() && !player.getCooldowns().isOnCooldown(stackInHand)) {
                        player.swing(hand);
                    }
                }
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });
    }
}
