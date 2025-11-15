package com.bobotweaks.dustandore.gameplay;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class EnchantmentUtilities {

    public static int getLevel(LevelAccessor world, Entity entity, String enchantment_id) {
        if (!(entity instanceof Player player))
            return 0;

        ResourceLocation id = ResourceLocation.tryParse(enchantment_id);
        if (id == null)
            return 0;

        ResourceKey<Enchantment> enchantment = ResourceKey.create(Registries.ENCHANTMENT, id);

        HolderLookup<Enchantment> lookup = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> holder = lookup.get(enchantment).orElse(null);
        if (holder == null)
            return 0;

        return EnchantmentHelper.getItemEnchantmentLevel(holder, player.getMainHandItem());
    }

    public static int getLevel(LevelAccessor world, ItemStack item, String enchantment_id) {
        ResourceLocation id = ResourceLocation.tryParse(enchantment_id);
        if (id == null)
            return 0;

        ResourceKey<Enchantment> enchantment = ResourceKey.create(Registries.ENCHANTMENT, id);

        HolderLookup<Enchantment> lookup = world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> holder = lookup.get(enchantment).orElse(null);
        if (holder == null)
            return 0;

        return EnchantmentHelper.getItemEnchantmentLevel(holder, item);
    }

    public static boolean isUnbreakable(ItemStack item) {
        try {
            Object unb = item.get(net.minecraft.core.component.DataComponents.UNBREAKABLE);
            return (unb != null);
        } catch (Throwable ignored) {
        }
        return false;
    }

}
