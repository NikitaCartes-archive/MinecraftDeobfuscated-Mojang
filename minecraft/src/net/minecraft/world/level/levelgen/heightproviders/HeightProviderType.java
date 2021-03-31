package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public interface HeightProviderType<P extends HeightProvider> {
	HeightProviderType<ConstantHeight> CONSTANT = register("constant", ConstantHeight.CODEC);
	HeightProviderType<UniformHeight> UNIFORM = register("uniform", UniformHeight.CODEC);
	HeightProviderType<BiasedToBottomHeight> BIASED_TO_BOTTOM = register("biased_to_bottom", BiasedToBottomHeight.CODEC);

	Codec<P> codec();

	static <P extends HeightProvider> HeightProviderType<P> register(String string, Codec<P> codec) {
		return Registry.register(Registry.HEIGHT_PROVIDER_TYPES, string, () -> codec);
	}
}
