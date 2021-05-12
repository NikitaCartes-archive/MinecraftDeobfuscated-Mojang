package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BigDripleafStemBlock extends HorizontalDirectionalBlock implements BonemealableBlock, SimpleWaterloggedBlock {
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final int STEM_WIDTH = 6;
	protected static final VoxelShape NORTH_SHAPE = Block.box(5.0, 0.0, 9.0, 11.0, 16.0, 15.0);
	protected static final VoxelShape SOUTH_SHAPE = Block.box(5.0, 0.0, 1.0, 11.0, 16.0, 7.0);
	protected static final VoxelShape EAST_SHAPE = Block.box(1.0, 0.0, 5.0, 7.0, 16.0, 11.0);
	protected static final VoxelShape WEST_SHAPE = Block.box(9.0, 0.0, 5.0, 15.0, 16.0, 11.0);

	protected BigDripleafStemBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH));
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		switch ((Direction)blockState.getValue(FACING)) {
			case SOUTH:
				return SOUTH_SHAPE;
			case NORTH:
			default:
				return NORTH_SHAPE;
			case WEST:
				return WEST_SHAPE;
			case EAST:
				return EAST_SHAPE;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED, FACING);
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState2 = levelReader.getBlockState(blockPos2);
		BlockState blockState3 = levelReader.getBlockState(blockPos.above());
		return (blockState2.is(this) || blockState2.isFaceSturdy(levelReader, blockPos2, Direction.UP))
			&& (blockState3.is(this) || blockState3.is(Blocks.BIG_DRIPLEAF));
	}

	protected static boolean place(LevelAccessor levelAccessor, BlockPos blockPos, FluidState fluidState, Direction direction) {
		BlockState blockState = Blocks.BIG_DRIPLEAF_STEM
			.defaultBlockState()
			.setValue(WATERLOGGED, Boolean.valueOf(fluidState.isSourceOfType(Fluids.WATER)))
			.setValue(FACING, direction);
		return levelAccessor.setBlock(blockPos, blockState, 3);
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((direction == Direction.DOWN || direction == Direction.UP) && !blockState.canSurvive(levelAccessor, blockPos)) {
			levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
		}

		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (!blockState.canSurvive(serverLevel, blockPos)) {
			serverLevel.destroyBlock(blockPos, true);
		}
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		Optional<BlockPos> optional = BlockUtil.getTopConnectedBlock(blockGetter, blockPos, blockState.getBlock(), Direction.UP, Blocks.BIG_DRIPLEAF);
		if (!optional.isPresent()) {
			return false;
		} else {
			BlockPos blockPos2 = ((BlockPos)optional.get()).above();
			BlockState blockState2 = blockGetter.getBlockState(blockPos2);
			return BigDripleafBlock.canPlaceAt(blockGetter, blockPos2, blockState2);
		}
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
		Optional<BlockPos> optional = BlockUtil.getTopConnectedBlock(serverLevel, blockPos, blockState.getBlock(), Direction.UP, Blocks.BIG_DRIPLEAF);
		if (optional.isPresent()) {
			BlockPos blockPos2 = (BlockPos)optional.get();
			BlockPos blockPos3 = blockPos2.above();
			Direction direction = blockState.getValue(FACING);
			place(serverLevel, blockPos2, serverLevel.getFluidState(blockPos2), direction);
			BigDripleafBlock.place(serverLevel, blockPos3, serverLevel.getFluidState(blockPos3), direction);
		}
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return new ItemStack(Blocks.BIG_DRIPLEAF);
	}
}
