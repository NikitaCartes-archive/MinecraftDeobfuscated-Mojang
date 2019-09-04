package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidBlock extends Block implements BucketPickup {
	public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
	protected final FlowingFluid fluid;
	private final List<FluidState> stateCache;

	protected LiquidBlock(FlowingFluid flowingFluid, Block.Properties properties) {
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
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		serverLevel.getFluidState(blockPos).randomTick(serverLevel, blockPos, random);
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

	@Environment(EnvType.CLIENT)
	@Override
	public boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
		return blockState2.getFluidState().getType().isSame(this.fluid) ? true : super.canOcclude(blockState);
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
	public int getTickDelay(LevelReader levelReader) {
		return this.fluid.getTickDelay(levelReader);
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (this.shouldSpreadLiquid(level, blockPos, blockState)) {
			level.getLiquidTicks().scheduleTick(blockPos, blockState.getFluidState().getType(), this.getTickDelay(level));
		}
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (blockState.getFluidState().isSource() || blockState2.getFluidState().isSource()) {
			levelAccessor.getLiquidTicks().scheduleTick(blockPos, blockState.getFluidState().getType(), this.getTickDelay(levelAccessor));
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (this.shouldSpreadLiquid(level, blockPos, blockState)) {
			level.getLiquidTicks().scheduleTick(blockPos, blockState.getFluidState().getType(), this.getTickDelay(level));
		}
	}

	public boolean shouldSpreadLiquid(Level level, BlockPos blockPos, BlockState blockState) {
		if (this.fluid.is(FluidTags.LAVA)) {
			boolean bl = false;

			for (Direction direction : Direction.values()) {
				if (direction != Direction.DOWN && level.getFluidState(blockPos.relative(direction)).is(FluidTags.WATER)) {
					bl = true;
					break;
				}
			}

			if (bl) {
				FluidState fluidState = level.getFluidState(blockPos);
				if (fluidState.isSource()) {
					level.setBlockAndUpdate(blockPos, Blocks.OBSIDIAN.defaultBlockState());
					this.fizz(level, blockPos);
					return false;
				}

				if (fluidState.getHeight(level, blockPos) >= 0.44444445F) {
					level.setBlockAndUpdate(blockPos, Blocks.COBBLESTONE.defaultBlockState());
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
	public Fluid takeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
		if ((Integer)blockState.getValue(LEVEL) == 0) {
			levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
			return this.fluid;
		} else {
			return Fluids.EMPTY;
		}
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (this.fluid.is(FluidTags.LAVA)) {
			entity.setInLava();
		}
	}
}
