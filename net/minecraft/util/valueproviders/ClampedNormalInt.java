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

public class ClampedNormalInt
extends IntProvider {
    public static final Codec<ClampedNormalInt> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("mean")).forGetter(clampedNormalInt -> Float.valueOf(clampedNormalInt.mean)), ((MapCodec)Codec.FLOAT.fieldOf("deviation")).forGetter(clampedNormalInt -> Float.valueOf(clampedNormalInt.deviation)), ((MapCodec)Codec.INT.fieldOf("min_inclusive")).forGetter(clampedNormalInt -> clampedNormalInt.min_inclusive), ((MapCodec)Codec.INT.fieldOf("max_inclusive")).forGetter(clampedNormalInt -> clampedNormalInt.max_inclusive)).apply((Applicative<ClampedNormalInt, ?>)instance, ClampedNormalInt::new)).comapFlatMap(clampedNormalInt -> {
        if (clampedNormalInt.max_inclusive < clampedNormalInt.min_inclusive) {
            return DataResult.error("Max must be larger than min: [" + clampedNormalInt.min_inclusive + ", " + clampedNormalInt.max_inclusive + "]");
        }
        return DataResult.success(clampedNormalInt);
    }, Function.identity());
    private final float mean;
    private final float deviation;
    private final int min_inclusive;
    private final int max_inclusive;

    public static ClampedNormalInt of(float f, float g, int i, int j) {
        return new ClampedNormalInt(f, g, i, j);
    }

    private ClampedNormalInt(float f, float g, int i, int j) {
        this.mean = f;
        this.deviation = g;
        this.min_inclusive = i;
        this.max_inclusive = j;
    }

    @Override
    public int sample(RandomSource randomSource) {
        return ClampedNormalInt.sample(randomSource, this.mean, this.deviation, this.min_inclusive, this.max_inclusive);
    }

    public static int sample(RandomSource randomSource, float f, float g, float h, float i) {
        return (int)Mth.clamp(Mth.normal(randomSource, f, g), h, i);
    }

    @Override
    public int getMinValue() {
        return this.min_inclusive;
    }

    @Override
    public int getMaxValue() {
        return this.max_inclusive;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.CLAMPED_NORMAL;
    }

    public String toString() {
        return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min_inclusive + "-" + this.max_inclusive + "]";
    }
}

