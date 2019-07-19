/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttackDamageMobEffect
extends MobEffect {
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

