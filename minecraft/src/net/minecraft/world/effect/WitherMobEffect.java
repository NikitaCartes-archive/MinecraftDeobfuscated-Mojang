package net.minecraft.world.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

class WitherMobEffect extends MobEffect {
	protected WitherMobEffect(MobEffectCategory mobEffectCategory, int i) {
		super(mobEffectCategory, i);
	}

	@Override
	public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity livingEntity, int i) {
		livingEntity.hurtServer(serverLevel, livingEntity.damageSources().wither(), 1.0F);
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int i, int j) {
		int k = 40 >> j;
		return k > 0 ? i % k == 0 : true;
	}
}
