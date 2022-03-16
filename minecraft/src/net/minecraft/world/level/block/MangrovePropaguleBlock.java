package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.grower.OakTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MangrovePropaguleBlock extends SaplingBlock {
	public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
	public static final int MAX_AGE = 4;
	private static final VoxelShape[] SHAPE_PER_AGE = new VoxelShape[]{
		Block.box(7.0, 13.0, 7.0, 9.0, 16.0, 9.0),
		Block.box(7.0, 10.0, 7.0, 9.0, 16.0, 9.0),
		Block.box(7.0, 7.0, 7.0, 9.0, 16.0, 9.0),
		Block.box(7.0, 3.0, 7.0, 9.0, 16.0, 9.0),
		Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0)
	};
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty HANGING = BlockStateProperties.HANGING;

	public MangrovePropaguleBlock(BlockBehaviour.Properties properties) {
		super(new OakTreeGrower(), properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(STAGE, Integer.valueOf(0))
				.setValue(AGE, Integer.valueOf(0))
				.setValue(WATERLOGGED, Boolean.valueOf(false))
				.setValue(HANGING, Boolean.valueOf(false))
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(STAGE).add(AGE).add(WATERLOGGED).add(HANGING);
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.is(BlockTags.DIRT) || blockState.is(Blocks.FARMLAND) || blockState.is(Blocks.CLAY) || blockState.is(Blocks.MUD);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
		boolean bl = fluidState.getType() == Fluids.WATER;
		return super.getStateForPlacement(blockPlaceContext).setValue(WATERLOGGED, Boolean.valueOf(bl)).setValue(AGE, Integer.valueOf(4));
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Vec3 vec3 = blockState.getOffset(blockGetter, blockPos);
		VoxelShape voxelShape;
		if (!(Boolean)blockState.getValue(HANGING)) {
			voxelShape = SHAPE_PER_AGE[4];
		} else {
			voxelShape = SHAPE_PER_AGE[blockState.getValue(AGE)];
		}

		return voxelShape.move(vec3.x, vec3.y, vec3.z);
	}

	@Override
	public BlockBehaviour.OffsetType getOffsetType() {
		return BlockBehaviour.OffsetType.XZ;
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return isHanging(blockState) ? levelReader.getBlockState(blockPos.above()).is(Blocks.MANGROVE_LEAVES) : super.canSurvive(blockState, levelReader, blockPos);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return direction == Direction.UP && !blockState.canSurvive(levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (!isHanging(blockState)) {
			if (random.nextInt(7) == 0) {
				this.advanceTree(serverLevel, blockPos, blockState, random);
			}
		} else {
			if (!isFullyGrown(blockState)) {
				serverLevel.setBlock(blockPos, blockState.cycle(AGE), 2);
			}
		}
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		return !isHanging(blockState) || !isFullyGrown(blockState);
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		return isHanging(blockState) ? !isFullyGrown(blockState) : super.isBonemealSuccess(level, random, blockPos, blockState);
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
		if (isHanging(blockState) && !isFullyGrown(blockState)) {
			serverLevel.setBlock(blockPos, blockState.cycle(AGE), 2);
		} else {
			super.performBonemeal(serverLevel, random, blockPos, blockState);
		}
	}

	private static boolean isHanging(BlockState blockState) {
		return (Boolean)blockState.getValue(HANGING);
	}

	private static boolean isFullyGrown(BlockState blockState) {
		return (Integer)blockState.getValue(AGE) == 4;
	}

	public static BlockState createNewHangingPropagule() {
		return Blocks.MANGROVE_PROPAGULE.defaultBlockState().setValue(HANGING, Boolean.valueOf(true)).setValue(AGE, Integer.valueOf(0));
	}
}
