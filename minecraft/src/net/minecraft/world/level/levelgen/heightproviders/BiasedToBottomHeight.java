package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiasedToBottomHeight extends HeightProvider {
	public static final Codec<BiasedToBottomHeight> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(biasedToBottomHeight -> biasedToBottomHeight.minInclusive),
						VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(biasedToBottomHeight -> biasedToBottomHeight.maxInclusive),
						Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("inner", 1).forGetter(biasedToBottomHeight -> biasedToBottomHeight.inner)
					)
					.apply(instance, BiasedToBottomHeight::new)
		)
		.comapFlatMap(DataResult::success, Function.identity());
	private static final Logger LOGGER = LogManager.getLogger();
	private final VerticalAnchor minInclusive;
	private final VerticalAnchor maxInclusive;
	private final int inner;

	private BiasedToBottomHeight(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2, int i) {
		this.minInclusive = verticalAnchor;
		this.maxInclusive = verticalAnchor2;
		this.inner = i;
	}

	public static BiasedToBottomHeight of(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2, int i) {
		return new BiasedToBottomHeight(verticalAnchor, verticalAnchor2, i);
	}

	@Override
	public int sample(Random random, WorldGenerationContext worldGenerationContext) {
		int i = this.minInclusive.resolveY(worldGenerationContext);
		int j = this.maxInclusive.resolveY(worldGenerationContext);
		if (j - i - this.inner + 1 <= 0) {
			LOGGER.warn("Empty height range: {}", this);
			return i;
		} else {
			int k = random.nextInt(j - i - this.inner + 1);
			return random.nextInt(k + this.inner) + i;
		}
	}

	@Override
	public HeightProviderType<?> getType() {
		return HeightProviderType.BIASED_TO_BOTTOM;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			BiasedToBottomHeight biasedToBottomHeight = (BiasedToBottomHeight)object;
			return this.minInclusive.equals(biasedToBottomHeight.minInclusive)
				&& this.maxInclusive.equals(this.maxInclusive)
				&& this.inner == biasedToBottomHeight.inner;
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.minInclusive, this.maxInclusive});
	}

	public String toString() {
		return "biased[" + this.minInclusive + '-' + this.maxInclusive + " inner: " + this.inner + "]";
	}
}
