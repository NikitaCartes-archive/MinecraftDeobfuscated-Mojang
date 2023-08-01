package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;

class RegenerationMobEffect extends MobEffect {
	protected RegenerationMobEffect(MobEffectCategory mobEffectCategory, int i) {
		super(mobEffectCategory, i);
	}

	@Override
	public void applyEffectTick(LivingEntity livingEntity, int i) {
		super.applyEffectTick(livingEntity, i);
		if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
			livingEntity.heal(1.0F);
		}
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int i, int j) {
		int k = 50 >> j;
		return k > 0 ? i % k == 0 : true;
	}
}
