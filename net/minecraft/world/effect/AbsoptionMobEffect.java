/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class AbsoptionMobEffect
extends MobEffect {
    protected AbsoptionMobEffect(MobEffectCategory mobEffectCategory, int i) {
        super(mobEffectCategory, i);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int i) {
        livingEntity.setAbsorptionAmount(livingEntity.getAbsorptionAmount() - (float)(4 * (i + 1)));
        super.removeAttributeModifiers(livingEntity, attributeMap, i);
    }

    @Override
    public void addAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int i) {
        livingEntity.setAbsorptionAmount(livingEntity.getAbsorptionAmount() + (float)(4 * (i + 1)));
        super.addAttributeModifiers(livingEntity, attributeMap, i);
    }
}

