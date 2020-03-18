package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class WeepingVinesPlant extends GrowingPlantBodyBlock {
	public WeepingVinesPlant(BlockBehaviour.Properties properties) {
		super(properties, Direction.DOWN, NetherVines.SHAPE, false);
	}

	@Override
	protected GrowingPlantHeadBlock getHeadBlock() {
		return (GrowingPlantHeadBlock)Blocks.WEEPING_VINES;
	}
}
