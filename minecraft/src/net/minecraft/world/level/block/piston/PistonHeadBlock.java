package net.minecraft.world.level.block.piston;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonHeadBlock extends DirectionalBlock {
	public static final EnumProperty<PistonType> TYPE = BlockStateProperties.PISTON_TYPE;
	public static final BooleanProperty SHORT = BlockStateProperties.SHORT;
	protected static final VoxelShape EAST_AABB = Block.box(12.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape WEST_AABB = Block.box(0.0, 0.0, 0.0, 4.0, 16.0, 16.0);
	protected static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 12.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 4.0);
	protected static final VoxelShape UP_AABB = Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape DOWN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
	protected static final VoxelShape UP_ARM_AABB = Block.box(6.0, -4.0, 6.0, 10.0, 12.0, 10.0);
	protected static final VoxelShape DOWN_ARM_AABB = Block.box(6.0, 4.0, 6.0, 10.0, 20.0, 10.0);
	protected static final VoxelShape SOUTH_ARM_AABB = Block.box(6.0, 6.0, -4.0, 10.0, 10.0, 12.0);
	protected static final VoxelShape NORTH_ARM_AABB = Block.box(6.0, 6.0, 4.0, 10.0, 10.0, 20.0);
	protected static final VoxelShape EAST_ARM_AABB = Block.box(-4.0, 6.0, 6.0, 12.0, 10.0, 10.0);
	protected static final VoxelShape WEST_ARM_AABB = Block.box(4.0, 6.0, 6.0, 20.0, 10.0, 10.0);
	protected static final VoxelShape SHORT_UP_ARM_AABB = Block.box(6.0, 0.0, 6.0, 10.0, 12.0, 10.0);
	protected static final VoxelShape SHORT_DOWN_ARM_AABB = Block.box(6.0, 4.0, 6.0, 10.0, 16.0, 10.0);
	protected static final VoxelShape SHORT_SOUTH_ARM_AABB = Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 12.0);
	protected static final VoxelShape SHORT_NORTH_ARM_AABB = Block.box(6.0, 6.0, 4.0, 10.0, 10.0, 16.0);
	protected static final VoxelShape SHORT_EAST_ARM_AABB = Block.box(0.0, 6.0, 6.0, 12.0, 10.0, 10.0);
	protected static final VoxelShape SHORT_WEST_ARM_AABB = Block.box(4.0, 6.0, 6.0, 16.0, 10.0, 10.0);
	private static final VoxelShape[] SHAPES_SHORT = makeShapes(true);
	private static final VoxelShape[] SHAPES_LONG = makeShapes(false);

	private static VoxelShape[] makeShapes(boolean bl) {
		return (VoxelShape[])Arrays.stream(Direction.values()).map(direction -> calculateShape(direction, bl)).toArray(VoxelShape[]::new);
	}

	private static VoxelShape calculateShape(Direction direction, boolean bl) {
		switch (direction) {
			case DOWN:
			default:
				return Shapes.or(DOWN_AABB, bl ? SHORT_DOWN_ARM_AABB : DOWN_ARM_AABB);
			case UP:
				return Shapes.or(UP_AABB, bl ? SHORT_UP_ARM_AABB : UP_ARM_AABB);
			case NORTH:
				return Shapes.or(NORTH_AABB, bl ? SHORT_NORTH_ARM_AABB : NORTH_ARM_AABB);
			case SOUTH:
				return Shapes.or(SOUTH_AABB, bl ? SHORT_SOUTH_ARM_AABB : SOUTH_ARM_AABB);
			case WEST:
				return Shapes.or(WEST_AABB, bl ? SHORT_WEST_ARM_AABB : WEST_ARM_AABB);
			case EAST:
				return Shapes.or(EAST_AABB, bl ? SHORT_EAST_ARM_AABB : EAST_ARM_AABB);
		}
	}

	public PistonHeadBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, PistonType.DEFAULT).setValue(SHORT, Boolean.valueOf(false))
		);
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (blockState.getValue(SHORT) ? SHAPES_SHORT : SHAPES_LONG)[((Direction)blockState.getValue(FACING)).ordinal()];
	}

	private boolean isFittingBase(BlockState blockState, BlockState blockState2) {
		Block block = blockState.getValue(TYPE) == PistonType.DEFAULT ? Blocks.PISTON : Blocks.STICKY_PISTON;
		return blockState2.is(block) && (Boolean)blockState2.getValue(PistonBaseBlock.EXTENDED) && blockState2.getValue(FACING) == blockState.getValue(FACING);
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
		if (!level.isClientSide && player.abilities.instabuild) {
			BlockPos blockPos2 = blockPos.relative(((Direction)blockState.getValue(FACING)).getOpposite());
			if (this.isFittingBase(blockState, level.getBlockState(blockPos2))) {
				level.destroyBlock(blockPos2, false);
			}
		}

		super.playerWillDestroy(level, blockPos, blockState, player);
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			super.onRemove(blockState, level, blockPos, blockState2, bl);
			BlockPos blockPos2 = blockPos.relative(((Direction)blockState.getValue(FACING)).getOpposite());
			if (this.isFittingBase(blockState, level.getBlockState(blockPos2))) {
				level.destroyBlock(blockPos2, true);
			}
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(levelAccessor, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		BlockState blockState2 = levelReader.getBlockState(blockPos.relative(((Direction)blockState.getValue(FACING)).getOpposite()));
		return this.isFittingBase(blockState, blockState2) || blockState2.is(Blocks.MOVING_PISTON) && blockState2.getValue(FACING) == blockState.getValue(FACING);
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (blockState.canSurvive(level, blockPos)) {
			BlockPos blockPos3 = blockPos.relative(((Direction)blockState.getValue(FACING)).getOpposite());
			level.getBlockState(blockPos3).neighborChanged(level, blockPos3, block, blockPos2, false);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return new ItemStack(blockState.getValue(TYPE) == PistonType.STICKY ? Blocks.STICKY_PISTON : Blocks.PISTON);
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
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, TYPE, SHORT);
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return false;
	}
}
