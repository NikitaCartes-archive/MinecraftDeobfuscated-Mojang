package net.minecraft.world.level.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockLayer;

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

	@Override
	public BlockLayer getRenderLayer() {
		return BlockLayer.TRANSLUCENT;
	}
}
