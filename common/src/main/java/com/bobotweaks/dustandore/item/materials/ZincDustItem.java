package com.bobotweaks.dustandore.item.materials;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class ZincDustItem extends Item {
	public ZincDustItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId())
				.withStyle(style -> style.withColor(0xCFD6D3));
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, TooltipDisplay tooltipDisplay,
			Consumer<Component> componentConsumer, TooltipFlag flag) {
		super.appendHoverText(itemstack, context, tooltipDisplay, componentConsumer, flag);
		Component zincName = Component.translatable("item.create.zinc_ingot")
				.withStyle(s -> s.withColor(0xCFD6D3));
		componentConsumer.accept(
				Component.literal("Can be smelted in a Furnace or Blast Furnace to obtain a ")
						.append(zincName)
						.append(Component.literal(".")));
	}
}