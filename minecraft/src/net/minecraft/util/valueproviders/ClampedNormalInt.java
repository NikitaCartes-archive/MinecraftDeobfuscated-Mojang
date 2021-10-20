package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.Mth;

public class ClampedNormalInt extends IntProvider {
	public static final Codec<ClampedNormalInt> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.FLOAT.fieldOf("mean").forGetter(clampedNormalInt -> clampedNormalInt.mean),
						Codec.FLOAT.fieldOf("deviation").forGetter(clampedNormalInt -> clampedNormalInt.deviation),
						Codec.INT.fieldOf("min_inclusive").forGetter(clampedNormalInt -> clampedNormalInt.min_inclusive),
						Codec.INT.fieldOf("max_inclusive").forGetter(clampedNormalInt -> clampedNormalInt.max_inclusive)
					)
					.apply(instance, ClampedNormalInt::new)
		)
		.comapFlatMap(
			clampedNormalInt -> clampedNormalInt.max_inclusive < clampedNormalInt.min_inclusive
					? DataResult.error("Max must be larger than min: [" + clampedNormalInt.min_inclusive + ", " + clampedNormalInt.max_inclusive + "]")
					: DataResult.success(clampedNormalInt),
			Function.identity()
		);
	private float mean;
	private float deviation;
	private int min_inclusive;
	private int max_inclusive;

	public static ClampedNormalInt of(float f, float g, int i, int j) {
		return new ClampedNormalInt(f, g, i, j);
	}

	private ClampedNormalInt(float f, float g, int i, int j) {
		this.mean = f;
		this.deviation = g;
		this.min_inclusive = i;
		this.max_inclusive = j;
	}

	@Override
	public int sample(Random random) {
		return sample(random, this.mean, this.deviation, (float)this.min_inclusive, (float)this.max_inclusive);
	}

	public static int sample(Random random, float f, float g, float h, float i) {
		return (int)Mth.clamp(Mth.normal(random, f, g), h, i);
	}

	@Override
	public int getMinValue() {
		return this.min_inclusive;
	}

	@Override
	public int getMaxValue() {
		return this.max_inclusive;
	}

	@Override
	public IntProviderType<?> getType() {
		return IntProviderType.CLAMPED_NORMAL;
	}

	public String toString() {
		return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min_inclusive + "-" + this.max_inclusive + "]";
	}
}
