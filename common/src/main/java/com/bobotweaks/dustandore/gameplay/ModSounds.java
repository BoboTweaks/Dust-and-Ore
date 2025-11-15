package com.bobotweaks.dustandore.gameplay;

import com.bobotweaks.dustandore.Constants;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModSounds {

    public static void playItemBreakSound(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (world instanceof ServerLevel level) {
            level.playSound(null, BlockPos.containing(x, y, z), soundFromKey("entity.item.break"), SoundSource.PLAYERS,
                    1.0F, 1.0F);
        } else {
            world.playSound(null, BlockPos.containing(x, y, z), soundFromKey("entity.item.break"), SoundSource.PLAYERS,
                    1.0F, 1.0F);
        }
    }

    private static SoundEvent soundFromKey(String key) {
        if (key == null || key.isEmpty())
            return SoundEvents.STONE_BREAK;
        ResourceLocation id;
        if (key.indexOf(':') >= 0) {
            id = ResourceLocation.parse(key);
        } else if (key.indexOf('.') >= 0) {
            id = ResourceLocation.fromNamespaceAndPath("minecraft", key);
        } else {
            id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, key);
        }
        Holder.Reference<SoundEvent> ref = BuiltInRegistries.SOUND_EVENT.get(id).orElse(null);
        if (ref != null)
            return ref.value();
        return SoundEvent.createVariableRangeEvent(id);
    }
}