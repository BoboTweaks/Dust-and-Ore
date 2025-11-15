package com.bobotweaks.dustandore;

import java.util.ArrayList;
import java.util.List;

import com.bobotweaks.dustandore.gameplay.TooltipManager;

import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class NeoforgeClient {
    private NeoforgeClient() {
    }

    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> lines = event.getToolTip();
        Item.TooltipContext ctx = Item.TooltipContext.EMPTY;
        TooltipFlag flag = event.getFlags();

        ArrayList<Component> addLines = new ArrayList<>();
        TooltipManager.CreateTooltipFromList(stack, ctx, addLines);
        if (!addLines.isEmpty()) {
            int insertAt = Math.min(1, lines.size());
            lines.addAll(insertAt, addLines);
        }
    }
}
