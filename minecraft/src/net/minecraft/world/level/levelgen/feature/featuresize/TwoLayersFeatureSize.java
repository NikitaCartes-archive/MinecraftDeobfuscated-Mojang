package net.minecraft.world.level.levelgen.feature.featuresize;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.OptionalInt;

public class TwoLayersFeatureSize extends FeatureSize {
	private final int limit;
	private final int lowerSize;
	private final int upperSize;

	public TwoLayersFeatureSize(int i, int j, int k) {
		this(i, j, k, OptionalInt.empty());
	}

	public TwoLayersFeatureSize(int i, int j, int k, OptionalInt optionalInt) {
		super(FeatureSizeType.TWO_LAYERS_FEATURE_SIZE, optionalInt);
		this.limit = i;
		this.lowerSize = j;
		this.upperSize = k;
	}

	public <T> TwoLayersFeatureSize(Dynamic<T> dynamic) {
		this(
			dynamic.get("limit").asInt(1),
			dynamic.get("lower_size").asInt(0),
			dynamic.get("upper_size").asInt(1),
			(OptionalInt)dynamic.get("min_clipped_height").asNumber().map(number -> OptionalInt.of(number.intValue())).orElse(OptionalInt.empty())
		);
	}

	@Override
	public int getSizeAtHeight(int i, int j) {
		return j < this.limit ? this.lowerSize : this.upperSize;
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("limit"), dynamicOps.createInt(this.limit))
			.put(dynamicOps.createString("lower_size"), dynamicOps.createInt(this.lowerSize))
			.put(dynamicOps.createString("upper_size"), dynamicOps.createInt(this.upperSize));
		return dynamicOps.merge(super.serialize(dynamicOps), dynamicOps.createMap(builder.build()));
	}
}
