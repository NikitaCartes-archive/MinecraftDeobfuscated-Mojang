package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LeavesBlock extends Block {
	public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;
	public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;

	public LeavesBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, Integer.valueOf(7)).setValue(PERSISTENT, Boolean.valueOf(false)));
	}

	@Override
	public VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return Shapes.empty();
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState) {
		return (Integer)blockState.getValue(DISTANCE) == 7 && !(Boolean)blockState.getValue(PERSISTENT);
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (!(Boolean)blockState.getValue(PERSISTENT) && (Integer)blockState.getValue(DISTANCE) == 7) {
			dropResources(blockState, serverLevel, blockPos);
			serverLevel.removeBlock(blockPos, false);
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		serverLevel.setBlock(blockPos, updateDistance(blockState, serverLevel, blockPos), 3);
	}

	@Override
	public int getLightBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return 1;
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		int i = getDistanceAt(blockState2) + 1;
		if (i != 1 || (Integer)blockState.getValue(DISTANCE) != i) {
			levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
		}

		return blockState;
	}

	private static BlockState updateDistance(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
		int i = 7;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.values()) {
			mutableBlockPos.setWithOffset(blockPos, direction);
			i = Math.min(i, getDistanceAt(levelAccessor.getBlockState(mutableBlockPos)) + 1);
			if (i == 1) {
				break;
			}
		}

		return blockState.setValue(DISTANCE, Integer.valueOf(i));
	}

	private static int getDistanceAt(BlockState blockState) {
		if (BlockTags.LOGS.contains(blockState.getBlock())) {
			return 0;
		} else {
			return blockState.getBlock() instanceof LeavesBlock ? (Integer)blockState.getValue(DISTANCE) : 7;
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if (level.isRainingAt(blockPos.above())) {
			if (random.nextInt(15) == 1) {
				BlockPos blockPos2 = blockPos.below();
				BlockState blockState2 = level.getBlockState(blockPos2);
				if (!blockState2.canOcclude() || !blockState2.isFaceSturdy(level, blockPos2, Direction.UP)) {
					double d = (double)blockPos.getX() + random.nextDouble();
					double e = (double)blockPos.getY() - 0.05;
					double f = (double)blockPos.getZ() + random.nextDouble();
					level.addParticle(ParticleTypes.DRIPPING_WATER, d, e, f, 0.0, 0.0, 0.0);
				}
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DISTANCE, PERSISTENT);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return updateDistance(this.defaultBlockState().setValue(PERSISTENT, Boolean.valueOf(true)), blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
	}
}
