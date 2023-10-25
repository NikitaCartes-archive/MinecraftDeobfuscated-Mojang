package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class CopperBulbBlock extends Block {
	public static final MapCodec<CopperBulbBlock> CODEC = simpleCodec(CopperBulbBlock::new);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	@Override
	protected MapCodec<? extends CopperBulbBlock> codec() {
		return CODEC;
	}

	public CopperBulbBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(LIT, Boolean.valueOf(false)).setValue(POWERED, Boolean.valueOf(false)));
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (blockState2.getBlock() != blockState.getBlock()) {
			level.scheduleTick(blockPos, this, 1);
		}
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		boolean bl2 = level.hasNeighborSignal(blockPos);
		if (bl2 != (Boolean)blockState.getValue(POWERED)) {
			level.scheduleTick(blockPos, this, 1);
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		boolean bl = serverLevel.hasNeighborSignal(blockPos);
		if (bl != (Boolean)blockState.getValue(POWERED)) {
			BlockState blockState2 = blockState;
			if (!(Boolean)blockState.getValue(POWERED)) {
				blockState2 = blockState.cycle(LIT);
				serverLevel.playSound(null, blockPos, blockState2.getValue(LIT) ? SoundEvents.COPPER_BULB_TURN_ON : SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS);
			}

			serverLevel.setBlock(blockPos, blockState2.setValue(POWERED, Boolean.valueOf(bl)), 3);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT, POWERED);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return level.getBlockState(blockPos).getValue(LIT) ? 15 : 0;
	}
}
