package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SeaPickleBlock extends BushBlock implements BonemealableBlock, SimpleWaterloggedBlock {
	public static final IntegerProperty PICKLES = BlockStateProperties.PICKLES;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final VoxelShape ONE_AABB = Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);
	protected static final VoxelShape TWO_AABB = Block.box(3.0, 0.0, 3.0, 13.0, 6.0, 13.0);
	protected static final VoxelShape THREE_AABB = Block.box(2.0, 0.0, 2.0, 14.0, 6.0, 14.0);
	protected static final VoxelShape FOUR_AABB = Block.box(2.0, 0.0, 2.0, 14.0, 7.0, 14.0);

	protected SeaPickleBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(PICKLES, Integer.valueOf(1)).setValue(WATERLOGGED, Boolean.valueOf(true)));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
		if (blockState.is(this)) {
			return blockState.setValue(PICKLES, Integer.valueOf(Math.min(4, (Integer)blockState.getValue(PICKLES) + 1)));
		} else {
			FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
			boolean bl = fluidState.getType() == Fluids.WATER;
			return super.getStateForPlacement(blockPlaceContext).setValue(WATERLOGGED, Boolean.valueOf(bl));
		}
	}

	public static boolean isDead(BlockState blockState) {
		return !(Boolean)blockState.getValue(WATERLOGGED);
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return !blockState.getCollisionShape(blockGetter, blockPos).getFaceShape(Direction.UP).isEmpty()
			|| blockState.isFaceSturdy(blockGetter, blockPos, Direction.UP);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		return this.mayPlaceOn(levelReader.getBlockState(blockPos2), levelReader, blockPos2);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (!blockState.canSurvive(levelAccessor, blockPos)) {
			return Blocks.AIR.defaultBlockState();
		} else {
			if ((Boolean)blockState.getValue(WATERLOGGED)) {
				levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
			}

			return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
		}
	}

	@Override
	public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		return blockPlaceContext.getItemInHand().getItem() == this.asItem() && blockState.getValue(PICKLES) < 4
			? true
			: super.canBeReplaced(blockState, blockPlaceContext);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		switch (blockState.getValue(PICKLES)) {
			case 1:
			default:
				return ONE_AABB;
			case 2:
				return TWO_AABB;
			case 3:
				return THREE_AABB;
			case 4:
				return FOUR_AABB;
		}
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PICKLES, WATERLOGGED);
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
		if (!isDead(blockState) && serverLevel.getBlockState(blockPos.below()).is(BlockTags.CORAL_BLOCKS)) {
			int i = 5;
			int j = 1;
			int k = 2;
			int l = 0;
			int m = blockPos.getX() - 2;
			int n = 0;

			for (int o = 0; o < 5; o++) {
				for (int p = 0; p < j; p++) {
					int q = 2 + blockPos.getY() - 1;

					for (int r = q - 2; r < q; r++) {
						BlockPos blockPos2 = new BlockPos(m + o, r, blockPos.getZ() - n + p);
						if (blockPos2 != blockPos && random.nextInt(6) == 0 && serverLevel.getBlockState(blockPos2).is(Blocks.WATER)) {
							BlockState blockState2 = serverLevel.getBlockState(blockPos2.below());
							if (blockState2.is(BlockTags.CORAL_BLOCKS)) {
								serverLevel.setBlock(blockPos2, Blocks.SEA_PICKLE.defaultBlockState().setValue(PICKLES, Integer.valueOf(random.nextInt(4) + 1)), 3);
							}
						}
					}
				}

				if (l < 2) {
					j += 2;
					n++;
				} else {
					j -= 2;
					n--;
				}

				l++;
			}

			serverLevel.setBlock(blockPos, blockState.setValue(PICKLES, Integer.valueOf(4)), 2);
		}
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
