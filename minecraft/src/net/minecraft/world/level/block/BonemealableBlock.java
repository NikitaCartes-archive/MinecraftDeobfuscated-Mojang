package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface BonemealableBlock {
	boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl);

	boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState);

	void performBonemeal(Level level, Random random, BlockPos blockPos, BlockState blockState);
}
