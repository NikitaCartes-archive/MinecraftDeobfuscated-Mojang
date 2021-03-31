package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.Registry;

public abstract class FloatProvider {
	private static final Codec<Either<Float, FloatProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(
		Codec.FLOAT, Registry.FLOAT_PROVIDER_TYPES.dispatch(FloatProvider::getType, FloatProviderType::codec)
	);
	public static final Codec<FloatProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(
		either -> either.map(ConstantFloat::of, floatProvider -> floatProvider),
		floatProvider -> floatProvider.getType() == FloatProviderType.CONSTANT ? Either.left(((ConstantFloat)floatProvider).getValue()) : Either.right(floatProvider)
	);

	public static Codec<FloatProvider> codec(float f, float g) {
		Function<FloatProvider, DataResult<FloatProvider>> function = floatProvider -> {
			if (floatProvider.getMinValue() < f) {
				return DataResult.error("Value provider too low: " + f + " [" + floatProvider.getMinValue() + "-" + floatProvider.getMaxValue() + "]");
			} else {
				return floatProvider.getMaxValue() > g
					? DataResult.error("Value provider too high: " + g + " [" + floatProvider.getMinValue() + "-" + floatProvider.getMaxValue() + "]")
					: DataResult.success(floatProvider);
			}
		};
		return CODEC.flatXmap(function, function);
	}

	public abstract float sample(Random random);

	public abstract float getMinValue();

	public abstract float getMaxValue();

	public abstract FloatProviderType<?> getType();
}
