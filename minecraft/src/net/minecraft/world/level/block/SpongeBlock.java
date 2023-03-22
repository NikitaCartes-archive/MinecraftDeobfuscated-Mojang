package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;

public class SpongeBlock extends Block {
	public static final int MAX_DEPTH = 6;
	public static final int MAX_COUNT = 64;
	private static final Direction[] ALL_DIRECTIONS = Direction.values();

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
		return BlockPos.breadthFirstTraversal(blockPos, 6, 65, (blockPosx, consumer) -> {
			for (Direction direction : ALL_DIRECTIONS) {
				consumer.accept(blockPosx.relative(direction));
			}
		}, blockPos2 -> {
			if (blockPos2.equals(blockPos)) {
				return true;
			} else {
				BlockState blockState = level.getBlockState(blockPos2);
				FluidState fluidState = level.getFluidState(blockPos2);
				Material material = blockState.getMaterial();
				if (!fluidState.is(FluidTags.WATER)) {
					return false;
				} else {
					if (!(blockState.getBlock() instanceof BucketPickup bucketPickup) || bucketPickup.pickupBlock(level, blockPos2, blockState).isEmpty()) {
						if (blockState.getBlock() instanceof LiquidBlock) {
							level.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 3);
						} else {
							if (material != Material.WATER_PLANT && material != Material.REPLACEABLE_WATER_PLANT) {
								return false;
							}

							BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos2) : null;
							dropResources(blockState, level, blockPos2, blockEntity);
							level.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 3);
						}
					}

					return true;
				}
			}
		}) > 1;
	}
}
