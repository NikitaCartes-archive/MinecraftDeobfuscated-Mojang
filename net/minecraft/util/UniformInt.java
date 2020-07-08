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

public class UniformInt {
    public static final Codec<UniformInt> CODEC = Codec.either(Codec.INT, RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("base")).forGetter(uniformInt -> uniformInt.baseValue), ((MapCodec)Codec.INT.fieldOf("spread")).forGetter(uniformInt -> uniformInt.spread)).apply((Applicative<UniformInt, ?>)instance, UniformInt::new)).comapFlatMap(uniformInt -> {
        if (uniformInt.spread < 0) {
            return DataResult.error("Spread must be non-negative, got: " + uniformInt.spread);
        }
        return DataResult.success(uniformInt);
    }, Function.identity())).xmap(either -> either.map(UniformInt::fixed, uniformInt -> uniformInt), uniformInt -> uniformInt.spread == 0 ? Either.left(uniformInt.baseValue) : Either.right(uniformInt));
    private final int baseValue;
    private final int spread;

    public static Codec<UniformInt> codec(int i, int j, int k) {
        Function<UniformInt, DataResult> function = uniformInt -> {
            if (uniformInt.baseValue >= i && uniformInt.baseValue <= j) {
                if (uniformInt.spread <= k) {
                    return DataResult.success(uniformInt);
                }
                return DataResult.error("Spread too big: " + uniformInt.spread + " > " + k);
            }
            return DataResult.error("Base value out of range: " + uniformInt.baseValue + " [" + i + "-" + j + "]");
        };
        return CODEC.flatXmap(function, function);
    }

    private UniformInt(int i, int j) {
        this.baseValue = i;
        this.spread = j;
    }

    public static UniformInt fixed(int i) {
        return new UniformInt(i, 0);
    }

    public static UniformInt of(int i, int j) {
        return new UniformInt(i, j);
    }

    public int sample(Random random) {
        if (this.spread == 0) {
            return this.baseValue;
        }
        return this.baseValue + random.nextInt(this.spread + 1);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        UniformInt uniformInt = (UniformInt)object;
        return this.baseValue == uniformInt.baseValue && this.spread == uniformInt.spread;
    }

    public int hashCode() {
        return Objects.hash(this.baseValue, this.spread);
    }

    public String toString() {
        return "[" + this.baseValue + '-' + (this.baseValue + this.spread) + ']';
    }
}

