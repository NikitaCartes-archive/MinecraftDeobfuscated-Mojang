package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CactusBlock extends Block {
	public static final MapCodec<CactusBlock> CODEC = simpleCodec(CactusBlock::new);
	public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
	public static final int MAX_AGE = 15;
	protected static final int AABB_OFFSET = 1;
	protected static final VoxelShape COLLISION_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);
	protected static final VoxelShape OUTLINE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

	@Override
	public MapCodec<CactusBlock> codec() {
		return CODEC;
	}

	protected CactusBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (!blockState.canSurvive(serverLevel, blockPos)) {
			serverLevel.destroyBlock(blockPos, true);
		}
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		BlockPos blockPos2 = blockPos.above();
		if (serverLevel.isEmptyBlock(blockPos2)) {
			int i = 1;

			while (serverLevel.getBlockState(blockPos.below(i)).is(this)) {
				i++;
			}

			if (i < 3) {
				int j = (Integer)blockState.getValue(AGE);
				if (j == 15) {
					serverLevel.setBlockAndUpdate(blockPos2, this.defaultBlockState());
					BlockState blockState2 = blockState.setValue(AGE, Integer.valueOf(0));
					serverLevel.setBlock(blockPos, blockState2, 4);
					serverLevel.neighborChanged(blockState2, blockPos2, this, null, false);
				} else {
					serverLevel.setBlock(blockPos, blockState.setValue(AGE, Integer.valueOf(j + 1)), 4);
				}
			}
		}
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return COLLISION_SHAPE;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return OUTLINE_SHAPE;
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState,
		LevelReader levelReader,
		ScheduledTickAccess scheduledTickAccess,
		BlockPos blockPos,
		Direction direction,
		BlockPos blockPos2,
		BlockState blockState2,
		RandomSource randomSource
	) {
		if (!blockState.canSurvive(levelReader, blockPos)) {
			scheduledTickAccess.scheduleTick(blockPos, this, 1);
		}

		return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockState blockState2 = levelReader.getBlockState(blockPos.relative(direction));
			if (blockState2.isSolid() || levelReader.getFluidState(blockPos.relative(direction)).is(FluidTags.LAVA)) {
				return false;
			}
		}

		BlockState blockState3 = levelReader.getBlockState(blockPos.below());
		return (blockState3.is(Blocks.CACTUS) || blockState3.is(BlockTags.SAND)) && !levelReader.getBlockState(blockPos.above()).liquid();
	}

	@Override
	protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		entity.hurt(level.damageSources().cactus(), 1.0F);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}
}
