/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.effect;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class MobEffect {
    private final Map<Attribute, AttributeModifier> attributeModifiers = Maps.newHashMap();
    private final MobEffectCategory category;
    private final int color;
    @Nullable
    private String descriptionId;

    @Nullable
    public static MobEffect byId(int i) {
        return (MobEffect)Registry.MOB_EFFECT.byId(i);
    }

    public static int getId(MobEffect mobEffect) {
        return Registry.MOB_EFFECT.getId(mobEffect);
    }

    protected MobEffect(MobEffectCategory mobEffectCategory, int i) {
        this.category = mobEffectCategory;
        this.color = i;
    }

    public void applyEffectTick(LivingEntity livingEntity, int i) {
        if (this == MobEffects.REGENERATION) {
            if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
                livingEntity.heal(1.0f);
            }
        } else if (this == MobEffects.POISON) {
            if (livingEntity.getHealth() > 1.0f) {
                livingEntity.hurt(DamageSource.MAGIC, 1.0f);
            }
        } else if (this == MobEffects.WITHER) {
            livingEntity.hurt(DamageSource.WITHER, 1.0f);
        } else if (this == MobEffects.HUNGER && livingEntity instanceof Player) {
            ((Player)livingEntity).causeFoodExhaustion(0.005f * (float)(i + 1));
        } else if (this == MobEffects.SATURATION && livingEntity instanceof Player) {
            if (!livingEntity.level.isClientSide) {
                ((Player)livingEntity).getFoodData().eat(i + 1, 1.0f);
            }
        } else if (this == MobEffects.HEAL && !livingEntity.isInvertedHealAndHarm() || this == MobEffects.HARM && livingEntity.isInvertedHealAndHarm()) {
            livingEntity.heal(Math.max(4 << i, 0));
        } else if (this == MobEffects.HARM && !livingEntity.isInvertedHealAndHarm() || this == MobEffects.HEAL && livingEntity.isInvertedHealAndHarm()) {
            livingEntity.hurt(DamageSource.MAGIC, 6 << i);
        }
    }

    public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity2, LivingEntity livingEntity, int i, double d) {
        if (this == MobEffects.HEAL && !livingEntity.isInvertedHealAndHarm() || this == MobEffects.HARM && livingEntity.isInvertedHealAndHarm()) {
            int j = (int)(d * (double)(4 << i) + 0.5);
            livingEntity.heal(j);
        } else if (this == MobEffects.HARM && !livingEntity.isInvertedHealAndHarm() || this == MobEffects.HEAL && livingEntity.isInvertedHealAndHarm()) {
            int j = (int)(d * (double)(6 << i) + 0.5);
            if (entity == null) {
                livingEntity.hurt(DamageSource.MAGIC, j);
            } else {
                livingEntity.hurt(DamageSource.indirectMagic(entity, entity2), j);
            }
        } else {
            this.applyEffectTick(livingEntity, i);
        }
    }

    public boolean isDurationEffectTick(int i, int j) {
        if (this == MobEffects.REGENERATION) {
            int k = 50 >> j;
            if (k > 0) {
                return i % k == 0;
            }
            return true;
        }
        if (this == MobEffects.POISON) {
            int k = 25 >> j;
            if (k > 0) {
                return i % k == 0;
            }
            return true;
        }
        if (this == MobEffects.WITHER) {
            int k = 40 >> j;
            if (k > 0) {
                return i % k == 0;
            }
            return true;
        }
        return this == MobEffects.HUNGER;
    }

    public boolean isInstantenous() {
        return false;
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("effect", Registry.MOB_EFFECT.getKey(this));
        }
        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public Component getDisplayName() {
        return new TranslatableComponent(this.getDescriptionId(), new Object[0]);
    }

    @Environment(value=EnvType.CLIENT)
    public MobEffectCategory getCategory() {
        return this.category;
    }

    public int getColor() {
        return this.color;
    }

    public MobEffect addAttributeModifier(Attribute attribute, String string, double d, AttributeModifier.Operation operation) {
        AttributeModifier attributeModifier = new AttributeModifier(UUID.fromString(string), this::getDescriptionId, d, operation);
        this.attributeModifiers.put(attribute, attributeModifier);
        return this;
    }

    @Environment(value=EnvType.CLIENT)
    public Map<Attribute, AttributeModifier> getAttributeModifiers() {
        return this.attributeModifiers;
    }

    public void removeAttributeModifiers(LivingEntity livingEntity, BaseAttributeMap baseAttributeMap, int i) {
        for (Map.Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entrySet()) {
            AttributeInstance attributeInstance = baseAttributeMap.getInstance(entry.getKey());
            if (attributeInstance == null) continue;
            attributeInstance.removeModifier(entry.getValue());
        }
    }

    public void addAttributeModifiers(LivingEntity livingEntity, BaseAttributeMap baseAttributeMap, int i) {
        for (Map.Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entrySet()) {
            AttributeInstance attributeInstance = baseAttributeMap.getInstance(entry.getKey());
            if (attributeInstance == null) continue;
            AttributeModifier attributeModifier = entry.getValue();
            attributeInstance.removeModifier(attributeModifier);
            attributeInstance.addModifier(new AttributeModifier(attributeModifier.getId(), this.getDescriptionId() + " " + i, this.getAttributeModifierValue(i, attributeModifier), attributeModifier.getOperation()));
        }
    }

    public double getAttributeModifierValue(int i, AttributeModifier attributeModifier) {
        return attributeModifier.getAmount() * (double)(i + 1);
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isBeneficial() {
        return this.category == MobEffectCategory.BENEFICIAL;
    }
}

