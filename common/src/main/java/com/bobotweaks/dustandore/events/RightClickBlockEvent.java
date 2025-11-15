package com.bobotweaks.dustandore.events;

import com.bobotweaks.dustandore.core.CrushingManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.MenuProvider;

public final class RightClickBlockEvent {

    private RightClickBlockEvent() {
    }

    public static InteractionResult onRightClickBlock(Player player, InteractionHand hand, BlockPos pos,
            BlockHitResult hitResult) {
        if (player == null)
            return InteractionResult.PASS;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offhand = player.getOffhandItem();

        if (CrushingManager.shouldBlockPlacement(player.level(), player, mainHand, offhand)) {
            if (hasGui(player.level(), pos, player, hitResult)) {
                return InteractionResult.PASS;
            }
            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }

    private static boolean hasGui(Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level == null || !level.isLoaded(pos)) {
            return false;
        }

        try {
            if (level.getBlockState(pos).getMenuProvider(level, pos) != null) {
                return true;
            }
        } catch (Exception e) {

        }

        try {
            InteractionResult testResult = level.getBlockState(pos).useWithoutItem(level, player, hitResult);
            if (testResult.consumesAction()) {
                return true;
            }
        } catch (Exception e) {

        }

        try {
            if (level.getBlockEntity(pos) != null) {
                var blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null) {
                    if (blockEntity instanceof MenuProvider) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {

        }

        return false;
    }
}