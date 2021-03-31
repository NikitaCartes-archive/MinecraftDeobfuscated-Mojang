package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public interface IntProviderType<P extends IntProvider> {
	IntProviderType<ConstantInt> CONSTANT = register("constant", ConstantInt.CODEC);
	IntProviderType<UniformInt> UNIFORM = register("uniform", UniformInt.CODEC);

	Codec<P> codec();

	static <P extends IntProvider> IntProviderType<P> register(String string, Codec<P> codec) {
		return Registry.register(Registry.INT_PROVIDER_TYPES, string, () -> codec);
	}
}
