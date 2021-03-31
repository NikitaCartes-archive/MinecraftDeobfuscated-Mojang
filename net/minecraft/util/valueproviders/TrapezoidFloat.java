/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviderType;

public class TrapezoidFloat
extends FloatProvider {
    public static final Codec<TrapezoidFloat> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("min")).forGetter(trapezoidFloat -> Float.valueOf(trapezoidFloat.min)), ((MapCodec)Codec.FLOAT.fieldOf("max")).forGetter(trapezoidFloat -> Float.valueOf(trapezoidFloat.max)), ((MapCodec)Codec.FLOAT.fieldOf("plateau")).forGetter(trapezoidFloat -> Float.valueOf(trapezoidFloat.plateau))).apply((Applicative<TrapezoidFloat, ?>)instance, TrapezoidFloat::new)).comapFlatMap(trapezoidFloat -> {
        if (trapezoidFloat.max < trapezoidFloat.min) {
            return DataResult.error("Max must be larger than min: [" + trapezoidFloat.min + ", " + trapezoidFloat.max + "]");
        }
        if (trapezoidFloat.plateau > trapezoidFloat.max - trapezoidFloat.min) {
            return DataResult.error("Plateau can at most be the full span: [" + trapezoidFloat.min + ", " + trapezoidFloat.max + "]");
        }
        return DataResult.success(trapezoidFloat);
    }, Function.identity());
    private float min;
    private float max;
    private float plateau;

    public static TrapezoidFloat of(float f, float g, float h) {
        return new TrapezoidFloat(f, g, h);
    }

    private TrapezoidFloat(float f, float g, float h) {
        this.min = f;
        this.max = g;
        this.plateau = h;
    }

    @Override
    public float sample(Random random) {
        float f = this.max - this.min;
        float g = (f - this.plateau) / 2.0f;
        float h = f - g;
        return this.min + random.nextFloat() * h + random.nextFloat() * g;
    }

    @Override
    public float getMinValue() {
        return this.min;
    }

    @Override
    public float getMaxValue() {
        return this.max;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.TRAPEZOID;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        TrapezoidFloat trapezoidFloat = (TrapezoidFloat)object;
        return this.min == trapezoidFloat.min && this.max == trapezoidFloat.max && this.plateau == trapezoidFloat.plateau;
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.min), Float.valueOf(this.max), Float.valueOf(this.plateau));
    }

    public String toString() {
        return "trapezoid(" + this.plateau + ") in [" + this.min + '-' + this.max + ']';
    }
}

