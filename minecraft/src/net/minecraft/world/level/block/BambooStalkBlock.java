package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BambooStalkBlock extends Block implements BonemealableBlock {
	public static final MapCodec<BambooStalkBlock> CODEC = simpleCodec(BambooStalkBlock::new);
	protected static final float SMALL_LEAVES_AABB_OFFSET = 3.0F;
	protected static final float LARGE_LEAVES_AABB_OFFSET = 5.0F;
	protected static final float COLLISION_AABB_OFFSET = 1.5F;
	protected static final VoxelShape SMALL_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
	protected static final VoxelShape LARGE_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
	protected static final VoxelShape COLLISION_SHAPE = Block.box(6.5, 0.0, 6.5, 9.5, 16.0, 9.5);
	public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
	public static final EnumProperty<BambooLeaves> LEAVES = BlockStateProperties.BAMBOO_LEAVES;
	public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
	public static final int MAX_HEIGHT = 16;
	public static final int STAGE_GROWING = 0;
	public static final int STAGE_DONE_GROWING = 1;
	public static final int AGE_THIN_BAMBOO = 0;
	public static final int AGE_THICK_BAMBOO = 1;

	@Override
	public MapCodec<BambooStalkBlock> codec() {
		return CODEC;
	}

	public BambooStalkBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)).setValue(LEAVES, BambooLeaves.NONE).setValue(STAGE, Integer.valueOf(0))
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE, LEAVES, STAGE);
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return true;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		VoxelShape voxelShape = blockState.getValue(LEAVES) == BambooLeaves.LARGE ? LARGE_SHAPE : SMALL_SHAPE;
		Vec3 vec3 = blockState.getOffset(blockGetter, blockPos);
		return voxelShape.move(vec3.x, vec3.y, vec3.z);
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Vec3 vec3 = blockState.getOffset(blockGetter, blockPos);
		return COLLISION_SHAPE.move(vec3.x, vec3.y, vec3.z);
	}

	@Override
	protected boolean isCollisionShapeFullBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return false;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		if (!fluidState.isEmpty()) {
			return null;
		} else {
			BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().below());
			if (blockState.is(BlockTags.BAMBOO_PLANTABLE_ON)) {
				if (blockState.is(Blocks.BAMBOO_SAPLING)) {
					return this.defaultBlockState().setValue(AGE, Integer.valueOf(0));
				} else if (blockState.is(Blocks.BAMBOO)) {
					int i = blockState.getValue(AGE) > 0 ? 1 : 0;
					return this.defaultBlockState().setValue(AGE, Integer.valueOf(i));
				} else {
					BlockState blockState2 = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().above());
					return blockState2.is(Blocks.BAMBOO)
						? this.defaultBlockState().setValue(AGE, (Integer)blockState2.getValue(AGE))
						: Blocks.BAMBOO_SAPLING.defaultBlockState();
				}
			} else {
				return null;
			}
		}
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (!blockState.canSurvive(serverLevel, blockPos)) {
			serverLevel.destroyBlock(blockPos, true);
		}
	}

	@Override
	protected boolean isRandomlyTicking(BlockState blockState) {
		return (Integer)blockState.getValue(STAGE) == 0;
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if ((Integer)blockState.getValue(STAGE) == 0) {
			if (randomSource.nextInt(3) == 0 && serverLevel.isEmptyBlock(blockPos.above()) && serverLevel.getRawBrightness(blockPos.above(), 0) >= 9) {
				int i = this.getHeightBelowUpToMax(serverLevel, blockPos) + 1;
				if (i < 16) {
					this.growBamboo(blockState, serverLevel, blockPos, randomSource, i);
				}
			}
		}
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return levelReader.getBlockState(blockPos.below()).is(BlockTags.BAMBOO_PLANTABLE_ON);
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (!blockState.canSurvive(levelAccessor, blockPos)) {
			levelAccessor.scheduleTick(blockPos, this, 1);
		}

		if (direction == Direction.UP && blockState2.is(Blocks.BAMBOO) && (Integer)blockState2.getValue(AGE) > (Integer)blockState.getValue(AGE)) {
			levelAccessor.setBlock(blockPos, blockState.cycle(AGE), 2);
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		int i = this.getHeightAboveUpToMax(levelReader, blockPos);
		int j = this.getHeightBelowUpToMax(levelReader, blockPos);
		return i + j + 1 < 16 && (Integer)levelReader.getBlockState(blockPos.above(i)).getValue(STAGE) != 1;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		int i = this.getHeightAboveUpToMax(serverLevel, blockPos);
		int j = this.getHeightBelowUpToMax(serverLevel, blockPos);
		int k = i + j + 1;
		int l = 1 + randomSource.nextInt(2);

		for (int m = 0; m < l; m++) {
			BlockPos blockPos2 = blockPos.above(i);
			BlockState blockState2 = serverLevel.getBlockState(blockPos2);
			if (k >= 16 || (Integer)blockState2.getValue(STAGE) == 1 || !serverLevel.isEmptyBlock(blockPos2.above())) {
				return;
			}

			this.growBamboo(blockState2, serverLevel, blockPos2, randomSource, k);
			i++;
			k++;
		}
	}

	@Override
	protected float getDestroyProgress(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos) {
		return player.getMainHandItem().getItem() instanceof SwordItem ? 1.0F : super.getDestroyProgress(blockState, player, blockGetter, blockPos);
	}

	protected void growBamboo(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource, int i) {
		BlockState blockState2 = level.getBlockState(blockPos.below());
		BlockPos blockPos2 = blockPos.below(2);
		BlockState blockState3 = level.getBlockState(blockPos2);
		BambooLeaves bambooLeaves = BambooLeaves.NONE;
		if (i >= 1) {
			if (!blockState2.is(Blocks.BAMBOO) || blockState2.getValue(LEAVES) == BambooLeaves.NONE) {
				bambooLeaves = BambooLeaves.SMALL;
			} else if (blockState2.is(Blocks.BAMBOO) && blockState2.getValue(LEAVES) != BambooLeaves.NONE) {
				bambooLeaves = BambooLeaves.LARGE;
				if (blockState3.is(Blocks.BAMBOO)) {
					level.setBlock(blockPos.below(), blockState2.setValue(LEAVES, BambooLeaves.SMALL), 3);
					level.setBlock(blockPos2, blockState3.setValue(LEAVES, BambooLeaves.NONE), 3);
				}
			}
		}

		int j = blockState.getValue(AGE) != 1 && !blockState3.is(Blocks.BAMBOO) ? 0 : 1;
		int k = (i < 11 || !(randomSource.nextFloat() < 0.25F)) && i != 15 ? 0 : 1;
		level.setBlock(
			blockPos.above(), this.defaultBlockState().setValue(AGE, Integer.valueOf(j)).setValue(LEAVES, bambooLeaves).setValue(STAGE, Integer.valueOf(k)), 3
		);
	}

	protected int getHeightAboveUpToMax(BlockGetter blockGetter, BlockPos blockPos) {
		int i = 0;

		while (i < 16 && blockGetter.getBlockState(blockPos.above(i + 1)).is(Blocks.BAMBOO)) {
			i++;
		}

		return i;
	}

	protected int getHeightBelowUpToMax(BlockGetter blockGetter, BlockPos blockPos) {
		int i = 0;

		while (i < 16 && blockGetter.getBlockState(blockPos.below(i + 1)).is(Blocks.BAMBOO)) {
			i++;
		}

		return i;
	}
}
