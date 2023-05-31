package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
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
	private static final VoxelShape FULL_UPPER_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 15.0, 13.0);
	private static final VoxelShape FULL_LOWER_SHAPE = Block.box(3.0, -1.0, 3.0, 13.0, 16.0, 13.0);
	private static final VoxelShape COLLISION_SHAPE_BULB = Block.box(5.0, -1.0, 5.0, 11.0, 3.0, 11.0);
	private static final VoxelShape COLLISION_SHAPE_CROP = Block.box(3.0, -1.0, 3.0, 13.0, 5.0, 13.0);
	private static final VoxelShape[] UPPER_SHAPE_BY_AGE = new VoxelShape[]{Block.box(3.0, 0.0, 3.0, 13.0, 11.0, 13.0), FULL_UPPER_SHAPE};
	private static final VoxelShape[] LOWER_SHAPE_BY_AGE = new VoxelShape[]{
		COLLISION_SHAPE_BULB, Block.box(3.0, -1.0, 3.0, 13.0, 14.0, 13.0), FULL_LOWER_SHAPE, FULL_LOWER_SHAPE, FULL_LOWER_SHAPE
	};

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
		return !isLower(blockState)
			? super.canSurvive(blockState, levelReader, blockPos)
			: this.mayPlaceOn(levelReader.getBlockState(blockPos.below()), levelReader, blockPos.below())
				&& sufficientLight(levelReader, blockPos)
				&& ((Integer)blockState.getValue(AGE) < 3 || isUpper(levelReader.getBlockState(blockPos.above())));
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
		return blockState.getValue(HALF) == DoubleBlockHalf.UPPER
			? UPPER_SHAPE_BY_AGE[Math.min(Math.abs(4 - ((Integer)blockState.getValue(AGE) + 1)), UPPER_SHAPE_BY_AGE.length - 1)]
			: LOWER_SHAPE_BY_AGE[blockState.getValue(AGE)];
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (entity instanceof Ravager && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
			level.destroyBlock(blockPos, true, entity);
		}

		super.entityInside(blockState, level, blockPos, entity);
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
		if (this.canGrow(serverLevel, blockPos, blockState, j)) {
			serverLevel.setBlock(blockPos, blockState.setValue(AGE, Integer.valueOf(j)), 2);
			if (j >= 3) {
				BlockPos blockPos2 = blockPos.above();
				serverLevel.setBlock(
					blockPos2, copyWaterloggedFrom(serverLevel, blockPos, this.defaultBlockState().setValue(AGE, Integer.valueOf(j)).setValue(HALF, DoubleBlockHalf.UPPER)), 3
				);
			}
		}
	}

	private static boolean canGrowInto(LevelReader levelReader, BlockPos blockPos) {
		BlockState blockState = levelReader.getBlockState(blockPos);
		return blockState.isAir() || blockState.is(Blocks.PITCHER_CROP);
	}

	private static boolean sufficientLight(LevelReader levelReader, BlockPos blockPos) {
		return levelReader.getRawBrightness(blockPos, 0) >= 8 || levelReader.canSeeSky(blockPos);
	}

	private static boolean isLower(BlockState blockState) {
		return blockState.is(Blocks.PITCHER_CROP) && blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
	}

	private static boolean isUpper(BlockState blockState) {
		return blockState.is(Blocks.PITCHER_CROP) && blockState.getValue(HALF) == DoubleBlockHalf.UPPER;
	}

	private boolean canGrow(LevelReader levelReader, BlockPos blockPos, BlockState blockState, int i) {
		return !this.isMaxAge(blockState) && sufficientLight(levelReader, blockPos) && (i < 3 || canGrowInto(levelReader, blockPos.above()));
	}

	@Nullable
	private PitcherCropBlock.PosAndState getLowerHalf(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		if (isLower(blockState)) {
			return new PitcherCropBlock.PosAndState(blockPos, blockState);
		} else {
			BlockPos blockPos2 = blockPos.below();
			BlockState blockState2 = levelReader.getBlockState(blockPos2);
			return isLower(blockState2) ? new PitcherCropBlock.PosAndState(blockPos2, blockState2) : null;
		}
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean bl) {
		PitcherCropBlock.PosAndState posAndState = this.getLowerHalf(levelReader, blockPos, blockState);
		return posAndState == null ? false : this.canGrow(levelReader, posAndState.pos, posAndState.state, (Integer)posAndState.state.getValue(AGE) + 1);
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		PitcherCropBlock.PosAndState posAndState = this.getLowerHalf(serverLevel, blockPos, blockState);
		if (posAndState != null) {
			this.grow(serverLevel, posAndState.state, posAndState.pos, 1);
		}
	}

	static record PosAndState(BlockPos pos, BlockState state) {
	}
}
