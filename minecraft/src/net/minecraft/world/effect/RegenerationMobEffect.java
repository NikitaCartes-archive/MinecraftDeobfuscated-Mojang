package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;

class RegenerationMobEffect extends MobEffect {
	protected RegenerationMobEffect(MobEffectCategory mobEffectCategory, int i) {
		super(mobEffectCategory, i);
	}

	@Override
	public boolean applyEffectTick(LivingEntity livingEntity, int i) {
		if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
			livingEntity.heal(1.0F);
		}

		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int i, int j) {
		int k = 50 >> j;
		if (k > 0) {
			return i % k == 0;
		} else {
			return true;
		}
	}
}
