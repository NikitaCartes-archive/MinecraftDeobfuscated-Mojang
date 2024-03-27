package net.minecraft.world.effect;

import java.util.function.ToIntFunction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.Level;

class InfestedMobEffect extends MobEffect {
	private final float chanceToSpawn;
	private final ToIntFunction<RandomSource> spawnedCount;

	protected InfestedMobEffect(MobEffectCategory mobEffectCategory, int i, float f, ToIntFunction<RandomSource> toIntFunction) {
		super(mobEffectCategory, i, ParticleTypes.INFESTED);
		this.chanceToSpawn = f;
		this.spawnedCount = toIntFunction;
	}

	@Override
	public void onMobHurt(LivingEntity livingEntity, int i, DamageSource damageSource, float f) {
		if (livingEntity.getRandom().nextFloat() <= this.chanceToSpawn) {
			int j = this.spawnedCount.applyAsInt(livingEntity.getRandom());

			for (int k = 0; k < j; k++) {
				this.spawnSilverfish(livingEntity.level(), livingEntity.getX(), livingEntity.getY() + 0.5, livingEntity.getZ());
			}
		}
	}

	private void spawnSilverfish(Level level, double d, double e, double f) {
		Silverfish silverfish = EntityType.SILVERFISH.create(level);
		if (silverfish != null) {
			silverfish.moveTo(d, e, f, level.getRandom().nextFloat() * 360.0F, 0.0F);
			level.addFreshEntity(silverfish);
		}
	}
}
