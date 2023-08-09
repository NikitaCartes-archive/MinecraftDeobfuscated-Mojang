package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;

public record LightPredicate(MinMaxBounds.Ints composite) {
	public static final Codec<LightPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "light", MinMaxBounds.Ints.ANY).forGetter(LightPredicate::composite))
				.apply(instance, LightPredicate::new)
	);

	static Optional<LightPredicate> of(MinMaxBounds.Ints ints) {
		return ints.isAny() ? Optional.empty() : Optional.of(new LightPredicate(ints));
	}

	public boolean matches(ServerLevel serverLevel, BlockPos blockPos) {
		return !serverLevel.isLoaded(blockPos) ? false : this.composite.matches(serverLevel.getMaxLocalRawBrightness(blockPos));
	}

	public static class Builder {
		private MinMaxBounds.Ints composite = MinMaxBounds.Ints.ANY;

		public static LightPredicate.Builder light() {
			return new LightPredicate.Builder();
		}

		public LightPredicate.Builder setComposite(MinMaxBounds.Ints ints) {
			this.composite = ints;
			return this;
		}

		public Optional<LightPredicate> build() {
			return LightPredicate.of(this.composite);
		}
	}
}
