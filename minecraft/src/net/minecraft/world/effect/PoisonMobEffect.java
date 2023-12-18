package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;

class PoisonMobEffect extends MobEffect {
	protected PoisonMobEffect(MobEffectCategory mobEffectCategory, int i) {
		super(mobEffectCategory, i);
	}

	@Override
	public boolean applyEffectTick(LivingEntity livingEntity, int i) {
		if (livingEntity.getHealth() > 1.0F) {
			livingEntity.hurt(livingEntity.damageSources().magic(), 1.0F);
		}

		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int i, int j) {
		int k = 25 >> j;
		return k > 0 ? i % k == 0 : true;
	}
}
