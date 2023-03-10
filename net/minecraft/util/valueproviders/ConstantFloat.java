/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviderType;

public class ConstantFloat
extends FloatProvider {
    public static final ConstantFloat ZERO = new ConstantFloat(0.0f);
    public static final Codec<ConstantFloat> CODEC = Codec.either(Codec.FLOAT, RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("value")).forGetter(constantFloat -> Float.valueOf(constantFloat.value))).apply((Applicative<ConstantFloat, ?>)instance, ConstantFloat::new))).xmap(either -> either.map(ConstantFloat::of, constantFloat -> constantFloat), constantFloat -> Either.left(Float.valueOf(constantFloat.value)));
    private final float value;

    public static ConstantFloat of(float f) {
        if (f == 0.0f) {
            return ZERO;
        }
        return new ConstantFloat(f);
    }

    private ConstantFloat(float f) {
        this.value = f;
    }

    public float getValue() {
        return this.value;
    }

    @Override
    public float sample(RandomSource randomSource) {
        return this.value;
    }

    @Override
    public float getMinValue() {
        return this.value;
    }

    @Override
    public float getMaxValue() {
        return this.value + 1.0f;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.CONSTANT;
    }

    public String toString() {
        return Float.toString(this.value);
    }
}

