package com.bobotweaks.dustandore.events;

import com.bobotweaks.dustandore.gameplay.hammer.HammerUtilities;
import com.bobotweaks.dustandore.core.CrushingInteraction;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

public final class FabricRightClickItem {

    private FabricRightClickItem() {
    }

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (hand != InteractionHand.MAIN_HAND)
                return InteractionResult.PASS;
            ItemStack stack = player.getMainHandItem();
            if (stack == null || stack.isEmpty())
                return InteractionResult.PASS;
            if (world.isClientSide())
                return InteractionResult.PASS;
            if (!HammerUtilities.isCrushingHammer(stack.getItem()))
                return InteractionResult.PASS;
            CrushingInteraction.execute(world, player.getX(), player.getY(), player.getZ(), player);
            return InteractionResult.SUCCESS;
        });
    }
}
