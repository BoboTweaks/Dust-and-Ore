package com.bobotweaks.dustandore;

import java.util.List;
import java.util.ArrayList;

import com.bobotweaks.dustandore.core.CrushingManager;
import com.bobotweaks.dustandore.gameplay.TooltipManager;
import com.bobotweaks.dustandore.network.CrushingSummaryPayload;
import com.bobotweaks.dustandore.network.FabricNetwork;
import com.bobotweaks.dustandore.core.CrushingManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

public final class FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        com.bobotweaks.dustandore.network.FabricNetwork.registerPayloadTypes();
        FabricNetwork.initClient();
        ClientPlayNetworking.registerGlobalReceiver(
                CrushingSummaryPayload.TYPE,
                (payload, context) -> {
                    context.client().execute(() -> CrushingManager.setClientSummary(payload.summaries()));
                });
        ItemTooltipCallback.EVENT.register(new ItemTooltipCallback() {
            @Override
            public void getTooltip(ItemStack stack,
                    Item.TooltipContext context,
                    TooltipFlag flag,
                    List<Component> lines) {

                // 1) Raw-ore hint (for left_hand_item of hammer_crush)
                TooltipManager.appendRawOreTooltip(stack, lines);

                // 2) Hammer tooltip (for actual hammers)
                ArrayList<Component> addLines = new ArrayList<>();
                TooltipManager.CreateTooltipFromList(stack, context, addLines);
                if (!addLines.isEmpty()) {
                    int insertAt = Math.min(1, lines.size());
                    lines.addAll(insertAt, addLines);
                }
            }
        });
    }
}
