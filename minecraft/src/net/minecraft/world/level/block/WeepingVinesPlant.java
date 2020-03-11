package net.minecraft.world.level.block;

import net.minecraft.core.Direction;

public class WeepingVinesPlant extends GrowingPlantBodyBlock {
	public WeepingVinesPlant(Block.Properties properties) {
		super(properties, Direction.DOWN, NetherVines.SHAPE, false);
	}

	@Override
	protected GrowingPlantHeadBlock getHeadBlock() {
		return (GrowingPlantHeadBlock)Blocks.WEEPING_VINES;
	}
}
