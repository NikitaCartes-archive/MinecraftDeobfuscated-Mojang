package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public enum DefaultExplosionDamageCalculator implements ExplosionDamageCalculator {
	INSTANCE;

	@Override
	public Optional<Float> getBlockExplosionResistance(
		Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState
	) {
		return blockState.isAir() && fluidState.isEmpty()
			? Optional.empty()
			: Optional.of(Math.max(blockState.getBlock().getExplosionResistance(), fluidState.getExplosionResistance()));
	}

	@Override
	public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float f) {
		return true;
	}
}