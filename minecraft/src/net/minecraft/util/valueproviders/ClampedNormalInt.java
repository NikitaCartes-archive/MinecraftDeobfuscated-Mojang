package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ClampedNormalInt extends IntProvider {
	public static final MapCodec<ClampedNormalInt> CODEC = RecordCodecBuilder.<ClampedNormalInt>mapCodec(
			instance -> instance.group(
						Codec.FLOAT.fieldOf("mean").forGetter(clampedNormalInt -> clampedNormalInt.mean),
						Codec.FLOAT.fieldOf("deviation").forGetter(clampedNormalInt -> clampedNormalInt.deviation),
						Codec.INT.fieldOf("min_inclusive").forGetter(clampedNormalInt -> clampedNormalInt.minInclusive),
						Codec.INT.fieldOf("max_inclusive").forGetter(clampedNormalInt -> clampedNormalInt.maxInclusive)
					)
					.apply(instance, ClampedNormalInt::new)
		)
		.validate(
			clampedNormalInt -> clampedNormalInt.maxInclusive < clampedNormalInt.minInclusive
					? DataResult.error(() -> "Max must be larger than min: [" + clampedNormalInt.minInclusive + ", " + clampedNormalInt.maxInclusive + "]")
					: DataResult.success(clampedNormalInt)
		);
	private final float mean;
	private final float deviation;
	private final int minInclusive;
	private final int maxInclusive;

	public static ClampedNormalInt of(float f, float g, int i, int j) {
		return new ClampedNormalInt(f, g, i, j);
	}

	private ClampedNormalInt(float f, float g, int i, int j) {
		this.mean = f;
		this.deviation = g;
		this.minInclusive = i;
		this.maxInclusive = j;
	}

	@Override
	public int sample(RandomSource randomSource) {
		return sample(randomSource, this.mean, this.deviation, (float)this.minInclusive, (float)this.maxInclusive);
	}

	public static int sample(RandomSource randomSource, float f, float g, float h, float i) {
		return (int)Mth.clamp(Mth.normal(randomSource, f, g), h, i);
	}

	@Override
	public int getMinValue() {
		return this.minInclusive;
	}

	@Override
	public int getMaxValue() {
		return this.maxInclusive;
	}

	@Override
	public IntProviderType<?> getType() {
		return IntProviderType.CLAMPED_NORMAL;
	}

	public String toString() {
		return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.minInclusive + "-" + this.maxInclusive + "]";
	}
}
