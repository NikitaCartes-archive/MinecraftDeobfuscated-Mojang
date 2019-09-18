package net.minecraft.world.level.block;

import net.minecraft.world.item.DyeColor;

public class StainedGlassBlock extends AbstractGlassBlock implements BeaconBeamBlock {
	private final DyeColor color;

	public StainedGlassBlock(DyeColor dyeColor, Block.Properties properties) {
		super(properties);
		this.color = dyeColor;
	}

	@Override
	public DyeColor getColor() {
		return this.color;
	}
}
