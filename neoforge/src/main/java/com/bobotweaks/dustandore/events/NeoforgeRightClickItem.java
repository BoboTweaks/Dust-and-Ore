package com.bobotweaks.dustandore.events;

import com.bobotweaks.dustandore.gameplay.hammer.HammerUtilities;
import com.bobotweaks.dustandore.core.CrushingInteraction;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class NeoforgeRightClickItem {

    private NeoforgeRightClickItem() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(NeoforgeRightClickItem::onRightClickItem);
    }

    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND)
            return;

        Player player = event.getEntity();
        if (player == null)
            return;

        Level level = event.getLevel();
        if (level.isClientSide())
            return;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty())
            return;

        if (!HammerUtilities.isCrushingHammer(stack.getItem()))
            return;

        CrushingInteraction.execute(level, player.getX(), player.getY(), player.getZ(), player);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
