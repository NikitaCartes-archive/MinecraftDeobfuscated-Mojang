package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record MobEffectsPredicate(Map<Holder<MobEffect>, MobEffectsPredicate.MobEffectInstancePredicate> effectMap) {
	public static final Codec<MobEffectsPredicate> CODEC = Codec.unboundedMap(
			BuiltInRegistries.MOB_EFFECT.holderByNameCodec(), MobEffectsPredicate.MobEffectInstancePredicate.CODEC
		)
		.xmap(MobEffectsPredicate::new, MobEffectsPredicate::effectMap);

	public boolean matches(Entity entity) {
		if (entity instanceof LivingEntity livingEntity && this.matches(livingEntity.getActiveEffectsMap())) {
			return true;
		}

		return false;
	}

	public boolean matches(LivingEntity livingEntity) {
		return this.matches(livingEntity.getActiveEffectsMap());
	}

	public boolean matches(Map<Holder<MobEffect>, MobEffectInstance> map) {
		for (Entry<Holder<MobEffect>, MobEffectsPredicate.MobEffectInstancePredicate> entry : this.effectMap.entrySet()) {
			MobEffectInstance mobEffectInstance = (MobEffectInstance)map.get(entry.getKey());
			if (!((MobEffectsPredicate.MobEffectInstancePredicate)entry.getValue()).matches(mobEffectInstance)) {
				return false;
			}
		}

		return true;
	}

	public static class Builder {
		private final ImmutableMap.Builder<Holder<MobEffect>, MobEffectsPredicate.MobEffectInstancePredicate> effectMap = ImmutableMap.builder();

		public static MobEffectsPredicate.Builder effects() {
			return new MobEffectsPredicate.Builder();
		}

		public MobEffectsPredicate.Builder and(Holder<MobEffect> holder) {
			this.effectMap.put(holder, new MobEffectsPredicate.MobEffectInstancePredicate());
			return this;
		}

		public MobEffectsPredicate.Builder and(Holder<MobEffect> holder, MobEffectsPredicate.MobEffectInstancePredicate mobEffectInstancePredicate) {
			this.effectMap.put(holder, mobEffectInstancePredicate);
			return this;
		}

		public Optional<MobEffectsPredicate> build() {
			return Optional.of(new MobEffectsPredicate(this.effectMap.build()));
		}
	}

	public static record MobEffectInstancePredicate(MinMaxBounds.Ints amplifier, MinMaxBounds.Ints duration, Optional<Boolean> ambient, Optional<Boolean> visible) {
		public static final Codec<MobEffectsPredicate.MobEffectInstancePredicate> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						MinMaxBounds.Ints.CODEC.optionalFieldOf("amplifier", MinMaxBounds.Ints.ANY).forGetter(MobEffectsPredicate.MobEffectInstancePredicate::amplifier),
						MinMaxBounds.Ints.CODEC.optionalFieldOf("duration", MinMaxBounds.Ints.ANY).forGetter(MobEffectsPredicate.MobEffectInstancePredicate::duration),
						Codec.BOOL.optionalFieldOf("ambient").forGetter(MobEffectsPredicate.MobEffectInstancePredicate::ambient),
						Codec.BOOL.optionalFieldOf("visible").forGetter(MobEffectsPredicate.MobEffectInstancePredicate::visible)
					)
					.apply(instance, MobEffectsPredicate.MobEffectInstancePredicate::new)
		);

		public MobEffectInstancePredicate() {
			this(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, Optional.empty(), Optional.empty());
		}

		public boolean matches(@Nullable MobEffectInstance mobEffectInstance) {
			if (mobEffectInstance == null) {
				return false;
			} else if (!this.amplifier.matches(mobEffectInstance.getAmplifier())) {
				return false;
			} else if (!this.duration.matches(mobEffectInstance.getDuration())) {
				return false;
			} else {
				return this.ambient.isPresent() && this.ambient.get() != mobEffectInstance.isAmbient()
					? false
					: !this.visible.isPresent() || (Boolean)this.visible.get() == mobEffectInstance.isVisible();
			}
		}
	}
}
