package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RedstoneWallTorchBlock extends RedstoneTorchBlock {
	public static final MapCodec<RedstoneWallTorchBlock> CODEC = simpleCodec(RedstoneWallTorchBlock::new);
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

	@Override
	public MapCodec<RedstoneWallTorchBlock> codec() {
		return CODEC;
	}

	protected RedstoneWallTorchBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.valueOf(true)));
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return WallTorchBlock.getShape(blockState);
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return WallTorchBlock.canSurvive(levelReader, blockPos, blockState.getValue(FACING));
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
		return direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(levelReader, blockPos) ? Blocks.AIR.defaultBlockState() : blockState;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = Blocks.WALL_TORCH.getStateForPlacement(blockPlaceContext);
		return blockState == null ? null : this.defaultBlockState().setValue(FACING, (Direction)blockState.getValue(FACING));
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if ((Boolean)blockState.getValue(LIT)) {
			Direction direction = ((Direction)blockState.getValue(FACING)).getOpposite();
			double d = 0.27;
			double e = (double)blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 0.2 + 0.27 * (double)direction.getStepX();
			double f = (double)blockPos.getY() + 0.7 + (randomSource.nextDouble() - 0.5) * 0.2 + 0.22;
			double g = (double)blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 0.2 + 0.27 * (double)direction.getStepZ();
			level.addParticle(DustParticleOptions.REDSTONE, e, f, g, 0.0, 0.0, 0.0);
		}
	}

	@Override
	protected boolean hasNeighborSignal(Level level, BlockPos blockPos, BlockState blockState) {
		Direction direction = ((Direction)blockState.getValue(FACING)).getOpposite();
		return level.hasSignal(blockPos.relative(direction), direction);
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(LIT) && blockState.getValue(FACING) != direction ? 15 : 0;
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}

	@Nullable
	@Override
	protected Orientation randomOrientation(Level level, BlockState blockState) {
		return ExperimentalRedstoneUtils.initialOrientation(level, ((Direction)blockState.getValue(FACING)).getOpposite(), Direction.UP);
	}
}
