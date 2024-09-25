package net.minecraft.world.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.Level;

class WindChargedMobEffect extends MobEffect {
	protected WindChargedMobEffect(MobEffectCategory mobEffectCategory, int i) {
		super(mobEffectCategory, i, ParticleTypes.SMALL_GUST);
	}

	@Override
	public void onMobRemoved(ServerLevel serverLevel, LivingEntity livingEntity, int i, Entity.RemovalReason removalReason) {
		if (removalReason == Entity.RemovalReason.KILLED) {
			double d = livingEntity.getX();
			double e = livingEntity.getY() + (double)(livingEntity.getBbHeight() / 2.0F);
			double f = livingEntity.getZ();
			float g = 3.0F + livingEntity.getRandom().nextFloat() * 2.0F;
			serverLevel.explode(
				livingEntity,
				null,
				AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR,
				d,
				e,
				f,
				g,
				false,
				Level.ExplosionInteraction.TRIGGER,
				ParticleTypes.GUST_EMITTER_SMALL,
				ParticleTypes.GUST_EMITTER_LARGE,
				SoundEvents.BREEZE_WIND_CHARGE_BURST
			);
		}
	}
}
