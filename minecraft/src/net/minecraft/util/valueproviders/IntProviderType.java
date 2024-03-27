package net.minecraft.util.valueproviders;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface IntProviderType<P extends IntProvider> {
	IntProviderType<ConstantInt> CONSTANT = register("constant", ConstantInt.CODEC);
	IntProviderType<UniformInt> UNIFORM = register("uniform", UniformInt.CODEC);
	IntProviderType<BiasedToBottomInt> BIASED_TO_BOTTOM = register("biased_to_bottom", BiasedToBottomInt.CODEC);
	IntProviderType<ClampedInt> CLAMPED = register("clamped", ClampedInt.CODEC);
	IntProviderType<WeightedListInt> WEIGHTED_LIST = register("weighted_list", WeightedListInt.CODEC);
	IntProviderType<ClampedNormalInt> CLAMPED_NORMAL = register("clamped_normal", ClampedNormalInt.CODEC);

	MapCodec<P> codec();

	static <P extends IntProvider> IntProviderType<P> register(String string, MapCodec<P> mapCodec) {
		return Registry.register(BuiltInRegistries.INT_PROVIDER_TYPE, string, () -> mapCodec);
	}
}
