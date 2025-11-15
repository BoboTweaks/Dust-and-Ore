package com.bobotweaks.dustandore.events;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.sounds.SoundEvent;
import com.bobotweaks.dustandore.Constants;
import com.bobotweaks.dustandore.gameplay.recipe.HammerCrushRecipe;

public class CrushingEventHandler {
    private static final String[] CRUSH_SOUNDS = {
            "crushing_sound_effect"
    };
    private static final Random RANDOM = new Random();

    public static boolean CrushingEventTriggered(ServerLevel level,
            ServerPlayer player,
            ItemStack hammer,
            ItemStack result,
            HammerCrushRecipe recipe,
            double chance) {
        if (level == null || player == null || result == null || result.isEmpty())
            return false;

        try {
            PlayParticlesAndSound(level, player.getX(), player.getY(), player.getZ(), player);
        } catch (Throwable ignored) {
        }

        try {
            String cs = recipe != null ? recipe.getCrushingSound() : null;
            if (cs != null && !cs.isEmpty()) {
                SoundEvent ev = soundFromKey(cs);
                player.playNotifySound(ev, SoundSource.PLAYERS, 1.0f, 1.0f);
            } else {
                float volume = 0.15f + (RANDOM.nextFloat() * 0.3f);
                float pitch = 0.85f + (RANDOM.nextFloat() * 0.5f);
                player.playNotifySound(getRandomCrushingSound(), SoundSource.PLAYERS, volume, pitch);
            }
        } catch (Throwable ignored) {
        }

        if (chance <= 0.0f)
            return false;

        if (chance < 1.0f) {
            double roll = level.getRandom().nextDouble();
            if (roll >= chance)
                return false;
        }

        return true;
    }

    public static void PlayParticlesAndSound(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (!(world instanceof Level level) || !(entity instanceof LivingEntity living))
            return;

        ItemStack raw = living.getOffhandItem();
        if (raw.isEmpty())
            return;

        float yawDeg = living.getYRot();
        float pitchDeg = living.getXRot();
        double yaw = Math.toRadians(yawDeg);
        double pitch = Math.toRadians(pitchDeg);

        double fx = -Math.sin(yaw) * Math.cos(pitch);
        double fy = -Math.sin(pitch);
        double fz = Math.cos(yaw) * Math.cos(pitch);

        double forward = 1;
        double height = living.getY() + living.getEyeHeight() - 0.4;

        Vec3 at = new Vec3(
                living.getX() + fx * forward,
                height + fy * forward,
                living.getZ() + fz * forward);

        if (level.isClientSide) {
            spawnParticlesClient(level, at, raw, 24);
        } else if (level instanceof ServerLevel sl) {
            spawnParticlesServer(sl, at, raw, 24);
        }
    }

    private static void spawnParticlesClient(Level level, Vec3 location, ItemStack itemStack, int count) {
        if (itemStack == null || itemStack.isEmpty())
            return;

        ItemStack particleStack = itemStack.copy();
        particleStack.setCount(1);

        for (int i = 0; i < count; i++) {
            double dx = (level.random.nextDouble() - 0.5) * 0.12;
            double dy = (level.random.nextDouble() - 0.5) * 0.12;
            double dz = (level.random.nextDouble() - 0.5) * 0.12;
            double px = location.x + (level.random.nextDouble() - 0.5) * 0.2;
            double py = location.y + (level.random.nextDouble() - 0.5) * 0.2;
            double pz = location.z + (level.random.nextDouble() - 0.5) * 0.2;

            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, particleStack),
                    px, py, pz, dx, dy, dz);
        }
    }

    private static void spawnParticlesServer(ServerLevel level, Vec3 location, ItemStack itemStack, int count) {
        if (itemStack == null || itemStack.isEmpty())
            return;

        ItemStack particleStack = itemStack.copy();
        particleStack.setCount(1);

        double spread = 0.3;
        double speed = 0.05;

        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, particleStack),
                location.x, location.y, location.z,
                count,
                spread * 0.5, spread * 0.5, spread * 0.5,
                speed);
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

    public static SoundEvent getRandomCrushingSound() {
        if (CRUSH_SOUNDS.length == 0)
            return SoundEvents.STONE_BREAK;
        String key = CRUSH_SOUNDS[RANDOM.nextInt(CRUSH_SOUNDS.length)];
        return soundFromKey(key);
    }

    public static void playRandomCrushingSound(Level level, Player player, float volume, float pitch) {
        SoundEvent sound = getRandomCrushingSound();
        level.playSound(
                /* player = */ null,
                player.getX(), player.getY(), player.getZ(),
                sound,
                SoundSource.PLAYERS,
                volume,
                pitch);
    }
}
