package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class FeatureSizeType<P extends FeatureSize> {
	public static final FeatureSizeType<TwoLayersFeatureSize> TWO_LAYERS_FEATURE_SIZE = register("two_layers_feature_size", TwoLayersFeatureSize::new);
	public static final FeatureSizeType<ThreeLayersFeatureSize> THREE_LAYERS_FEATURE_SIZE = register("three_layers_feature_size", ThreeLayersFeatureSize::new);
	private final Function<Dynamic<?>, P> deserializer;

	private static <P extends FeatureSize> FeatureSizeType<P> register(String string, Function<Dynamic<?>, P> function) {
		return Registry.register(Registry.FEATURE_SIZE_TYPES, string, new FeatureSizeType<>(function));
	}

	private FeatureSizeType(Function<Dynamic<?>, P> function) {
		this.deserializer = function;
	}

	public P deserialize(Dynamic<?> dynamic) {
		return (P)this.deserializer.apply(dynamic);
	}
}
