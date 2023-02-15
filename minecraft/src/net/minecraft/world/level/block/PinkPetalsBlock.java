package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PinkPetalsBlock extends BushBlock implements BonemealableBlock {
	public static final int MIN_FLOWERS = 1;
	public static final int MAX_FLOWERS = 4;
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final IntegerProperty AMOUNT = BlockStateProperties.FLOWER_AMOUNT;

	protected PinkPetalsBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(AMOUNT, Integer.valueOf(1)));
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		return !blockPlaceContext.isSecondaryUseActive() && blockPlaceContext.getItemInHand().is(this.asItem()) && blockState.getValue(AMOUNT) < 4
			? true
			: super.canBeReplaced(blockState, blockPlaceContext);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
		return blockState.is(this)
			? blockState.setValue(AMOUNT, Integer.valueOf(Math.min(4, (Integer)blockState.getValue(AMOUNT) + 1)))
			: this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, AMOUNT);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean bl) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		int i = (Integer)blockState.getValue(AMOUNT);
		if (i < 4) {
			serverLevel.setBlock(blockPos, blockState.setValue(AMOUNT, Integer.valueOf(i + 1)), 2);
		} else {
			popResource(serverLevel, blockPos, new ItemStack(this));
		}
	}
}
