package net.minecraft.world.effect;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
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
	private final Map<Holder<Attribute>, MobEffect.AttributeTemplate> attributeModifiers = new Object2ObjectOpenHashMap<>();
	private final MobEffectCategory category;
	private final int color;
	@Nullable
	private String descriptionId;
	private int blendDurationTicks;

	protected MobEffect(MobEffectCategory mobEffectCategory, int i) {
		this.category = mobEffectCategory;
		this.color = i;
	}

	public int getBlendDurationTicks() {
		return this.blendDurationTicks;
	}

	public boolean applyEffectTick(LivingEntity livingEntity, int i) {
		return true;
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

	public MobEffect addAttributeModifier(Holder<Attribute> holder, String string, double d, AttributeModifier.Operation operation) {
		this.attributeModifiers.put(holder, new MobEffect.AttributeTemplate(UUID.fromString(string), d, operation));
		return this;
	}

	public MobEffect setBlendDuration(int i) {
		this.blendDurationTicks = i;
		return this;
	}

	public void createModifiers(int i, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
		this.attributeModifiers.forEach((holder, attributeTemplate) -> biConsumer.accept(holder, attributeTemplate.create(this.getDescriptionId(), i)));
	}

	public void removeAttributeModifiers(AttributeMap attributeMap) {
		for (Entry<Holder<Attribute>, MobEffect.AttributeTemplate> entry : this.attributeModifiers.entrySet()) {
			AttributeInstance attributeInstance = attributeMap.getInstance((Holder<Attribute>)entry.getKey());
			if (attributeInstance != null) {
				attributeInstance.removeModifier(((MobEffect.AttributeTemplate)entry.getValue()).id());
			}
		}
	}

	public void addAttributeModifiers(AttributeMap attributeMap, int i) {
		for (Entry<Holder<Attribute>, MobEffect.AttributeTemplate> entry : this.attributeModifiers.entrySet()) {
			AttributeInstance attributeInstance = attributeMap.getInstance((Holder<Attribute>)entry.getKey());
			if (attributeInstance != null) {
				attributeInstance.removeModifier(((MobEffect.AttributeTemplate)entry.getValue()).id());
				attributeInstance.addPermanentModifier(((MobEffect.AttributeTemplate)entry.getValue()).create(this.getDescriptionId(), i));
			}
		}
	}

	public boolean isBeneficial() {
		return this.category == MobEffectCategory.BENEFICIAL;
	}

	static record AttributeTemplate(UUID id, double amount, AttributeModifier.Operation operation) {
		public AttributeModifier create(String string, int i) {
			return new AttributeModifier(this.id, string + " " + i, this.amount * (double)(i + 1), this.operation);
		}
	}
}
