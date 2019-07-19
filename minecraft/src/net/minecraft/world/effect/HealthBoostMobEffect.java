package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.BaseAttributeMap;

public class HealthBoostMobEffect extends MobEffect {
	public HealthBoostMobEffect(MobEffectCategory mobEffectCategory, int i) {
		super(mobEffectCategory, i);
	}

	@Override
	public void removeAttributeModifiers(LivingEntity livingEntity, BaseAttributeMap baseAttributeMap, int i) {
		super.removeAttributeModifiers(livingEntity, baseAttributeMap, i);
		if (livingEntity.getHealth() > livingEntity.getMaxHealth()) {
			livingEntity.setHealth(livingEntity.getMaxHealth());
		}
	}
}
