package net.minecraft.world.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

class WeavingMobEffect extends MobEffect {
	private final ToIntFunction<RandomSource> maxCobwebs;

	protected WeavingMobEffect(MobEffectCategory mobEffectCategory, int i, ToIntFunction<RandomSource> toIntFunction) {
		super(mobEffectCategory, i, ParticleTypes.ITEM_COBWEB);
		this.maxCobwebs = toIntFunction;
	}

	@Override
	public void onMobRemoved(LivingEntity livingEntity, int i, Entity.RemovalReason removalReason) {
		if (removalReason == Entity.RemovalReason.KILLED) {
			this.spawnCobwebsRandomlyAround(livingEntity.level(), livingEntity.getRandom(), livingEntity.getOnPos());
		}
	}

	private void spawnCobwebsRandomlyAround(Level level, RandomSource randomSource, BlockPos blockPos) {
		List<BlockPos> list = new ArrayList();
		int i = this.maxCobwebs.applyAsInt(randomSource);

		for (BlockPos blockPos2 : BlockPos.randomInCube(randomSource, 10, blockPos, 3)) {
			BlockPos blockPos3 = blockPos2.below();
			if (level.getBlockState(blockPos2).isAir() && level.getBlockState(blockPos3).isFaceSturdy(level, blockPos3, Direction.UP)) {
				list.add(blockPos2.immutable());
				if (list.size() >= i) {
					break;
				}
			}
		}

		for (BlockPos blockPos2x : list) {
			level.setBlock(blockPos2x, Blocks.COBWEB.defaultBlockState(), 3);
			level.levelEvent(3018, blockPos2x, 0);
		}
	}
}
