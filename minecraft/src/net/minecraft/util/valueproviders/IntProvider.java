package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.Registry;

public abstract class IntProvider {
	private static final Codec<Either<Integer, IntProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(
		Codec.INT, Registry.INT_PROVIDER_TYPES.dispatch(IntProvider::getType, IntProviderType::codec)
	);
	public static final Codec<IntProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(
		either -> either.map(ConstantInt::of, intProvider -> intProvider),
		intProvider -> intProvider.getType() == IntProviderType.CONSTANT ? Either.left(((ConstantInt)intProvider).getValue()) : Either.right(intProvider)
	);
	public static final Codec<IntProvider> NON_NEGATIVE_CODEC = codec(0, Integer.MAX_VALUE);
	public static final Codec<IntProvider> POSITIVE_CODEC = codec(1, Integer.MAX_VALUE);

	public static Codec<IntProvider> codec(int i, int j) {
		Function<IntProvider, DataResult<IntProvider>> function = intProvider -> {
			if (intProvider.getMinValue() < i) {
				return DataResult.error("Value provider too low: " + i + " [" + intProvider.getMinValue() + "-" + intProvider.getMaxValue() + "]");
			} else {
				return intProvider.getMaxValue() > j
					? DataResult.error("Value provider too high: " + j + " [" + intProvider.getMinValue() + "-" + intProvider.getMaxValue() + "]")
					: DataResult.success(intProvider);
			}
		};
		return CODEC.flatXmap(function, function);
	}

	public abstract int sample(Random random);

	public abstract int getMinValue();

	public abstract int getMaxValue();

	public abstract IntProviderType<?> getType();
}
