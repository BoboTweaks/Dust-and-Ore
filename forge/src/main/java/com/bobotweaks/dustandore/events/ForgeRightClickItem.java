package com.bobotweaks.dustandore.events;

import com.bobotweaks.dustandore.Constants;
import com.bobotweaks.dustandore.ForgeMod;
import com.bobotweaks.dustandore.core.CrushingInteraction;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ForgeMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeRightClickItem {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player == null)
            return;

        if (event.getHand() != InteractionHand.MAIN_HAND)
            return;

        Level level = event.getLevel();

        CrushingInteraction.execute(level, player.getX(), player.getY(), player.getZ(), player);

        if (event.getCancellationResult() == InteractionResult.PASS) {
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
