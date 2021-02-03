package net.minecraft.world.level.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class WoolCarpetBlock extends CarpetBlock {
	private final DyeColor color;

	protected WoolCarpetBlock(DyeColor dyeColor, BlockBehaviour.Properties properties) {
		super(properties);
		this.color = dyeColor;
	}

	public DyeColor getColor() {
		return this.color;
	}
}
