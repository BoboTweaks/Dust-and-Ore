package com.bobotweaks.dustandore.gameplay.hammer;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class CooldownHandler {
    public static void applyCooldown(LevelAccessor level, Player player, ItemStack hammer, int finalCooldown) {
        try {
            player.getCooldowns().addCooldown(hammer, finalCooldown);
        } catch (Throwable ignored) {
        }
        long endTick = getGameTime(level) + finalCooldown;
        try {
            net.minecraft.world.item.component.CustomData existing = hammer
                    .get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            net.minecraft.nbt.CompoundTag tag = existing != null ? existing.copyTag()
                    : new net.minecraft.nbt.CompoundTag();
            tag.putLong("dustandore_cooldown_until", endTick);
            hammer.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.of(tag));
        } catch (Throwable ignored) {
        }
    }

    public static int getFinalCooldown(int baseCooldown, float effectiveSpeed) {
        return Math.max(1, Math.round(baseCooldown / effectiveSpeed));
    }

    public static int getBaseCooldown(int a, int b) {
        return Math.max(a, b);
    }

    private static long getGameTime(LevelAccessor levelAccessor) {
        if (levelAccessor instanceof Level) {
            return ((Level) levelAccessor).getGameTime();
        }
        return 0L;
    }
}