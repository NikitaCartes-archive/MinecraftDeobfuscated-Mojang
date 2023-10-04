package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseTorchBlock extends Block {
	protected static final int AABB_STANDING_OFFSET = 2;
	protected static final VoxelShape AABB = Block.box(6.0, 0.0, 6.0, 10.0, 10.0, 10.0);

	protected BaseTorchBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected abstract MapCodec<? extends BaseTorchBlock> codec();

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return AABB;
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return direction == Direction.DOWN && !this.canSurvive(blockState, levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return canSupportCenter(levelReader, blockPos.below(), Direction.UP);
	}
}
