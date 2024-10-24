package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface BonemealableBlock {
	boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState);

	boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState);

	void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState);

	default BlockPos getParticlePos(BlockPos blockPos) {
		return switch (this.getType()) {
			case NEIGHBOR_SPREADER -> blockPos.above();
			case GROWER -> blockPos;
		};
	}

	default BonemealableBlock.Type getType() {
		return BonemealableBlock.Type.GROWER;
	}

	public static enum Type {
		NEIGHBOR_SPREADER,
		GROWER;
	}
}
