package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VeryBiasedToBottomHeight extends HeightProvider {
	public static final Codec<VeryBiasedToBottomHeight> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter(veryBiasedToBottomHeight -> veryBiasedToBottomHeight.minInclusive),
					VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter(veryBiasedToBottomHeight -> veryBiasedToBottomHeight.maxInclusive),
					Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("inner", 1).forGetter(veryBiasedToBottomHeight -> veryBiasedToBottomHeight.inner)
				)
				.apply(instance, VeryBiasedToBottomHeight::new)
	);
	private static final Logger LOGGER = LogManager.getLogger();
	private final VerticalAnchor minInclusive;
	private final VerticalAnchor maxInclusive;
	private final int inner;

	private VeryBiasedToBottomHeight(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2, int i) {
		this.minInclusive = verticalAnchor;
		this.maxInclusive = verticalAnchor2;
		this.inner = i;
	}

	public static VeryBiasedToBottomHeight of(VerticalAnchor verticalAnchor, VerticalAnchor verticalAnchor2, int i) {
		return new VeryBiasedToBottomHeight(verticalAnchor, verticalAnchor2, i);
	}

	@Override
	public int sample(Random random, WorldGenerationContext worldGenerationContext) {
		int i = this.minInclusive.resolveY(worldGenerationContext);
		int j = this.maxInclusive.resolveY(worldGenerationContext);
		if (j - i - this.inner + 1 <= 0) {
			LOGGER.warn("Empty height range: {}", this);
			return i;
		} else {
			int k = Mth.nextInt(random, i + this.inner, j);
			int l = Mth.nextInt(random, i, k - 1);
			return Mth.nextInt(random, i, l - 1 + this.inner);
		}
	}

	@Override
	public HeightProviderType<?> getType() {
		return HeightProviderType.VERY_BIASED_TO_BOTTOM;
	}

	public String toString() {
		return "biased[" + this.minInclusive + "-" + this.maxInclusive + " inner: " + this.inner + "]";
	}
}
