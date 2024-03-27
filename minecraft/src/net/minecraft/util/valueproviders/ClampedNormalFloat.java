package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ClampedNormalFloat extends FloatProvider {
	public static final MapCodec<ClampedNormalFloat> CODEC = RecordCodecBuilder.<ClampedNormalFloat>mapCodec(
			instance -> instance.group(
						Codec.FLOAT.fieldOf("mean").forGetter(clampedNormalFloat -> clampedNormalFloat.mean),
						Codec.FLOAT.fieldOf("deviation").forGetter(clampedNormalFloat -> clampedNormalFloat.deviation),
						Codec.FLOAT.fieldOf("min").forGetter(clampedNormalFloat -> clampedNormalFloat.min),
						Codec.FLOAT.fieldOf("max").forGetter(clampedNormalFloat -> clampedNormalFloat.max)
					)
					.apply(instance, ClampedNormalFloat::new)
		)
		.validate(
			clampedNormalFloat -> clampedNormalFloat.max < clampedNormalFloat.min
					? DataResult.error(() -> "Max must be larger than min: [" + clampedNormalFloat.min + ", " + clampedNormalFloat.max + "]")
					: DataResult.success(clampedNormalFloat)
		);
	private final float mean;
	private final float deviation;
	private final float min;
	private final float max;

	public static ClampedNormalFloat of(float f, float g, float h, float i) {
		return new ClampedNormalFloat(f, g, h, i);
	}

	private ClampedNormalFloat(float f, float g, float h, float i) {
		this.mean = f;
		this.deviation = g;
		this.min = h;
		this.max = i;
	}

	@Override
	public float sample(RandomSource randomSource) {
		return sample(randomSource, this.mean, this.deviation, this.min, this.max);
	}

	public static float sample(RandomSource randomSource, float f, float g, float h, float i) {
		return Mth.clamp(Mth.normal(randomSource, f, g), h, i);
	}

	@Override
	public float getMinValue() {
		return this.min;
	}

	@Override
	public float getMaxValue() {
		return this.max;
	}

	@Override
	public FloatProviderType<?> getType() {
		return FloatProviderType.CLAMPED_NORMAL;
	}

	public String toString() {
		return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min + "-" + this.max + "]";
	}
}
