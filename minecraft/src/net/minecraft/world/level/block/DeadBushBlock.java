package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DeadBushBlock extends BushBlock {
	protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

	protected DeadBushBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		Block block = blockState.getBlock();
		return block == Blocks.SAND
			|| block == Blocks.RED_SAND
			|| block == Blocks.TERRACOTTA
			|| block == Blocks.WHITE_TERRACOTTA
			|| block == Blocks.ORANGE_TERRACOTTA
			|| block == Blocks.MAGENTA_TERRACOTTA
			|| block == Blocks.LIGHT_BLUE_TERRACOTTA
			|| block == Blocks.YELLOW_TERRACOTTA
			|| block == Blocks.LIME_TERRACOTTA
			|| block == Blocks.PINK_TERRACOTTA
			|| block == Blocks.GRAY_TERRACOTTA
			|| block == Blocks.LIGHT_GRAY_TERRACOTTA
			|| block == Blocks.CYAN_TERRACOTTA
			|| block == Blocks.PURPLE_TERRACOTTA
			|| block == Blocks.BLUE_TERRACOTTA
			|| block == Blocks.BROWN_TERRACOTTA
			|| block == Blocks.GREEN_TERRACOTTA
			|| block == Blocks.RED_TERRACOTTA
			|| block == Blocks.BLACK_TERRACOTTA
			|| block == Blocks.DIRT
			|| block == Blocks.COARSE_DIRT
			|| block == Blocks.PODZOL;
	}
}
