package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class ExplosionDamageCalculator {
	public Optional<Float> getBlockExplosionResistance(
		Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState
	) {
		return blockState.isAir() && fluidState.isEmpty()
			? Optional.empty()
			: Optional.of(Math.max(blockState.getBlock().getExplosionResistance(), fluidState.getExplosionResistance()));
	}

	public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float f) {
		return true;
	}

	public float getEntityDamageAmount(Explosion explosion, Entity entity) {
		float f = explosion.radius() * 2.0F;
		Vec3 vec3 = explosion.center();
		double d = Math.sqrt(entity.distanceToSqr(vec3)) / (double)f;
		double e = (1.0 - d) * (double)Explosion.getSeenPercent(vec3, entity);
		return (float)((e * e + e) / 2.0 * 7.0 * (double)f + 1.0);
	}
}
