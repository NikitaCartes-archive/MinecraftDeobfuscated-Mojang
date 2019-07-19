package net.minecraft.world.effect;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttackDamageMobEffect extends MobEffect {
	protected final double multiplier;

	protected AttackDamageMobEffect(MobEffectCategory mobEffectCategory, int i, double d) {
		super(mobEffectCategory, i);
		this.multiplier = d;
	}

	@Override
	public double getAttributeModifierValue(int i, AttributeModifier attributeModifier) {
		return this.multiplier * (double)(i + 1);
	}
}
