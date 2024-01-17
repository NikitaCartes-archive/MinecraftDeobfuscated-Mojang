package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FrostedIceBlock extends IceBlock {
	public static final MapCodec<FrostedIceBlock> CODEC = simpleCodec(FrostedIceBlock::new);
	public static final int MAX_AGE = 3;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
	private static final int NEIGHBORS_TO_AGE = 4;
	private static final int NEIGHBORS_TO_MELT = 2;

	@Override
	public MapCodec<FrostedIceBlock> codec() {
		return CODEC;
	}

	public FrostedIceBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
	}

	@Override
	protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		this.tick(blockState, serverLevel, blockPos, randomSource);
	}

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if ((randomSource.nextInt(3) == 0 || this.fewerNeigboursThan(serverLevel, blockPos, 4))
			&& serverLevel.getMaxLocalRawBrightness(blockPos) > 11 - (Integer)blockState.getValue(AGE) - blockState.getLightBlock(serverLevel, blockPos)
			&& this.slightlyMelt(blockState, serverLevel, blockPos)) {
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (Direction direction : Direction.values()) {
				mutableBlockPos.setWithOffset(blockPos, direction);
				BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos);
				if (blockState2.is(this) && !this.slightlyMelt(blockState2, serverLevel, mutableBlockPos)) {
					serverLevel.scheduleTick(mutableBlockPos, this, Mth.nextInt(randomSource, 20, 40));
				}
			}
		} else {
			serverLevel.scheduleTick(blockPos, this, Mth.nextInt(randomSource, 20, 40));
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
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if (block.defaultBlockState().is(this) && this.fewerNeigboursThan(level, blockPos, 2)) {
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

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return ItemStack.EMPTY;
	}
}
