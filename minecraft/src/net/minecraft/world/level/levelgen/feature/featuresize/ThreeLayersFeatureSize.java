package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;

public class ThreeLayersFeatureSize extends FeatureSize {
	public static final Codec<ThreeLayersFeatureSize> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.intRange(0, 80).fieldOf("limit").orElse(1).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.limit),
					Codec.intRange(0, 80).fieldOf("upper_limit").orElse(1).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.upperLimit),
					Codec.intRange(0, 16).fieldOf("lower_size").orElse(0).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.lowerSize),
					Codec.intRange(0, 16).fieldOf("middle_size").orElse(1).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.middleSize),
					Codec.intRange(0, 16).fieldOf("upper_size").orElse(1).forGetter(threeLayersFeatureSize -> threeLayersFeatureSize.upperSize),
					minClippedHeightCodec()
				)
				.apply(instance, ThreeLayersFeatureSize::new)
	);
	private final int limit;
	private final int upperLimit;
	private final int lowerSize;
	private final int middleSize;
	private final int upperSize;

	public ThreeLayersFeatureSize(int i, int j, int k, int l, int m, OptionalInt optionalInt) {
		super(optionalInt);
		this.limit = i;
		this.upperLimit = j;
		this.lowerSize = k;
		this.middleSize = l;
		this.upperSize = m;
	}

	@Override
	protected FeatureSizeType<?> type() {
		return FeatureSizeType.THREE_LAYERS_FEATURE_SIZE;
	}

	@Override
	public int getSizeAtHeight(int i, int j) {
		if (j < this.limit) {
			return this.lowerSize;
		} else {
			return j >= i - this.upperLimit ? this.upperSize : this.middleSize;
		}
	}
}
