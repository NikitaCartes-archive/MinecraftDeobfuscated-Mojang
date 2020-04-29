package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FrostedIceBlock extends IceBlock {
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

	public FrostedIceBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		this.tick(blockState, serverLevel, blockPos, random);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		if ((random.nextInt(3) == 0 || this.fewerNeigboursThan(serverLevel, blockPos, 4))
			&& serverLevel.getMaxLocalRawBrightness(blockPos) > 11 - (Integer)blockState.getValue(AGE) - blockState.getLightBlock(serverLevel, blockPos)
			&& this.slightlyMelt(blockState, serverLevel, blockPos)) {
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (Direction direction : Direction.values()) {
				mutableBlockPos.setWithOffset(blockPos, direction);
				BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos);
				if (blockState2.is(this) && !this.slightlyMelt(blockState2, serverLevel, mutableBlockPos)) {
					serverLevel.getBlockTicks().scheduleTick(mutableBlockPos, this, Mth.nextInt(random, 20, 40));
				}
			}
		} else {
			serverLevel.getBlockTicks().scheduleTick(blockPos, this, Mth.nextInt(random, 20, 40));
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
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.values()) {
			mutableBlockPos.setWithOffset(blockPos, direction);
			if (blockGetter.getBlockState(mutableBlockPos).is(this)) {
				if (++j >= i) {
					return false;
				}
			}
		}

		return true;
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
