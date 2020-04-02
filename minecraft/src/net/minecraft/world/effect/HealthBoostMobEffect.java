package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class HealthBoostMobEffect extends MobEffect {
	public HealthBoostMobEffect(MobEffectCategory mobEffectCategory, int i) {
		super(mobEffectCategory, i);
	}

	@Override
	public void removeAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int i) {
		super.removeAttributeModifiers(livingEntity, attributeMap, i);
		if (livingEntity.getHealth() > livingEntity.getMaxHealth()) {
			livingEntity.setHealth(livingEntity.getMaxHealth());
		}
	}
}
