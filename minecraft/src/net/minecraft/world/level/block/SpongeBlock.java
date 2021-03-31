package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.Queue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;

public class SpongeBlock extends Block {
	public static final int MAX_DEPTH = 6;
	public static final int MAX_COUNT = 64;

	protected SpongeBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock())) {
			this.tryAbsorbWater(level, blockPos);
		}
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		this.tryAbsorbWater(level, blockPos);
		super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
	}

	protected void tryAbsorbWater(Level level, BlockPos blockPos) {
		if (this.removeWaterBreadthFirstSearch(level, blockPos)) {
			level.setBlock(blockPos, Blocks.WET_SPONGE.defaultBlockState(), 2);
			level.levelEvent(2001, blockPos, Block.getId(Blocks.WATER.defaultBlockState()));
		}
	}

	private boolean removeWaterBreadthFirstSearch(Level level, BlockPos blockPos) {
		Queue<Tuple<BlockPos, Integer>> queue = Lists.<Tuple<BlockPos, Integer>>newLinkedList();
		queue.add(new Tuple<>(blockPos, 0));
		int i = 0;

		while (!queue.isEmpty()) {
			Tuple<BlockPos, Integer> tuple = (Tuple<BlockPos, Integer>)queue.poll();
			BlockPos blockPos2 = tuple.getA();
			int j = tuple.getB();

			for (Direction direction : Direction.values()) {
				BlockPos blockPos3 = blockPos2.relative(direction);
				BlockState blockState = level.getBlockState(blockPos3);
				FluidState fluidState = level.getFluidState(blockPos3);
				Material material = blockState.getMaterial();
				if (fluidState.is(FluidTags.WATER)) {
					if (blockState.getBlock() instanceof BucketPickup && !((BucketPickup)blockState.getBlock()).pickupBlock(level, blockPos3, blockState).isEmpty()) {
						i++;
						if (j < 6) {
							queue.add(new Tuple<>(blockPos3, j + 1));
						}
					} else if (blockState.getBlock() instanceof LiquidBlock) {
						level.setBlock(blockPos3, Blocks.AIR.defaultBlockState(), 3);
						i++;
						if (j < 6) {
							queue.add(new Tuple<>(blockPos3, j + 1));
						}
					} else if (material == Material.WATER_PLANT || material == Material.REPLACEABLE_WATER_PLANT) {
						BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos3) : null;
						dropResources(blockState, level, blockPos3, blockEntity);
						level.setBlock(blockPos3, Blocks.AIR.defaultBlockState(), 3);
						i++;
						if (j < 6) {
							queue.add(new Tuple<>(blockPos3, j + 1));
						}
					}
				}
			}

			if (i > 64) {
				break;
			}
		}

		return i > 0;
	}
}
