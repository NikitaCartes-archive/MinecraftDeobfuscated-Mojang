package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class AirItem extends Item {
	private final Block block;

	public AirItem(Block block, Item.Properties properties) {
		super(properties);
		this.block = block;
	}

	@Override
	public String getDescriptionId() {
		return this.block.getDescriptionId();
	}

	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, level, list, tooltipFlag);
		this.block.appendHoverText(itemStack, level, list, tooltipFlag);
	}
}
