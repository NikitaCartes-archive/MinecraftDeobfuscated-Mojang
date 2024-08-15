package net.minecraft.world.effect;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class MobEffect implements FeatureElement {
	public static final Codec<Holder<MobEffect>> CODEC = BuiltInRegistries.MOB_EFFECT.holderByNameCodec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<MobEffect>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT);
	private static final int AMBIENT_ALPHA = Mth.floor(38.25F);
	private final Map<Holder<Attribute>, MobEffect.AttributeTemplate> attributeModifiers = new Object2ObjectOpenHashMap<>();
	private final MobEffectCategory category;
	private final int color;
	private final Function<MobEffectInstance, ParticleOptions> particleFactory;
	@Nullable
	private String descriptionId;
	private int blendDurationTicks;
	private Optional<SoundEvent> soundOnAdded = Optional.empty();
	private FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;

	protected MobEffect(MobEffectCategory mobEffectCategory, int i) {
		this.category = mobEffectCategory;
		this.color = i;
		this.particleFactory = mobEffectInstance -> {
			int j = mobEffectInstance.isAmbient() ? AMBIENT_ALPHA : 255;
			return ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, ARGB.color(j, i));
		};
	}

	protected MobEffect(MobEffectCategory mobEffectCategory, int i, ParticleOptions particleOptions) {
		this.category = mobEffectCategory;
		this.color = i;
		this.particleFactory = mobEffectInstance -> particleOptions;
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

	public void onEffectAdded(LivingEntity livingEntity, int i) {
		this.soundOnAdded
			.ifPresent(
				soundEvent -> livingEntity.level()
						.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), soundEvent, livingEntity.getSoundSource(), 1.0F, 1.0F)
			);
	}

	public void onMobRemoved(LivingEntity livingEntity, int i, Entity.RemovalReason removalReason) {
	}

	public void onMobHurt(LivingEntity livingEntity, int i, DamageSource damageSource, float f) {
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

	public MobEffect addAttributeModifier(Holder<Attribute> holder, ResourceLocation resourceLocation, double d, AttributeModifier.Operation operation) {
		this.attributeModifiers.put(holder, new MobEffect.AttributeTemplate(resourceLocation, d, operation));
		return this;
	}

	public MobEffect setBlendDuration(int i) {
		this.blendDurationTicks = i;
		return this;
	}

	public void createModifiers(int i, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
		this.attributeModifiers.forEach((holder, attributeTemplate) -> biConsumer.accept(holder, attributeTemplate.create(i)));
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
				attributeInstance.addPermanentModifier(((MobEffect.AttributeTemplate)entry.getValue()).create(i));
			}
		}
	}

	public boolean isBeneficial() {
		return this.category == MobEffectCategory.BENEFICIAL;
	}

	public ParticleOptions createParticleOptions(MobEffectInstance mobEffectInstance) {
		return (ParticleOptions)this.particleFactory.apply(mobEffectInstance);
	}

	public MobEffect withSoundOnAdded(SoundEvent soundEvent) {
		this.soundOnAdded = Optional.of(soundEvent);
		return this;
	}

	public MobEffect requiredFeatures(FeatureFlag... featureFlags) {
		this.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlags);
		return this;
	}

	@Override
	public FeatureFlagSet requiredFeatures() {
		return this.requiredFeatures;
	}

	static record AttributeTemplate(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
		public AttributeModifier create(int i) {
			return new AttributeModifier(this.id, this.amount * (double)(i + 1), this.operation);
		}
	}
}
