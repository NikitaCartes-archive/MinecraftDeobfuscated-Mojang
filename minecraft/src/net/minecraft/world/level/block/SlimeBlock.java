package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SlimeBlock extends HalfTransparentBlock {
	public static final MapCodec<SlimeBlock> CODEC = simpleCodec(SlimeBlock::new);

	@Override
	public MapCodec<SlimeBlock> codec() {
		return CODEC;
	}

	public SlimeBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, float f) {
		if (entity.isSuppressingBounce()) {
			super.fallOn(level, blockState, blockPos, entity, f);
		} else {
			entity.causeFallDamage(f, 0.0F, level.damageSources().fall());
		}
	}

	@Override
	public void updateEntityMovementAfterFallOn(BlockGetter blockGetter, Entity entity) {
		if (entity.isSuppressingBounce()) {
			super.updateEntityMovementAfterFallOn(blockGetter, entity);
		} else {
			this.bounceUp(entity);
		}
	}

	private void bounceUp(Entity entity) {
		Vec3 vec3 = entity.getDeltaMovement();
		if (vec3.y < 0.0) {
			double d = entity instanceof LivingEntity ? 1.0 : 0.8;
			entity.setDeltaMovement(vec3.x, -vec3.y * d, vec3.z);
		}
	}

	@Override
	public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
		double d = Math.abs(entity.getDeltaMovement().y);
		if (d < 0.1 && !entity.isSteppingCarefully()) {
			double e = 0.4 + d * 0.2;
			entity.setDeltaMovement(entity.getDeltaMovement().multiply(e, 1.0, e));
		}

		super.stepOn(level, blockPos, blockState, entity);
	}
}
