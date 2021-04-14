/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

public class ConstantInt
extends IntProvider {
    public static final ConstantInt ZERO = new ConstantInt(0);
    public static final Codec<ConstantInt> CODEC = Codec.either(Codec.INT, RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("value")).forGetter(constantInt -> constantInt.value)).apply((Applicative<ConstantInt, ?>)instance, ConstantInt::new))).xmap(either -> either.map(ConstantInt::of, constantInt -> constantInt), constantInt -> Either.left(constantInt.value));
    private final int value;

    public static ConstantInt of(int i) {
        if (i == 0) {
            return ZERO;
        }
        return new ConstantInt(i);
    }

    private ConstantInt(int i) {
        this.value = i;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public int sample(Random random) {
        return this.value;
    }

    @Override
    public int getMinValue() {
        return this.value;
    }

    @Override
    public int getMaxValue() {
        return this.value;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.CONSTANT;
    }

    public String toString() {
        return Integer.toString(this.value);
    }
}

