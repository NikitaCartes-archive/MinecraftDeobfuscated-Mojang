package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.Mth;

public class ClampedInt extends IntProvider {
	public static final Codec<ClampedInt> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						IntProvider.CODEC.fieldOf("source").forGetter(clampedInt -> clampedInt.source),
						Codec.INT.fieldOf("min_inclusive").forGetter(clampedInt -> clampedInt.minInclusive),
						Codec.INT.fieldOf("max_inclusive").forGetter(clampedInt -> clampedInt.maxInclusive)
					)
					.apply(instance, ClampedInt::new)
		)
		.comapFlatMap(
			clampedInt -> clampedInt.maxInclusive < clampedInt.minInclusive
					? DataResult.error("Max must be at least min, min_inclusive: " + clampedInt.minInclusive + ", max_inclusive: " + clampedInt.maxInclusive)
					: DataResult.success(clampedInt),
			Function.identity()
		);
	private final IntProvider source;
	private int minInclusive;
	private int maxInclusive;

	public static ClampedInt of(IntProvider intProvider, int i, int j) {
		return new ClampedInt(intProvider, i, j);
	}

	public ClampedInt(IntProvider intProvider, int i, int j) {
		this.source = intProvider;
		this.minInclusive = i;
		this.maxInclusive = j;
	}

	@Override
	public int sample(Random random) {
		return Mth.clamp(this.source.sample(random), this.minInclusive, this.maxInclusive);
	}

	@Override
	public int getMinValue() {
		return Math.max(this.minInclusive, this.source.getMinValue());
	}

	@Override
	public int getMaxValue() {
		return Math.min(this.maxInclusive, this.source.getMaxValue());
	}

	@Override
	public IntProviderType<?> getType() {
		return IntProviderType.CLAMPED;
	}
}
