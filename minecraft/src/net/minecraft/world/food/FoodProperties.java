package net.minecraft.world.food;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;

public record FoodProperties(int nutrition, float saturationModifier, boolean canAlwaysEat, float eatSeconds, List<FoodProperties.PossibleEffect> effects) {
	private static final float DEFAULT_EAT_SECONDS = 1.6F;
	public static final Codec<FoodProperties> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.NON_NEGATIVE_INT.fieldOf("nutrition").forGetter(FoodProperties::nutrition),
					Codec.FLOAT.fieldOf("saturation_modifier").forGetter(FoodProperties::saturationModifier),
					ExtraCodecs.strictOptionalField(Codec.BOOL, "can_always_eat", false).forGetter(FoodProperties::canAlwaysEat),
					ExtraCodecs.strictOptionalField(ExtraCodecs.POSITIVE_FLOAT, "eat_seconds", 1.6F).forGetter(FoodProperties::eatSeconds),
					ExtraCodecs.strictOptionalField(FoodProperties.PossibleEffect.CODEC.listOf(), "effects", List.of()).forGetter(FoodProperties::effects)
				)
				.apply(instance, FoodProperties::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, FoodProperties> DIRECT_STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		FoodProperties::nutrition,
		ByteBufCodecs.FLOAT,
		FoodProperties::saturationModifier,
		ByteBufCodecs.BOOL,
		FoodProperties::canAlwaysEat,
		ByteBufCodecs.FLOAT,
		FoodProperties::eatSeconds,
		FoodProperties.PossibleEffect.STREAM_CODEC.apply(ByteBufCodecs.list()),
		FoodProperties::effects,
		FoodProperties::new
	);

	public int eatDurationTicks() {
		return (int)(this.eatSeconds * 20.0F);
	}

	public static class Builder {
		private int nutrition;
		private float saturationModifier;
		private boolean canAlwaysEat;
		private float eatSeconds = 1.6F;
		private final ImmutableList.Builder<FoodProperties.PossibleEffect> effects = ImmutableList.builder();

		public FoodProperties.Builder nutrition(int i) {
			this.nutrition = i;
			return this;
		}

		public FoodProperties.Builder saturationModifier(float f) {
			this.saturationModifier = f;
			return this;
		}

		public FoodProperties.Builder alwaysEdible() {
			this.canAlwaysEat = true;
			return this;
		}

		public FoodProperties.Builder fast() {
			this.eatSeconds = 0.8F;
			return this;
		}

		public FoodProperties.Builder effect(MobEffectInstance mobEffectInstance, float f) {
			this.effects.add(new FoodProperties.PossibleEffect(mobEffectInstance, f));
			return this;
		}

		public FoodProperties build() {
			return new FoodProperties(this.nutrition, this.saturationModifier, this.canAlwaysEat, this.eatSeconds, this.effects.build());
		}
	}

	public static record PossibleEffect(MobEffectInstance effect, float probability) {
		public static final Codec<FoodProperties.PossibleEffect> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						MobEffectInstance.CODEC.fieldOf("effect").forGetter(FoodProperties.PossibleEffect::effect),
						ExtraCodecs.strictOptionalField(Codec.floatRange(0.0F, 1.0F), "probability", 1.0F).forGetter(FoodProperties.PossibleEffect::probability)
					)
					.apply(instance, FoodProperties.PossibleEffect::new)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, FoodProperties.PossibleEffect> STREAM_CODEC = StreamCodec.composite(
			MobEffectInstance.STREAM_CODEC,
			FoodProperties.PossibleEffect::effect,
			ByteBufCodecs.FLOAT,
			FoodProperties.PossibleEffect::probability,
			FoodProperties.PossibleEffect::new
		);

		public MobEffectInstance effect() {
			return new MobEffectInstance(this.effect);
		}
	}
}
