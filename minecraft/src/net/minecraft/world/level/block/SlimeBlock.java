package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SlimeBlock extends HalfTransparentBlock {
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
			double e = -vec3.y;
			if (Rules.BOUNCY_CASTLE.get()) {
				double f = Math.max(0.0, -(2.0 - e));
				e = Math.max(f * 5.5, e);
				e *= 2.0;
			}

			entity.setDeltaMovement(vec3.x, e * d, vec3.z);
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

	@Override
	public boolean canStickToStuff(BlockState blockState) {
		return true;
	}

	@Override
	public boolean isStickyToNeighbour(
		Level level, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2, Direction direction, Direction direction2
	) {
		return blockState2.is(Blocks.HONEY_BLOCK)
			? false
			: !Rules.STICKY.get() || !blockState2.getFaceOcclusionShape(level, blockPos2, direction.getOpposite()).isEmpty();
	}
}
