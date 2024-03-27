package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;

public class BiasedToBottomInt extends IntProvider {
	public static final MapCodec<BiasedToBottomInt> CODEC = RecordCodecBuilder.<BiasedToBottomInt>mapCodec(
			instance -> instance.group(
						Codec.INT.fieldOf("min_inclusive").forGetter(biasedToBottomInt -> biasedToBottomInt.minInclusive),
						Codec.INT.fieldOf("max_inclusive").forGetter(biasedToBottomInt -> biasedToBottomInt.maxInclusive)
					)
					.apply(instance, BiasedToBottomInt::new)
		)
		.validate(
			biasedToBottomInt -> biasedToBottomInt.maxInclusive < biasedToBottomInt.minInclusive
					? DataResult.error(
						() -> "Max must be at least min, min_inclusive: " + biasedToBottomInt.minInclusive + ", max_inclusive: " + biasedToBottomInt.maxInclusive
					)
					: DataResult.success(biasedToBottomInt)
		);
	private final int minInclusive;
	private final int maxInclusive;

	private BiasedToBottomInt(int i, int j) {
		this.minInclusive = i;
		this.maxInclusive = j;
	}

	public static BiasedToBottomInt of(int i, int j) {
		return new BiasedToBottomInt(i, j);
	}

	@Override
	public int sample(RandomSource randomSource) {
		return this.minInclusive + randomSource.nextInt(randomSource.nextInt(this.maxInclusive - this.minInclusive + 1) + 1);
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
		return IntProviderType.BIASED_TO_BOTTOM;
	}

	public String toString() {
		return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
	}
}
