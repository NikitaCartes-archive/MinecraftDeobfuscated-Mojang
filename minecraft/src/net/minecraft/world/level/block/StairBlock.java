package net.minecraft.world.level.block;

import java.util.Random;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StairBlock extends Block implements SimpleWaterloggedBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
	public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final VoxelShape TOP_AABB = SlabBlock.TOP_AABB;
	protected static final VoxelShape BOTTOM_AABB = SlabBlock.BOTTOM_AABB;
	protected static final VoxelShape OCTET_NNN = Block.box(0.0, 0.0, 0.0, 8.0, 8.0, 8.0);
	protected static final VoxelShape OCTET_NNP = Block.box(0.0, 0.0, 8.0, 8.0, 8.0, 16.0);
	protected static final VoxelShape OCTET_NPN = Block.box(0.0, 8.0, 0.0, 8.0, 16.0, 8.0);
	protected static final VoxelShape OCTET_NPP = Block.box(0.0, 8.0, 8.0, 8.0, 16.0, 16.0);
	protected static final VoxelShape OCTET_PNN = Block.box(8.0, 0.0, 0.0, 16.0, 8.0, 8.0);
	protected static final VoxelShape OCTET_PNP = Block.box(8.0, 0.0, 8.0, 16.0, 8.0, 16.0);
	protected static final VoxelShape OCTET_PPN = Block.box(8.0, 8.0, 0.0, 16.0, 16.0, 8.0);
	protected static final VoxelShape OCTET_PPP = Block.box(8.0, 8.0, 8.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape[] TOP_SHAPES = makeShapes(TOP_AABB, OCTET_NNN, OCTET_PNN, OCTET_NNP, OCTET_PNP);
	protected static final VoxelShape[] BOTTOM_SHAPES = makeShapes(BOTTOM_AABB, OCTET_NPN, OCTET_PPN, OCTET_NPP, OCTET_PPP);
	private static final int[] SHAPE_BY_STATE = new int[]{12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8};
	private final Block base;
	private final BlockState baseState;

	private static VoxelShape[] makeShapes(VoxelShape voxelShape, VoxelShape voxelShape2, VoxelShape voxelShape3, VoxelShape voxelShape4, VoxelShape voxelShape5) {
		return (VoxelShape[])IntStream.range(0, 16)
			.mapToObj(i -> makeStairShape(i, voxelShape, voxelShape2, voxelShape3, voxelShape4, voxelShape5))
			.toArray(VoxelShape[]::new);
	}

	private static VoxelShape makeStairShape(
		int i, VoxelShape voxelShape, VoxelShape voxelShape2, VoxelShape voxelShape3, VoxelShape voxelShape4, VoxelShape voxelShape5
	) {
		VoxelShape voxelShape6 = voxelShape;
		if ((i & 1) != 0) {
			voxelShape6 = Shapes.or(voxelShape, voxelShape2);
		}

		if ((i & 2) != 0) {
			voxelShape6 = Shapes.or(voxelShape6, voxelShape3);
		}

		if ((i & 4) != 0) {
			voxelShape6 = Shapes.or(voxelShape6, voxelShape4);
		}

		if ((i & 8) != 0) {
			voxelShape6 = Shapes.or(voxelShape6, voxelShape5);
		}

		return voxelShape6;
	}

	protected StairBlock(BlockState blockState, BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(HALF, Half.BOTTOM)
				.setValue(SHAPE, StairsShape.STRAIGHT)
				.setValue(WATERLOGGED, Boolean.valueOf(false))
		);
		this.base = blockState.getBlock();
		this.baseState = blockState;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (blockState.getValue(HALF) == Half.TOP ? TOP_SHAPES : BOTTOM_SHAPES)[SHAPE_BY_STATE[this.getShapeIndex(blockState)]];
	}

	private int getShapeIndex(BlockState blockState) {
		return ((StairsShape)blockState.getValue(SHAPE)).ordinal() * 4 + ((Direction)blockState.getValue(FACING)).get2DDataValue();
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		this.base.animateTick(blockState, level, blockPos, random);
	}

	@Override
	public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
		this.baseState.attack(level, blockPos, player);
	}

	@Override
	public void destroy(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
		this.base.destroy(levelAccessor, blockPos, blockState);
	}

	@Override
	public float getExplosionResistance() {
		return this.base.getExplosionResistance();
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState.getBlock())) {
			this.baseState.neighborChanged(level, blockPos, Blocks.AIR, blockPos, false);
			this.base.onPlace(this.baseState, level, blockPos, blockState2, false);
		}
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			this.baseState.onRemove(level, blockPos, blockState2, bl);
		}
	}

	@Override
	public void stepOn(Level level, BlockPos blockPos, Entity entity) {
		this.base.stepOn(level, blockPos, entity);
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState) {
		return this.base.isRandomlyTicking(blockState);
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		this.base.randomTick(blockState, serverLevel, blockPos, random);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		this.base.tick(blockState, serverLevel, blockPos, random);
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		return this.baseState.use(level, player, interactionHand, blockHitResult);
	}

	@Override
	public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
		this.base.wasExploded(level, blockPos, explosion);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		Direction direction = blockPlaceContext.getClickedFace();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPos);
		BlockState blockState = this.defaultBlockState()
			.setValue(FACING, blockPlaceContext.getHorizontalDirection())
			.setValue(
				HALF,
				direction != Direction.DOWN && (direction == Direction.UP || !(blockPlaceContext.getClickLocation().y - (double)blockPos.getY() > 0.5))
					? Half.BOTTOM
					: Half.TOP
			)
			.setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
		return blockState.setValue(SHAPE, getStairsShape(blockState, blockPlaceContext.getLevel(), blockPos));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if ((Boolean)blockState.getValue(WATERLOGGED)) {
			levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
		}

		return direction.getAxis().isHorizontal()
			? blockState.setValue(SHAPE, getStairsShape(blockState, levelAccessor, blockPos))
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	private static StairsShape getStairsShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		Direction direction = blockState.getValue(FACING);
		BlockState blockState2 = blockGetter.getBlockState(blockPos.relative(direction));
		if (isStairs(blockState2) && blockState.getValue(HALF) == blockState2.getValue(HALF)) {
			Direction direction2 = blockState2.getValue(FACING);
			if (direction2.getAxis() != ((Direction)blockState.getValue(FACING)).getAxis() && canTakeShape(blockState, blockGetter, blockPos, direction2.getOpposite())) {
				if (direction2 == direction.getCounterClockWise()) {
					return StairsShape.OUTER_LEFT;
				}

				return StairsShape.OUTER_RIGHT;
			}
		}

		BlockState blockState3 = blockGetter.getBlockState(blockPos.relative(direction.getOpposite()));
		if (isStairs(blockState3) && blockState.getValue(HALF) == blockState3.getValue(HALF)) {
			Direction direction3 = blockState3.getValue(FACING);
			if (direction3.getAxis() != ((Direction)blockState.getValue(FACING)).getAxis() && canTakeShape(blockState, blockGetter, blockPos, direction3)) {
				if (direction3 == direction.getCounterClockWise()) {
					return StairsShape.INNER_LEFT;
				}

				return StairsShape.INNER_RIGHT;
			}
		}

		return StairsShape.STRAIGHT;
	}

	private static boolean canTakeShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		BlockState blockState2 = blockGetter.getBlockState(blockPos.relative(direction));
		return !isStairs(blockState2) || blockState2.getValue(FACING) != blockState.getValue(FACING) || blockState2.getValue(HALF) != blockState.getValue(HALF);
	}

	public static boolean isStairs(BlockState blockState) {
		return blockState.getBlock() instanceof StairBlock;
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		Direction direction = blockState.getValue(FACING);
		StairsShape stairsShape = blockState.getValue(SHAPE);
		switch (mirror) {
			case LEFT_RIGHT:
				if (direction.getAxis() == Direction.Axis.Z) {
					switch (stairsShape) {
						case INNER_LEFT:
							return blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
						case INNER_RIGHT:
							return blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
						case OUTER_LEFT:
							return blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
						case OUTER_RIGHT:
							return blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
						default:
							return blockState.rotate(Rotation.CLOCKWISE_180);
					}
				}
				break;
			case FRONT_BACK:
				if (direction.getAxis() == Direction.Axis.X) {
					switch (stairsShape) {
						case INNER_LEFT:
							return blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
						case INNER_RIGHT:
							return blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
						case OUTER_LEFT:
							return blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
						case OUTER_RIGHT:
							return blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
						case STRAIGHT:
							return blockState.rotate(Rotation.CLOCKWISE_180);
					}
				}
		}

		return super.mirror(blockState, mirror);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, HALF, SHAPE, WATERLOGGED);
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
