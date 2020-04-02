/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class HealthBoostMobEffect
extends MobEffect {
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

