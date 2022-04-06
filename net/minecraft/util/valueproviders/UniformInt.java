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
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class UniformInt
extends IntProvider {
    public static final Codec<UniformInt> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("min_inclusive")).forGetter(uniformInt -> uniformInt.minInclusive), ((MapCodec)Codec.INT.fieldOf("max_inclusive")).forGetter(uniformInt -> uniformInt.maxInclusive)).apply((Applicative<UniformInt, ?>)instance, UniformInt::new)).comapFlatMap(uniformInt -> {
        if (uniformInt.maxInclusive < uniformInt.minInclusive) {
            return DataResult.error("Max must be at least min, min_inclusive: " + uniformInt.minInclusive + ", max_inclusive: " + uniformInt.maxInclusive);
        }
        return DataResult.success(uniformInt);
    }, Function.identity());
    private final int minInclusive;
    private final int maxInclusive;

    private UniformInt(int i, int j) {
        this.minInclusive = i;
        this.maxInclusive = j;
    }

    public static UniformInt of(int i, int j) {
        return new UniformInt(i, j);
    }

    @Override
    public int sample(RandomSource randomSource) {
        return Mth.randomBetweenInclusive(randomSource, this.minInclusive, this.maxInclusive);
    }

    @Override
    public int getMinValue() {
        return this.minInclusive;
    }

    @Override
    public int getMaxValue() {
        return this.maxInclusive;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.UNIFORM;
    }

    public String toString() {
        return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}

