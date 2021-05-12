package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.Mth;

public class UniformFloat extends FloatProvider {
	public static final Codec<UniformFloat> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.FLOAT.fieldOf("min_inclusive").forGetter(uniformFloat -> uniformFloat.minInclusive),
						Codec.FLOAT.fieldOf("max_exclusive").forGetter(uniformFloat -> uniformFloat.maxExclusive)
					)
					.apply(instance, UniformFloat::new)
		)
		.comapFlatMap(
			uniformFloat -> uniformFloat.maxExclusive <= uniformFloat.minInclusive
					? DataResult.error("Max must be larger than min, min_inclusive: " + uniformFloat.minInclusive + ", max_exclusive: " + uniformFloat.maxExclusive)
					: DataResult.success(uniformFloat),
			Function.identity()
		);
	private final float minInclusive;
	private final float maxExclusive;

	private UniformFloat(float f, float g) {
		this.minInclusive = f;
		this.maxExclusive = g;
	}

	public static UniformFloat of(float f, float g) {
		if (g <= f) {
			throw new IllegalArgumentException("Max must exceed min");
		} else {
			return new UniformFloat(f, g);
		}
	}

	@Override
	public float sample(Random random) {
		return Mth.randomBetween(random, this.minInclusive, this.maxExclusive);
	}

	@Override
	public float getMinValue() {
		return this.minInclusive;
	}

	@Override
	public float getMaxValue() {
		return this.maxExclusive;
	}

	@Override
	public FloatProviderType<?> getType() {
		return FloatProviderType.UNIFORM;
	}

	public String toString() {
		return "[" + this.minInclusive + "-" + this.maxExclusive + "]";
	}
}
