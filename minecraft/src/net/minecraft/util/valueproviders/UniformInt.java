package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.Mth;

public class UniformInt extends IntProvider {
	public static final Codec<UniformInt> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.INT.fieldOf("min_inclusive").forGetter(uniformInt -> uniformInt.minInclusive),
						Codec.INT.fieldOf("max_inclusive").forGetter(uniformInt -> uniformInt.maxInclusive)
					)
					.apply(instance, UniformInt::new)
		)
		.comapFlatMap(
			uniformInt -> uniformInt.maxInclusive < uniformInt.minInclusive
					? DataResult.error("Max must be at least min, min_inclusive: " + uniformInt.minInclusive + ", max_inclusive: " + uniformInt.maxInclusive)
					: DataResult.success(uniformInt),
			Function.identity()
		);
	private final int minInclusive;
	private final int maxInclusive;

	private UniformInt(int i, int j) {
		this.minInclusive = i;
		this.maxInclusive = j;
	}

	public static UniformInt of(int i, int j) {
		return new UniformInt(i, j);
	}

	@Override
	public int sample(Random random) {
		return Mth.randomBetweenInclusive(random, this.minInclusive, this.maxInclusive);
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
		return IntProviderType.UNIFORM;
	}

	public String toString() {
		return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
	}
}
