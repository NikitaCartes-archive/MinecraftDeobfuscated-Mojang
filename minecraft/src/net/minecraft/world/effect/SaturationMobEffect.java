package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

class SaturationMobEffect extends InstantenousMobEffect {
	protected SaturationMobEffect(MobEffectCategory mobEffectCategory, int i) {
		super(mobEffectCategory, i);
	}

	@Override
	public boolean applyEffectTick(LivingEntity livingEntity, int i) {
		if (!livingEntity.level().isClientSide && livingEntity instanceof Player player) {
			player.getFoodData().eat(i + 1, 1.0F);
		}

		return true;
	}
}
