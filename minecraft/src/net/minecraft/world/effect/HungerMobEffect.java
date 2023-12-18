package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

class HungerMobEffect extends MobEffect {
	protected HungerMobEffect(MobEffectCategory mobEffectCategory, int i) {
		super(mobEffectCategory, i);
	}

	@Override
	public boolean applyEffectTick(LivingEntity livingEntity, int i) {
		if (livingEntity instanceof Player player) {
			player.causeFoodExhaustion(0.005F * (float)(i + 1));
		}

		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int i, int j) {
		return true;
	}
}
