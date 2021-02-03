package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SmallDripleafBlock extends DoublePlantBlock implements BonemealableBlock, SimpleWaterloggedBlock {
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

	public SmallDripleafBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(WATERLOGGED, Boolean.valueOf(false)));
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.is(Blocks.CLAY) || super.mayPlaceOn(blockState, blockGetter, blockPos);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = super.getStateForPlacement(blockPlaceContext);
		if (blockState != null) {
			FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
			return blockState.setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
		} else {
			return null;
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		level.setBlock(
			blockPos.above(),
			this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER).setValue(WATERLOGGED, Boolean.valueOf(level.isWaterAt(blockPos.above()))),
			3
		);
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		if (blockState.getValue(HALF) == DoubleBlockHalf.UPPER) {
			return super.canSurvive(blockState, levelReader, blockPos);
		} else {
			BlockPos blockPos2 = blockPos.below();
			BlockState blockState2 = levelReader.getBlockState(blockPos2);
			return blockState2.is(Blocks.CLAY)
				|| levelReader.getFluidState(blockPos).isSourceOfType(Fluids.WATER) && this.mayPlaceOn(blockState2, levelReader, blockPos2);
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF, WATERLOGGED);
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
		if (blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
			BigDripleafBlock.place(serverLevel, random, blockPos);
		} else {
			BlockPos blockPos2 = blockPos.below();
			this.performBonemeal(serverLevel, random, blockPos2, serverLevel.getBlockState(blockPos2));
		}
	}
}
