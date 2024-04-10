package net.minecraft.world.effect;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
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
	protected static int numberOfSlimesToSpawn(int i, int j, int k) {
		return Mth.clamp(0, i - j, k);
	}

	@Override
	public void onMobRemoved(LivingEntity livingEntity, int i, Entity.RemovalReason removalReason) {
		if (removalReason == Entity.RemovalReason.KILLED) {
			int j = this.spawnedCount.applyAsInt(livingEntity.getRandom());
			Level level = livingEntity.level();
			int k = level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
			List<Slime> list = new ArrayList();
			level.getEntities(EntityType.SLIME, livingEntity.getBoundingBox().inflate(2.0), slime -> slime != livingEntity, list, k);
			int l = numberOfSlimesToSpawn(k, list.size(), j);

			for (int m = 0; m < l; m++) {
				this.spawnSlimeOffspring(livingEntity.level(), livingEntity.getX(), livingEntity.getY() + 0.5, livingEntity.getZ());
			}
		}
	}

	private void spawnSlimeOffspring(Level level, double d, double e, double f) {
		Slime slime = EntityType.SLIME.create(level);
		if (slime != null) {
			slime.setSize(2, true);
			slime.moveTo(d, e, f, level.getRandom().nextFloat() * 360.0F, 0.0F);
			level.addFreshEntity(slime);
		}
	}
}
