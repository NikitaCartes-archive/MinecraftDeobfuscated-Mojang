package net.minecraft.world.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

public class AirItem extends Item {
	private final Block block;

	public AirItem(Block block, Item.Properties properties) {
		super(properties);
		this.block = block;
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
		this.block.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);
	}
}
