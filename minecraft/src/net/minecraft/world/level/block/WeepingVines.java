package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WeepingVines extends GrowingPlantHeadBlock {
	protected static final VoxelShape SHAPE = Block.box(4.0, 9.0, 4.0, 12.0, 16.0, 12.0);

	public WeepingVines(BlockBehaviour.Properties properties) {
		super(properties, Direction.DOWN, SHAPE, false, 0.1);
	}

	@Override
	protected Block getBodyBlock() {
		return Blocks.WEEPING_VINES_PLANT;
	}

	@Override
	protected boolean canGrowInto(BlockState blockState) {
		return NetherVines.isValidGrowthState(blockState);
	}
}
