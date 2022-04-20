package net.minecraft.world.effect;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public class MobEffect {
	private final Map<Attribute, AttributeModifier> attributeModifiers = Maps.<Attribute, AttributeModifier>newHashMap();
	private final MobEffectCategory category;
	private final int color;
	@Nullable
	private String descriptionId;
	private Supplier<MobEffectInstance.FactorData> factorDataFactory = () -> null;

	@Nullable
	public static MobEffect byId(int i) {
		return Registry.MOB_EFFECT.byId(i);
	}

	public static int getId(MobEffect mobEffect) {
		return Registry.MOB_EFFECT.getId(mobEffect);
	}

	public static int getIdFromNullable(@Nullable MobEffect mobEffect) {
		return Registry.MOB_EFFECT.getId(mobEffect);
	}

	protected MobEffect(MobEffectCategory mobEffectCategory, int i) {
		this.category = mobEffectCategory;
		this.color = i;
	}

	public Optional<MobEffectInstance.FactorData> createFactorData() {
		return Optional.ofNullable((MobEffectInstance.FactorData)this.factorDataFactory.get());
	}

	public void applyEffectTick(LivingEntity livingEntity, int i) {
		if (this == MobEffects.REGENERATION) {
			if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
				livingEntity.heal(1.0F);
			}
		} else if (this == MobEffects.POISON) {
			if (livingEntity.getHealth() > 1.0F) {
				livingEntity.hurt(DamageSource.MAGIC, 1.0F);
			}
		} else if (this == MobEffects.WITHER) {
			livingEntity.hurt(DamageSource.WITHER, 1.0F);
		} else if (this == MobEffects.HUNGER && livingEntity instanceof Player) {
			((Player)livingEntity).causeFoodExhaustion(0.005F * (float)(i + 1));
		} else if (this == MobEffects.SATURATION && livingEntity instanceof Player) {
			if (!livingEntity.level.isClientSide) {
				((Player)livingEntity).getFoodData().eat(i + 1, 1.0F);
			}
		} else if ((this != MobEffects.HEAL || livingEntity.isInvertedHealAndHarm()) && (this != MobEffects.HARM || !livingEntity.isInvertedHealAndHarm())) {
			if (this == MobEffects.HARM && !livingEntity.isInvertedHealAndHarm() || this == MobEffects.HEAL && livingEntity.isInvertedHealAndHarm()) {
				livingEntity.hurt(DamageSource.MAGIC, (float)(6 << i));
			}
		} else {
			livingEntity.heal((float)Math.max(4 << i, 0));
		}
	}

	public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity2, LivingEntity livingEntity, int i, double d) {
		if ((this != MobEffects.HEAL || livingEntity.isInvertedHealAndHarm()) && (this != MobEffects.HARM || !livingEntity.isInvertedHealAndHarm())) {
			if (this == MobEffects.HARM && !livingEntity.isInvertedHealAndHarm() || this == MobEffects.HEAL && livingEntity.isInvertedHealAndHarm()) {
				int j = (int)(d * (double)(6 << i) + 0.5);
				if (entity == null) {
					livingEntity.hurt(DamageSource.MAGIC, (float)j);
				} else {
					livingEntity.hurt(DamageSource.indirectMagic(entity, entity2), (float)j);
				}
			} else {
				this.applyEffectTick(livingEntity, i);
			}
		} else {
			int j = (int)(d * (double)(4 << i) + 0.5);
			livingEntity.heal((float)j);
		}
	}

	public boolean isDurationEffectTick(int i, int j) {
		if (this == MobEffects.REGENERATION) {
			int k = 50 >> j;
			return k > 0 ? i % k == 0 : true;
		} else if (this == MobEffects.POISON) {
			int k = 25 >> j;
			return k > 0 ? i % k == 0 : true;
		} else if (this == MobEffects.WITHER) {
			int k = 40 >> j;
			return k > 0 ? i % k == 0 : true;
		} else {
			return this == MobEffects.HUNGER;
		}
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
		return Component.translatable(this.getDescriptionId());
	}

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

	public MobEffect setFactorDataFactory(Supplier<MobEffectInstance.FactorData> supplier) {
		this.factorDataFactory = supplier;
		return this;
	}

	public Map<Attribute, AttributeModifier> getAttributeModifiers() {
		return this.attributeModifiers;
	}

	public void removeAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int i) {
		for (Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entrySet()) {
			AttributeInstance attributeInstance = attributeMap.getInstance((Attribute)entry.getKey());
			if (attributeInstance != null) {
				attributeInstance.removeModifier((AttributeModifier)entry.getValue());
			}
		}
	}

	public void addAttributeModifiers(LivingEntity livingEntity, AttributeMap attributeMap, int i) {
		for (Entry<Attribute, AttributeModifier> entry : this.attributeModifiers.entrySet()) {
			AttributeInstance attributeInstance = attributeMap.getInstance((Attribute)entry.getKey());
			if (attributeInstance != null) {
				AttributeModifier attributeModifier = (AttributeModifier)entry.getValue();
				attributeInstance.removeModifier(attributeModifier);
				attributeInstance.addPermanentModifier(
					new AttributeModifier(
						attributeModifier.getId(), this.getDescriptionId() + " " + i, this.getAttributeModifierValue(i, attributeModifier), attributeModifier.getOperation()
					)
				);
			}
		}
	}

	public double getAttributeModifierValue(int i, AttributeModifier attributeModifier) {
		return attributeModifier.getAmount() * (double)(i + 1);
	}

	public boolean isBeneficial() {
		return this.category == MobEffectCategory.BENEFICIAL;
	}
}
