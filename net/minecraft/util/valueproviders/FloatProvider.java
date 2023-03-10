/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProviderType;
import net.minecraft.util.valueproviders.SampledFloat;

public abstract class FloatProvider
implements SampledFloat {
    private static final Codec<Either<Float, FloatProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(Codec.FLOAT, BuiltInRegistries.FLOAT_PROVIDER_TYPE.byNameCodec().dispatch(FloatProvider::getType, FloatProviderType::codec));
    public static final Codec<FloatProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(either -> either.map(ConstantFloat::of, floatProvider -> floatProvider), floatProvider -> floatProvider.getType() == FloatProviderType.CONSTANT ? Either.left(Float.valueOf(((ConstantFloat)floatProvider).getValue())) : Either.right(floatProvider));

    public static Codec<FloatProvider> codec(float f, float g) {
        return ExtraCodecs.validate(CODEC, floatProvider -> {
            if (floatProvider.getMinValue() < f) {
                return DataResult.error(() -> "Value provider too low: " + f + " [" + floatProvider.getMinValue() + "-" + floatProvider.getMaxValue() + "]");
            }
            if (floatProvider.getMaxValue() > g) {
                return DataResult.error(() -> "Value provider too high: " + g + " [" + floatProvider.getMinValue() + "-" + floatProvider.getMaxValue() + "]");
            }
            return DataResult.success(floatProvider);
        });
    }

    public abstract float getMinValue();

    public abstract float getMaxValue();

    public abstract FloatProviderType<?> getType();
}

