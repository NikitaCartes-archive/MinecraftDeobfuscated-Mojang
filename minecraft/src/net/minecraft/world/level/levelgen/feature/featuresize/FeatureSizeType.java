package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class FeatureSizeType<P extends FeatureSize> {
	public static final FeatureSizeType<TwoLayersFeatureSize> TWO_LAYERS_FEATURE_SIZE = register("two_layers_feature_size", TwoLayersFeatureSize.CODEC);
	public static final FeatureSizeType<ThreeLayersFeatureSize> THREE_LAYERS_FEATURE_SIZE = register("three_layers_feature_size", ThreeLayersFeatureSize.CODEC);
	private final MapCodec<P> codec;

	private static <P extends FeatureSize> FeatureSizeType<P> register(String string, MapCodec<P> mapCodec) {
		return Registry.register(BuiltInRegistries.FEATURE_SIZE_TYPE, string, new FeatureSizeType<>(mapCodec));
	}

	private FeatureSizeType(MapCodec<P> mapCodec) {
		this.codec = mapCodec;
	}

	public MapCodec<P> codec() {
		return this.codec;
	}
}
