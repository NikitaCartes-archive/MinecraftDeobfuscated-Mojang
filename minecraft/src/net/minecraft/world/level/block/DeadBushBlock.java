package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DeadBushBlock extends BushBlock {
	protected static final float AABB_OFFSET = 6.0F;
	protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

	protected DeadBushBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.is(Blocks.SAND)
			|| blockState.is(Blocks.RED_SAND)
			|| blockState.is(Blocks.TERRACOTTA)
			|| blockState.is(Blocks.WHITE_TERRACOTTA)
			|| blockState.is(Blocks.ORANGE_TERRACOTTA)
			|| blockState.is(Blocks.MAGENTA_TERRACOTTA)
			|| blockState.is(Blocks.LIGHT_BLUE_TERRACOTTA)
			|| blockState.is(Blocks.YELLOW_TERRACOTTA)
			|| blockState.is(Blocks.LIME_TERRACOTTA)
			|| blockState.is(Blocks.PINK_TERRACOTTA)
			|| blockState.is(Blocks.GRAY_TERRACOTTA)
			|| blockState.is(Blocks.LIGHT_GRAY_TERRACOTTA)
			|| blockState.is(Blocks.CYAN_TERRACOTTA)
			|| blockState.is(Blocks.PURPLE_TERRACOTTA)
			|| blockState.is(Blocks.BLUE_TERRACOTTA)
			|| blockState.is(Blocks.BROWN_TERRACOTTA)
			|| blockState.is(Blocks.GREEN_TERRACOTTA)
			|| blockState.is(Blocks.RED_TERRACOTTA)
			|| blockState.is(Blocks.BLACK_TERRACOTTA)
			|| blockState.is(BlockTags.DIRT);
	}
}
