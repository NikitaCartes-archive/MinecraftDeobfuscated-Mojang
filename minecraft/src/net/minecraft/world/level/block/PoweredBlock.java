package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PoweredBlock extends Block {
	public static final MapCodec<PoweredBlock> CODEC = simpleCodec(PoweredBlock::new);

	@Override
	public MapCodec<PoweredBlock> codec() {
		return CODEC;
	}

	public PoweredBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected boolean isSignalSource(BlockState blockState) {
		return true;
	}

	@Override
	protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return 15;
	}
}
