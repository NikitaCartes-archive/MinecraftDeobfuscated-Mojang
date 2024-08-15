package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.redstone.Orientation;

public class SpongeBlock extends Block {
	public static final MapCodec<SpongeBlock> CODEC = simpleCodec(SpongeBlock::new);
	public static final int MAX_DEPTH = 6;
	public static final int MAX_COUNT = 64;
	private static final Direction[] ALL_DIRECTIONS = Direction.values();

	@Override
	public MapCodec<SpongeBlock> codec() {
		return CODEC;
	}

	protected SpongeBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState2.is(blockState.getBlock())) {
			this.tryAbsorbWater(level, blockPos);
		}
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
		this.tryAbsorbWater(level, blockPos);
		super.neighborChanged(blockState, level, blockPos, block, orientation, bl);
	}

	protected void tryAbsorbWater(Level level, BlockPos blockPos) {
		if (this.removeWaterBreadthFirstSearch(level, blockPos)) {
			level.setBlock(blockPos, Blocks.WET_SPONGE.defaultBlockState(), 2);
			level.playSound(null, blockPos, SoundEvents.SPONGE_ABSORB, SoundSource.BLOCKS, 1.0F, 1.0F);
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
				if (!fluidState.is(FluidTags.WATER)) {
					return false;
				} else {
					if (blockState.getBlock() instanceof BucketPickup bucketPickup && !bucketPickup.pickupBlock(null, level, blockPos2, blockState).isEmpty()) {
						return true;
					}

					if (blockState.getBlock() instanceof LiquidBlock) {
						level.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 3);
					} else {
						if (!blockState.is(Blocks.KELP) && !blockState.is(Blocks.KELP_PLANT) && !blockState.is(Blocks.SEAGRASS) && !blockState.is(Blocks.TALL_SEAGRASS)) {
							return false;
						}

						BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos2) : null;
						dropResources(blockState, level, blockPos2, blockEntity);
						level.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 3);
					}

					return true;
				}
			}
		}) > 1;
	}
}
