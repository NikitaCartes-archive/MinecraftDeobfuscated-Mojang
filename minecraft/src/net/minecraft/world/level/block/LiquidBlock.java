package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidBlock extends Block implements BucketPickup {
	public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
	protected final FlowingFluid fluid;
	private final List<FluidState> stateCache;
	public static final VoxelShape STABLE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
	public static final ImmutableList<Direction> POSSIBLE_FLOW_DIRECTIONS = ImmutableList.of(
		Direction.DOWN, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST
	);

	protected LiquidBlock(FlowingFluid flowingFluid, BlockBehaviour.Properties properties) {
		super(properties);
		this.fluid = flowingFluid;
		this.stateCache = Lists.<FluidState>newArrayList();
		this.stateCache.add(flowingFluid.getSource(false));

		for (int i = 1; i < 8; i++) {
			this.stateCache.add(flowingFluid.getFlowing(8 - i, false));
		}

		this.stateCache.add(flowingFluid.getFlowing(8, true));
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return collisionContext.isAbove(STABLE_SHAPE, blockPos, true)
				&& blockState.getValue(LEVEL) == 0
				&& collisionContext.canStandOnFluid(blockGetter.getFluidState(blockPos.above()), this.fluid)
			? STABLE_SHAPE
			: Shapes.empty();
	}

	@Override
	public boolean isRandomlyTicking(BlockState blockState) {
		return blockState.getFluidState().isRandomlyTicking();
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		blockState.getFluidState().randomTick(serverLevel, blockPos, random);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return false;
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
		return !this.fluid.is(FluidTags.LAVA);
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		int i = (Integer)blockState.getValue(LEVEL);
		return (FluidState)this.stateCache.get(Math.min(i, 8));
	}

	@Override
	public boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
		return blockState2.getFluidState().getType().isSame(this.fluid);
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public List<ItemStack> getDrops(BlockState blockState, LootContext.Builder builder) {
		return Collections.emptyList();
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return Shapes.empty();
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (this.shouldSpreadLiquid(level, blockPos, blockState)) {
			level.getLiquidTicks().scheduleTick(blockPos, blockState.getFluidState().getType(), this.fluid.getTickDelay(level));
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (blockState.getFluidState().isSource() || blockState2.getFluidState().isSource()) {
			levelAccessor.getLiquidTicks().scheduleTick(blockPos, blockState.getFluidState().getType(), this.fluid.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (this.shouldSpreadLiquid(level, blockPos, blockState)) {
			level.getLiquidTicks().scheduleTick(blockPos, blockState.getFluidState().getType(), this.fluid.getTickDelay(level));
		}
	}

	private boolean shouldSpreadLiquid(Level level, BlockPos blockPos, BlockState blockState) {
		if (this.fluid.is(FluidTags.LAVA)) {
			boolean bl = level.getBlockState(blockPos.below()).is(Blocks.SOUL_SOIL);

			for (Direction direction : POSSIBLE_FLOW_DIRECTIONS) {
				BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
				if (level.getFluidState(blockPos2).is(FluidTags.WATER)) {
					Block block = level.getFluidState(blockPos).isSource() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE;
					level.setBlockAndUpdate(blockPos, block.defaultBlockState());
					this.fizz(level, blockPos);
					return false;
				}

				if (bl && level.getBlockState(blockPos2).is(Blocks.BLUE_ICE)) {
					level.setBlockAndUpdate(blockPos, Blocks.BASALT.defaultBlockState());
					this.fizz(level, blockPos);
					return false;
				}
			}
		}

		return true;
	}

	private void fizz(LevelAccessor levelAccessor, BlockPos blockPos) {
		levelAccessor.levelEvent(1501, blockPos, 0);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}

	@Override
	public ItemStack pickupBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
		if ((Integer)blockState.getValue(LEVEL) == 0) {
			levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
			return new ItemStack(this.fluid.getBucket());
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public Optional<SoundEvent> getPickupSound() {
		return this.fluid.getPickupSound();
	}
}
