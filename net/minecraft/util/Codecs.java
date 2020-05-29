/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.function.Function;

public class Codecs {
    private static Function<Integer, DataResult<Integer>> checkRange(int i, int j) {
        return integer -> {
            if (integer >= i && integer <= j) {
                return DataResult.success(integer);
            }
            return DataResult.error("Value " + integer + " outside of range [" + i + ":" + j + "]", integer);
        };
    }

    public static Codec<Integer> intRange(int i, int j) {
        Function<Integer, DataResult<Integer>> function = Codecs.checkRange(i, j);
        return Codec.INT.flatXmap(function, function);
    }

    private static Function<Double, DataResult<Double>> checkRange(double d, double e) {
        return double_ -> {
            if (double_ >= d && double_ <= e) {
                return DataResult.success(double_);
            }
            return DataResult.error("Value " + double_ + " outside of range [" + d + ":" + e + "]", double_);
        };
    }

    public static Codec<Double> doubleRange(double d, double e) {
        Function<Double, DataResult<Double>> function = Codecs.checkRange(d, e);
        return Codec.DOUBLE.flatXmap(function, function);
    }
}

