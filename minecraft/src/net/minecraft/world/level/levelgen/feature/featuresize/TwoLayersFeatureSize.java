package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;

public class TwoLayersFeatureSize extends FeatureSize {
	public static final Codec<TwoLayersFeatureSize> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("limit").withDefault(1).forGetter(twoLayersFeatureSize -> twoLayersFeatureSize.limit),
					Codec.INT.fieldOf("lower_size").withDefault(0).forGetter(twoLayersFeatureSize -> twoLayersFeatureSize.lowerSize),
					Codec.INT.fieldOf("upper_size").withDefault(1).forGetter(twoLayersFeatureSize -> twoLayersFeatureSize.upperSize),
					minClippedHeightCodec()
				)
				.apply(instance, TwoLayersFeatureSize::new)
	);
	private final int limit;
	private final int lowerSize;
	private final int upperSize;

	public TwoLayersFeatureSize(int i, int j, int k) {
		this(i, j, k, OptionalInt.empty());
	}

	public TwoLayersFeatureSize(int i, int j, int k, OptionalInt optionalInt) {
		super(optionalInt);
		this.limit = i;
		this.lowerSize = j;
		this.upperSize = k;
	}

	@Override
	protected FeatureSizeType<?> type() {
		return FeatureSizeType.TWO_LAYERS_FEATURE_SIZE;
	}

	@Override
	public int getSizeAtHeight(int i, int j) {
		return j < this.limit ? this.lowerSize : this.upperSize;
	}
}
