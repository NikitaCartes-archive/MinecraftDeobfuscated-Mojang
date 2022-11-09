package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.registries.BuiltInRegistries;

public abstract class FeatureSize {
	public static final Codec<FeatureSize> CODEC = BuiltInRegistries.FEATURE_SIZE_TYPE.byNameCodec().dispatch(FeatureSize::type, FeatureSizeType::codec);
	protected static final int MAX_WIDTH = 16;
	protected final OptionalInt minClippedHeight;

	protected static <S extends FeatureSize> RecordCodecBuilder<S, OptionalInt> minClippedHeightCodec() {
		return Codec.intRange(0, 80)
			.optionalFieldOf("min_clipped_height")
			.<OptionalInt>xmap(
				optional -> (OptionalInt)optional.map(OptionalInt::of).orElse(OptionalInt.empty()),
				optionalInt -> optionalInt.isPresent() ? Optional.of(optionalInt.getAsInt()) : Optional.empty()
			)
			.forGetter(featureSize -> featureSize.minClippedHeight);
	}

	public FeatureSize(OptionalInt optionalInt) {
		this.minClippedHeight = optionalInt;
	}

	protected abstract FeatureSizeType<?> type();

	public abstract int getSizeAtHeight(int i, int j);

	public OptionalInt minClippedHeight() {
		return this.minClippedHeight;
	}
}
