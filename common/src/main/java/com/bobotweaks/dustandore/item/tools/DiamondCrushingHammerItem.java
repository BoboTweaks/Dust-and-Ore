package com.bobotweaks.dustandore.item.tools;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class DiamondCrushingHammerItem extends Item {
	public DiamondCrushingHammerItem(Item.Properties properties) {
		super(properties.durability(750)
				.attributes(
						ItemAttributeModifiers.builder()
								.add(Attributes.ATTACK_DAMAGE,
										new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 0,
												AttributeModifier.Operation.ADD_VALUE),
										EquipmentSlotGroup.MAINHAND)
								.add(Attributes.ATTACK_SPEED,
										new AttributeModifier(BASE_ATTACK_SPEED_ID, -3,
												AttributeModifier.Operation.ADD_VALUE),
										EquipmentSlotGroup.MAINHAND)
								.build())
				.enchantable(15));
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId())
				.withStyle(style -> style.withColor(0x33E0FF));
	}

	@Override
	public float getDestroySpeed(ItemStack itemstack, BlockState blockstate) {
		return 1;
	}

	@Override
	public boolean mineBlock(ItemStack itemstack, Level world, BlockState blockstate, BlockPos pos,
			LivingEntity entity) {
		return true;
	}

	@Override
	public void hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
	}
}