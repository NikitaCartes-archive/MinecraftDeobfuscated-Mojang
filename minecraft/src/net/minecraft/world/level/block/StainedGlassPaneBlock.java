package net.minecraft.world.level.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockLayer;

public class StainedGlassPaneBlock extends IronBarsBlock implements BeaconBeamBlock {
	private final DyeColor color;

	public StainedGlassPaneBlock(DyeColor dyeColor, Block.Properties properties) {
		super(properties);
		this.color = dyeColor;
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(NORTH, Boolean.valueOf(false))
				.setValue(EAST, Boolean.valueOf(false))
				.setValue(SOUTH, Boolean.valueOf(false))
				.setValue(WEST, Boolean.valueOf(false))
				.setValue(WATERLOGGED, Boolean.valueOf(false))
		);
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
