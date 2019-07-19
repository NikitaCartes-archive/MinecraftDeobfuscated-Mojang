package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FrostedIceBlock extends IceBlock {
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

	public FrostedIceBlock(Block.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
	}

	@Override
	public void tick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if ((random.nextInt(3) == 0 || this.fewerNeigboursThan(level, blockPos, 4))
			&& level.getMaxLocalRawBrightness(blockPos) > 11 - (Integer)blockState.getValue(AGE) - blockState.getLightBlock(level, blockPos)
			&& this.slightlyMelt(blockState, level, blockPos)) {
			try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.acquire()) {
				for (Direction direction : Direction.values()) {
					pooledMutableBlockPos.set(blockPos).move(direction);
					BlockState blockState2 = level.getBlockState(pooledMutableBlockPos);
					if (blockState2.getBlock() == this && !this.slightlyMelt(blockState2, level, pooledMutableBlockPos)) {
						level.getBlockTicks().scheduleTick(pooledMutableBlockPos, this, Mth.nextInt(random, 20, 40));
					}
				}
			}
		} else {
			level.getBlockTicks().scheduleTick(blockPos, this, Mth.nextInt(random, 20, 40));
		}
	}

	private boolean slightlyMelt(BlockState blockState, Level level, BlockPos blockPos) {
		int i = (Integer)blockState.getValue(AGE);
		if (i < 3) {
			level.setBlock(blockPos, blockState.setValue(AGE, Integer.valueOf(i + 1)), 2);
			return false;
		} else {
			this.melt(blockState, level, blockPos);
			return true;
		}
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (block == this && this.fewerNeigboursThan(level, blockPos, 2)) {
			this.melt(blockState, level, blockPos);
		}

		super.neighborChanged(blockState, level, blockPos, block, blockPos2, bl);
	}

	private boolean fewerNeigboursThan(BlockGetter blockGetter, BlockPos blockPos, int i) {
		int j = 0;

		try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.acquire()) {
			for (Direction direction : Direction.values()) {
				pooledMutableBlockPos.set(blockPos).move(direction);
				if (blockGetter.getBlockState(pooledMutableBlockPos).getBlock() == this) {
					if (++j >= i) {
						return false;
					}
				}
			}

			return true;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return ItemStack.EMPTY;
	}
}
