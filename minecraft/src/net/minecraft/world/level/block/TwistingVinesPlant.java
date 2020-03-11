package net.minecraft.world.level.block;

import net.minecraft.core.Direction;

public class TwistingVinesPlant extends GrowingPlantBodyBlock {
	public TwistingVinesPlant(Block.Properties properties) {
		super(properties, Direction.UP, NetherVines.SHAPE, false);
	}

	@Override
	protected GrowingPlantHeadBlock getHeadBlock() {
		return (GrowingPlantHeadBlock)Blocks.TWISTING_VINES;
	}
}
