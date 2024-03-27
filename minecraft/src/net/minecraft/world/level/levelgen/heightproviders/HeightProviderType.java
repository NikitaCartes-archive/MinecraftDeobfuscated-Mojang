package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface HeightProviderType<P extends HeightProvider> {
	HeightProviderType<ConstantHeight> CONSTANT = register("constant", ConstantHeight.CODEC);
	HeightProviderType<UniformHeight> UNIFORM = register("uniform", UniformHeight.CODEC);
	HeightProviderType<BiasedToBottomHeight> BIASED_TO_BOTTOM = register("biased_to_bottom", BiasedToBottomHeight.CODEC);
	HeightProviderType<VeryBiasedToBottomHeight> VERY_BIASED_TO_BOTTOM = register("very_biased_to_bottom", VeryBiasedToBottomHeight.CODEC);
	HeightProviderType<TrapezoidHeight> TRAPEZOID = register("trapezoid", TrapezoidHeight.CODEC);
	HeightProviderType<WeightedListHeight> WEIGHTED_LIST = register("weighted_list", WeightedListHeight.CODEC);

	MapCodec<P> codec();

	private static <P extends HeightProvider> HeightProviderType<P> register(String string, MapCodec<P> mapCodec) {
		return Registry.register(BuiltInRegistries.HEIGHT_PROVIDER_TYPE, string, () -> mapCodec);
	}
}
