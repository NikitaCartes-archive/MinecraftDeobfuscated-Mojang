package net.minecraft.world.effect;

import java.util.function.ToIntFunction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;

class OozingMobEffect extends MobEffect {
	private final ToIntFunction<RandomSource> spawnedCount;

	protected OozingMobEffect(MobEffectCategory mobEffectCategory, int i, ToIntFunction<RandomSource> toIntFunction) {
		super(mobEffectCategory, i, ParticleTypes.ITEM_SLIME);
		this.spawnedCount = toIntFunction;
	}

	@Override
	public void onMobRemoved(LivingEntity livingEntity, int i, Entity.RemovalReason removalReason) {
		if (removalReason == Entity.RemovalReason.KILLED) {
			int j = this.spawnedCount.applyAsInt(livingEntity.getRandom());

			for (int k = 0; k < j; k++) {
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
