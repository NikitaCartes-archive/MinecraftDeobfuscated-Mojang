package net.minecraft.world.effect;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;

class WeavingMobEffect extends MobEffect {
	private final ToIntFunction<RandomSource> maxCobwebs;

	protected WeavingMobEffect(MobEffectCategory mobEffectCategory, int i, ToIntFunction<RandomSource> toIntFunction) {
		super(mobEffectCategory, i, ParticleTypes.ITEM_COBWEB);
		this.maxCobwebs = toIntFunction;
	}

	@Override
	public void onMobRemoved(ServerLevel serverLevel, LivingEntity livingEntity, int i, Entity.RemovalReason removalReason) {
		if (removalReason == Entity.RemovalReason.KILLED && (livingEntity instanceof Player || serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))) {
			this.spawnCobwebsRandomlyAround(serverLevel, livingEntity.getRandom(), livingEntity.blockPosition());
		}
	}

	private void spawnCobwebsRandomlyAround(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos) {
		Set<BlockPos> set = Sets.<BlockPos>newHashSet();
		int i = this.maxCobwebs.applyAsInt(randomSource);

		for (BlockPos blockPos2 : BlockPos.randomInCube(randomSource, 15, blockPos, 1)) {
			BlockPos blockPos3 = blockPos2.below();
			if (!set.contains(blockPos2)
				&& serverLevel.getBlockState(blockPos2).canBeReplaced()
				&& serverLevel.getBlockState(blockPos3).isFaceSturdy(serverLevel, blockPos3, Direction.UP)) {
				set.add(blockPos2.immutable());
				if (set.size() >= i) {
					break;
				}
			}
		}

		for (BlockPos blockPos2x : set) {
			serverLevel.setBlock(blockPos2x, Blocks.COBWEB.defaultBlockState(), 3);
			serverLevel.levelEvent(3018, blockPos2x, 0);
		}
	}
}
