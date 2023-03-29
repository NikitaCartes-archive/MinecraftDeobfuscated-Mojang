package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PitcherCropBlock extends DoublePlantBlock implements BonemealableBlock {
	public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
	public static final int MAX_AGE = 4;
	private static final int DOUBLE_PLANT_AGE_INTERSECTION = 3;
	private static final int BONEMEAL_INCREASE = 1;
	private static final VoxelShape UPPER_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 15.0, 13.0);
	private static final VoxelShape LOWER_SHAPE = Block.box(3.0, -1.0, 3.0, 13.0, 16.0, 13.0);
	private static final VoxelShape COLLISION_SHAPE_BULB = Block.box(5.0, -1.0, 5.0, 11.0, 3.0, 11.0);
	private static final VoxelShape COLLISION_SHAPE_CROP = Block.box(3.0, -1.0, 3.0, 13.0, 5.0, 13.0);

	public PitcherCropBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	private boolean isMaxAge(BlockState blockState) {
		return (Integer)blockState.getValue(AGE) >= 4;
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState) {
		return blockState.getValue(HALF) == DoubleBlockHalf.LOWER && !this.isMaxAge(blockState);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState();
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return !blockState.canSurvive(levelAccessor, blockPos) ? Blocks.AIR.defaultBlockState() : blockState;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		if ((Integer)blockState.getValue(AGE) == 0) {
			return COLLISION_SHAPE_BULB;
		} else {
			return blockState.getValue(HALF) == DoubleBlockHalf.LOWER
				? COLLISION_SHAPE_CROP
				: super.getCollisionShape(blockState, blockGetter, blockPos, collisionContext);
		}
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		if (blockState.getValue(HALF) == DoubleBlockHalf.LOWER && (Integer)blockState.getValue(AGE) >= 3) {
			BlockState blockState2 = levelReader.getBlockState(blockPos.above());
			return blockState2.is(this)
				&& blockState2.getValue(HALF) == DoubleBlockHalf.UPPER
				&& this.mayPlaceOn(levelReader.getBlockState(blockPos.below()), levelReader, blockPos);
		} else {
			return (levelReader.getRawBrightness(blockPos, 0) >= 8 || levelReader.canSeeSky(blockPos)) && super.canSurvive(blockState, levelReader, blockPos);
		}
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.is(Blocks.FARMLAND);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return blockState.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_SHAPE : LOWER_SHAPE;
	}

	@Override
	public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		return false;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		float f = CropBlock.getGrowthSpeed(this, serverLevel, blockPos);
		boolean bl = randomSource.nextInt((int)(25.0F / f) + 1) == 0;
		if (bl) {
			this.grow(serverLevel, blockState, blockPos, 1);
		}
	}

	private void grow(ServerLevel serverLevel, BlockState blockState, BlockPos blockPos, int i) {
		int j = Math.min((Integer)blockState.getValue(AGE) + i, 4);
		if (j < 3 || canGrowInto(serverLevel, blockPos.above())) {
			serverLevel.setBlock(blockPos, blockState.setValue(AGE, Integer.valueOf(j)), 2);
			if (j >= 3) {
				DoubleBlockHalf doubleBlockHalf = blockState.getValue(HALF);
				if (doubleBlockHalf == DoubleBlockHalf.LOWER) {
					BlockPos blockPos2 = blockPos.above();
					serverLevel.setBlock(
						blockPos2,
						copyWaterloggedFrom(serverLevel, blockPos, this.defaultBlockState().setValue(AGE, Integer.valueOf(j)).setValue(HALF, DoubleBlockHalf.UPPER)),
						3
					);
				} else if (doubleBlockHalf == DoubleBlockHalf.UPPER) {
					BlockPos blockPos2 = blockPos.below();
					serverLevel.setBlock(
						blockPos2,
						copyWaterloggedFrom(serverLevel, blockPos, this.defaultBlockState().setValue(AGE, Integer.valueOf(j)).setValue(HALF, DoubleBlockHalf.LOWER)),
						3
					);
				}
			}
		}
	}

	private static boolean canGrowInto(ServerLevel serverLevel, BlockPos blockPos) {
		BlockState blockState = serverLevel.getBlockState(blockPos);
		return blockState.isAir() || blockState.is(Blocks.PITCHER_CROP);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean bl) {
		return !this.isMaxAge(blockState);
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		this.grow(serverLevel, blockState, blockPos, 1);
	}
}
