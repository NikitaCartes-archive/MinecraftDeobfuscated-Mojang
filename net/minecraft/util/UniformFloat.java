/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.FloatProvider;
import net.minecraft.util.FloatProviderType;
import net.minecraft.util.Mth;

public class UniformFloat
extends FloatProvider {
    public static final Codec<UniformFloat> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("base")).forGetter(uniformFloat -> Float.valueOf(uniformFloat.baseValue)), ((MapCodec)Codec.FLOAT.fieldOf("spread")).forGetter(uniformFloat -> Float.valueOf(uniformFloat.spread))).apply((Applicative<UniformFloat, ?>)instance, UniformFloat::new)).comapFlatMap(uniformFloat -> {
        if (uniformFloat.spread < 0.0f) {
            return DataResult.error("Spread must be non-negative, got: " + uniformFloat.spread);
        }
        return DataResult.success(uniformFloat);
    }, Function.identity());
    private final float baseValue;
    private final float spread;

    private UniformFloat(float f, float g) {
        this.baseValue = f;
        this.spread = g;
    }

    public static UniformFloat of(float f, float g) {
        return new UniformFloat(f, g);
    }

    @Override
    public float sample(Random random) {
        if (this.spread == 0.0f) {
            return this.baseValue;
        }
        return Mth.randomBetween(random, this.baseValue, this.baseValue + this.spread);
    }

    @Override
    public float getMinValue() {
        return this.baseValue;
    }

    @Override
    public float getMaxValue() {
        return this.baseValue + this.spread;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.UNIFORM;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        UniformFloat uniformFloat = (UniformFloat)object;
        return this.baseValue == uniformFloat.baseValue && this.spread == uniformFloat.spread;
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.baseValue), Float.valueOf(this.spread));
    }

    public String toString() {
        return "[" + this.baseValue + '-' + (this.baseValue + this.spread) + ']';
    }
}

