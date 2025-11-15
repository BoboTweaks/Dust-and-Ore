package com.bobotweaks.dustandore.mixin;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.bobotweaks.dustandore.gameplay.TooltipManager;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

@Mixin(ItemStack.class)
public abstract class MixinItemStackTooltip {
    @Inject(method = "getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void dustandore$appendHammerTooltip(Item.TooltipContext ctx, @Nullable Player player, TooltipFlag flag,
            CallbackInfoReturnable<List<Component>> cir) {
        ItemStack self = (ItemStack) (Object) this;
        ArrayList<Component> addLines = new ArrayList<>();
        TooltipManager.CreateTooltipFromList(self, ctx, addLines);
        if (!addLines.isEmpty()) {
            List<Component> lines = new ArrayList<>(cir.getReturnValue());
            int insertAt = Math.min(1, lines.size());
            lines.addAll(insertAt, addLines);
            cir.setReturnValue(lines);
        }
    }
}
