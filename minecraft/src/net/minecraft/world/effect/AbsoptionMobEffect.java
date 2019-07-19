package net.minecraft.world.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.BaseAttributeMap;

public class AbsoptionMobEffect extends MobEffect {
	protected AbsoptionMobEffect(MobEffectCategory mobEffectCategory, int i) {
		super(mobEffectCategory, i);
	}

	@Override
	public void removeAttributeModifiers(LivingEntity livingEntity, BaseAttributeMap baseAttributeMap, int i) {
		livingEntity.setAbsorptionAmount(livingEntity.getAbsorptionAmount() - (float)(4 * (i + 1)));
		super.removeAttributeModifiers(livingEntity, baseAttributeMap, i);
	}

	@Override
	public void addAttributeModifiers(LivingEntity livingEntity, BaseAttributeMap baseAttributeMap, int i) {
		livingEntity.setAbsorptionAmount(livingEntity.getAbsorptionAmount() + (float)(4 * (i + 1)));
		super.addAttributeModifiers(livingEntity, baseAttributeMap, i);
	}
}
