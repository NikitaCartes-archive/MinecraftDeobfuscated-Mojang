/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.Mth;

public class UniformFloat {
    public static final Codec<UniformFloat> CODEC = Codec.either(Codec.FLOAT, RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("base")).forGetter(uniformFloat -> Float.valueOf(uniformFloat.baseValue)), ((MapCodec)Codec.FLOAT.fieldOf("spread")).forGetter(uniformFloat -> Float.valueOf(uniformFloat.spread))).apply((Applicative<UniformFloat, ?>)instance, UniformFloat::new)).comapFlatMap(uniformFloat -> {
        if (uniformFloat.spread < 0.0f) {
            return DataResult.error("Spread must be non-negative, got: " + uniformFloat.spread);
        }
        return DataResult.success(uniformFloat);
    }, Function.identity())).xmap(either -> either.map(UniformFloat::fixed, uniformFloat -> uniformFloat), uniformFloat -> uniformFloat.spread == 0.0f ? Either.left(Float.valueOf(uniformFloat.baseValue)) : Either.right(uniformFloat));
    private final float baseValue;
    private final float spread;

    public static Codec<UniformFloat> codec(float f, float g, float h) {
        Function<UniformFloat, DataResult> function = uniformFloat -> {
            if (uniformFloat.baseValue >= f && uniformFloat.baseValue <= g) {
                if (uniformFloat.spread <= h) {
                    return DataResult.success(uniformFloat);
                }
                return DataResult.error("Spread too big: " + uniformFloat.spread + " > " + h);
            }
            return DataResult.error("Base value out of range: " + uniformFloat.baseValue + " [" + f + "-" + g + "]");
        };
        return CODEC.flatXmap(function, function);
    }

    private UniformFloat(float f, float g) {
        this.baseValue = f;
        this.spread = g;
    }

    public static UniformFloat fixed(float f) {
        return new UniformFloat(f, 0.0f);
    }

    public static UniformFloat of(float f, float g) {
        return new UniformFloat(f, g);
    }

    public float sample(Random random) {
        if (this.spread == 0.0f) {
            return this.baseValue;
        }
        return Mth.randomBetween(random, this.baseValue, this.baseValue + this.spread);
    }

    public float getBaseValue() {
        return this.baseValue;
    }

    public float getMaxValue() {
        return this.baseValue + this.spread;
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

