/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviderType;

public class UniformFloat
extends FloatProvider {
    public static final Codec<UniformFloat> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("min_inclusive")).forGetter(uniformFloat -> Float.valueOf(uniformFloat.minInclusive)), ((MapCodec)Codec.FLOAT.fieldOf("max_exclusive")).forGetter(uniformFloat -> Float.valueOf(uniformFloat.maxExclusive))).apply((Applicative<UniformFloat, ?>)instance, UniformFloat::new)).comapFlatMap(uniformFloat -> {
        if (uniformFloat.maxExclusive <= uniformFloat.minInclusive) {
            return DataResult.error(() -> "Max must be larger than min, min_inclusive: " + uniformFloat.minInclusive + ", max_exclusive: " + uniformFloat.maxExclusive);
        }
        return DataResult.success(uniformFloat);
    }, Function.identity());
    private final float minInclusive;
    private final float maxExclusive;

    private UniformFloat(float f, float g) {
        this.minInclusive = f;
        this.maxExclusive = g;
    }

    public static UniformFloat of(float f, float g) {
        if (g <= f) {
            throw new IllegalArgumentException("Max must exceed min");
        }
        return new UniformFloat(f, g);
    }

    @Override
    public float sample(RandomSource randomSource) {
        return Mth.randomBetween(randomSource, this.minInclusive, this.maxExclusive);
    }

    @Override
    public float getMinValue() {
        return this.minInclusive;
    }

    @Override
    public float getMaxValue() {
        return this.maxExclusive;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.UNIFORM;
    }

    public String toString() {
        return "[" + this.minInclusive + "-" + this.maxExclusive + "]";
    }
}

