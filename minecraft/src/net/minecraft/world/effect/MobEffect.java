package net.minecraft.world.effect;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class MobEffect {
	private final Map<Attribute, AttributeModifierTemplate> attributeModifiers = Maps.<Attribute, AttributeModifierTemplate>newHashMap();
	private final MobEffectCategory category;
	private final int color;
	@Nullable
	private String descriptionId;
	private Supplier<MobEffectInstance.FactorData> factorDataFactory = () -> null;
	private final Holder.Reference<MobEffect> builtInRegistryHolder = BuiltInRegistries.MOB_EFFECT.createIntrusiveHolder(this);

	protected MobEffect(MobEffectCategory mobEffectCategory, int i) {
		this.category = mobEffectCategory;
		this.color = i;
	}

	public Optional<MobEffectInstance.FactorData> createFactorData() {
		return Optional.ofNullable((MobEffectInstance.FactorData)this.factorDataFactory.get());
	}

	public void applyEffectTick(LivingEntity livingEntity, int i) {
	}

	public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity2, LivingEntity livingEntity, int i, double d) {
		this.applyEffectTick(livingEntity, i);
	}

	public boolean shouldApplyEffectTickThisTick(int i, int j) {
		return false;
	}

	public void onEffectStarted(LivingEntity livingEntity, int i) {
	}

	public boolean isInstantenous() {
		return false;
	}

	protected String getOrCreateDescriptionId() {
		if (this.descriptionId == null) {
			this.descriptionId = Util.makeDescriptionId("effect", BuiltInRegistries.MOB_EFFECT.getKey(this));
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
		this.attributeModifiers.put(attribute, new MobEffect.MobEffectAttributeModifierTemplate(UUID.fromString(string), d, operation));
		return this;
	}

	public MobEffect setFactorDataFactory(Supplier<MobEffectInstance.FactorData> supplier) {
		this.factorDataFactory = supplier;
		return this;
	}

	public Map<Attribute, AttributeModifierTemplate> getAttributeModifiers() {
		return this.attributeModifiers;
	}

	public void removeAttributeModifiers(AttributeMap attributeMap) {
		for (Entry<Attribute, AttributeModifierTemplate> entry : this.attributeModifiers.entrySet()) {
			AttributeInstance attributeInstance = attributeMap.getInstance((Attribute)entry.getKey());
			if (attributeInstance != null) {
				attributeInstance.removeModifier(((AttributeModifierTemplate)entry.getValue()).getAttributeModifierId());
			}
		}
	}

	public void addAttributeModifiers(AttributeMap attributeMap, int i) {
		for (Entry<Attribute, AttributeModifierTemplate> entry : this.attributeModifiers.entrySet()) {
			AttributeInstance attributeInstance = attributeMap.getInstance((Attribute)entry.getKey());
			if (attributeInstance != null) {
				attributeInstance.removeModifier(((AttributeModifierTemplate)entry.getValue()).getAttributeModifierId());
				attributeInstance.addPermanentModifier(((AttributeModifierTemplate)entry.getValue()).create(i));
			}
		}
	}

	public boolean isBeneficial() {
		return this.category == MobEffectCategory.BENEFICIAL;
	}

	@Deprecated
	public Holder.Reference<MobEffect> builtInRegistryHolder() {
		return this.builtInRegistryHolder;
	}

	class MobEffectAttributeModifierTemplate implements AttributeModifierTemplate {
		private final UUID id;
		private final double amount;
		private final AttributeModifier.Operation operation;

		public MobEffectAttributeModifierTemplate(UUID uUID, double d, AttributeModifier.Operation operation) {
			this.id = uUID;
			this.amount = d;
			this.operation = operation;
		}

		@Override
		public UUID getAttributeModifierId() {
			return this.id;
		}

		@Override
		public AttributeModifier create(int i) {
			return new AttributeModifier(this.id, MobEffect.this.getDescriptionId() + " " + i, this.amount * (double)(i + 1), this.operation);
		}
	}
}
