package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SlimeBlock extends HalfTransparentBlock {
	public SlimeBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public BlockLayer getRenderLayer() {
		return BlockLayer.TRANSLUCENT;
	}

	@Override
	public void fallOn(Level level, BlockPos blockPos, Entity entity, float f) {
		if (entity.isSuppressingBounce()) {
			super.fallOn(level, blockPos, entity, f);
		} else {
			entity.causeFallDamage(f, 0.0F);
		}
	}

	@Override
	public void updateEntityAfterFallOn(BlockGetter blockGetter, Entity entity) {
		if (entity.isSuppressingBounce()) {
			super.updateEntityAfterFallOn(blockGetter, entity);
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
	public void stepOn(Level level, BlockPos blockPos, Entity entity) {
		double d = Math.abs(entity.getDeltaMovement().y);
		if (d < 0.1 && !entity.isSteppingCarefully()) {
			double e = 0.4 + d * 0.2;
			entity.setDeltaMovement(entity.getDeltaMovement().multiply(e, 1.0, e));
		}

		super.stepOn(level, blockPos, entity);
	}
}
