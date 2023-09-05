package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;

class AbsorptionMobEffect extends MobEffect {
	protected AbsorptionMobEffect(MobEffectCategory mobEffectCategory, int i) {
		super(mobEffectCategory, i);
	}

	@Override
	public void applyEffectTick(LivingEntity livingEntity, int i) {
		super.applyEffectTick(livingEntity, i);
		if (livingEntity.getAbsorptionAmount() <= 0.0F && !livingEntity.level().isClientSide) {
			livingEntity.removeEffect(this);
		}
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int i, int j) {
		return true;
	}

	@Override
	public void onEffectStarted(LivingEntity livingEntity, int i) {
		super.onEffectStarted(livingEntity, i);
		livingEntity.setAbsorptionAmount(Math.max(livingEntity.getAbsorptionAmount(), (float)(4 * (1 + i))));
	}
}
