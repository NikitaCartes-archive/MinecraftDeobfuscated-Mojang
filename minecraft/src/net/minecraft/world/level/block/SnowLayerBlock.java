package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SnowLayerBlock extends Block {
	public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
	protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[]{
		Shapes.empty(),
		Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
	};

	protected SnowLayerBlock(Block.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(1)));
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		switch (pathComputationType) {
			case LAND:
				return (Integer)blockState.getValue(LAYERS) < 5;
			case WATER:
				return false;
			case AIR:
				return false;
			default:
				return false;
		}
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE_BY_LAYER[blockState.getValue(LAYERS)];
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE_BY_LAYER[blockState.getValue(LAYERS) - 1];
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockState blockState2 = levelReader.getBlockState(blockPos.below());
		Block block = blockState2.getBlock();
		return block != Blocks.ICE && block != Blocks.PACKED_ICE && block != Blocks.BARRIER
			? Block.isFaceFull(blockState2.getCollisionShape(levelReader, blockPos.below()), Direction.UP)
				|| block == this && (Integer)blockState2.getValue(LAYERS) == 8
			: false;
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return !blockState.canSurvive(levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if (serverLevel.getBrightness(LightLayer.BLOCK, blockPos) > 11) {
			dropResources(blockState, serverLevel, blockPos);
			serverLevel.removeBlock(blockPos, false);
		}
	}

	@Override
	public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		int i = (Integer)blockState.getValue(LAYERS);
		if (blockPlaceContext.getItemInHand().getItem() != this.asItem() || i >= 8) {
			return i == 1;
		} else {
			return blockPlaceContext.replacingClickedOnBlock() ? blockPlaceContext.getClickedFace() == Direction.UP : true;
		}
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos());
		if (blockState.getBlock() == this) {
			int i = (Integer)blockState.getValue(LAYERS);
			return blockState.setValue(LAYERS, Integer.valueOf(Math.min(8, i + 1)));
		} else {
			return super.getStateForPlacement(blockPlaceContext);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LAYERS);
	}
}
