/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProviderType;

public abstract class IntProvider {
    private static final Codec<Either<Integer, IntProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(Codec.INT, BuiltInRegistries.INT_PROVIDER_TYPE.byNameCodec().dispatch(IntProvider::getType, IntProviderType::codec));
    public static final Codec<IntProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(either -> either.map(ConstantInt::of, intProvider -> intProvider), intProvider -> intProvider.getType() == IntProviderType.CONSTANT ? Either.left(((ConstantInt)intProvider).getValue()) : Either.right(intProvider));
    public static final Codec<IntProvider> NON_NEGATIVE_CODEC = IntProvider.codec(0, Integer.MAX_VALUE);
    public static final Codec<IntProvider> POSITIVE_CODEC = IntProvider.codec(1, Integer.MAX_VALUE);

    public static Codec<IntProvider> codec(int i, int j) {
        return IntProvider.codec(i, j, CODEC);
    }

    public static <T extends IntProvider> Codec<T> codec(int i, int j, Codec<T> codec) {
        return ExtraCodecs.validate(codec, intProvider -> {
            if (intProvider.getMinValue() < i) {
                return DataResult.error(() -> "Value provider too low: " + i + " [" + intProvider.getMinValue() + "-" + intProvider.getMaxValue() + "]");
            }
            if (intProvider.getMaxValue() > j) {
                return DataResult.error(() -> "Value provider too high: " + j + " [" + intProvider.getMinValue() + "-" + intProvider.getMaxValue() + "]");
            }
            return DataResult.success(intProvider);
        });
    }

    public abstract int sample(RandomSource var1);

    public abstract int getMinValue();

    public abstract int getMaxValue();

    public abstract IntProviderType<?> getType();
}

