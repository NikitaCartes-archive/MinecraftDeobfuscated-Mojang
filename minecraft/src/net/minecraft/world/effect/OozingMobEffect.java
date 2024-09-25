package net.minecraft.world.effect;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

class OozingMobEffect extends MobEffect {
	private static final int RADIUS_TO_CHECK_SLIMES = 2;
	public static final int SLIME_SIZE = 2;
	private final ToIntFunction<RandomSource> spawnedCount;

	protected OozingMobEffect(MobEffectCategory mobEffectCategory, int i, ToIntFunction<RandomSource> toIntFunction) {
		super(mobEffectCategory, i, ParticleTypes.ITEM_SLIME);
		this.spawnedCount = toIntFunction;
	}

	@VisibleForTesting
	protected static int numberOfSlimesToSpawn(int i, OozingMobEffect.NearbySlimes nearbySlimes, int j) {
		return i < 1 ? j : Mth.clamp(0, i - nearbySlimes.count(i), j);
	}

	@Override
	public void onMobRemoved(ServerLevel serverLevel, LivingEntity livingEntity, int i, Entity.RemovalReason removalReason) {
		if (removalReason == Entity.RemovalReason.KILLED) {
			int j = this.spawnedCount.applyAsInt(livingEntity.getRandom());
			int k = serverLevel.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
			int l = numberOfSlimesToSpawn(k, OozingMobEffect.NearbySlimes.closeTo(livingEntity), j);

			for (int m = 0; m < l; m++) {
				this.spawnSlimeOffspring(livingEntity.level(), livingEntity.getX(), livingEntity.getY() + 0.5, livingEntity.getZ());
			}
		}
	}

	private void spawnSlimeOffspring(Level level, double d, double e, double f) {
		Slime slime = EntityType.SLIME.create(level, EntitySpawnReason.TRIGGERED);
		if (slime != null) {
			slime.setSize(2, true);
			slime.moveTo(d, e, f, level.getRandom().nextFloat() * 360.0F, 0.0F);
			level.addFreshEntity(slime);
		}
	}

	@FunctionalInterface
	protected interface NearbySlimes {
		int count(int i);

		static OozingMobEffect.NearbySlimes closeTo(LivingEntity livingEntity) {
			return i -> {
				List<Slime> list = new ArrayList();
				livingEntity.level().getEntities(EntityType.SLIME, livingEntity.getBoundingBox().inflate(2.0), slime -> slime != livingEntity, list, i);
				return list.size();
			};
		}
	}
}
