package com.bobotweaks.dustandore.item.materials;

import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class CopperDustItem extends Item {
	public CopperDustItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, TooltipDisplay tooltipDisplay,
			Consumer<Component> componentConsumer, TooltipFlag flag) {
		super.appendHoverText(itemstack, context, tooltipDisplay, componentConsumer, flag);
		componentConsumer.accept(
				Component.literal("Can be smelted in a Furnace or Blast Furnace to obtain a ")
						.append(
								Component.translatable("item.minecraft.copper_ingot")
										.withStyle(s -> s.withColor(0xB87333)))
						.append(Component.literal(".")));
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId())
				.withStyle(style -> style.withColor(0xB87333));
	}
}