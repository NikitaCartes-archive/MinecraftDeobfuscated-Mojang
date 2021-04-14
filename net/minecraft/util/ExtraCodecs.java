/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class ExtraCodecs {
    public static final Codec<Integer> NON_NEGATIVE_INT = ExtraCodecs.intRangeWithMessage(0, Integer.MAX_VALUE, integer -> "Value must be non-negative: " + integer);
    public static final Codec<Integer> POSITIVE_INT = ExtraCodecs.intRangeWithMessage(1, Integer.MAX_VALUE, integer -> "Value must be positive: " + integer);

    public static <F, S> Codec<Either<F, S>> xor(Codec<F> codec, Codec<S> codec2) {
        return new XorCodec<F, S>(codec, codec2);
    }

    private static <N extends Number> Function<N, DataResult<N>> checkRangeWithMessage(N number, N number2, Function<N, String> function) {
        return number3 -> {
            if (((Comparable)((Object)number3)).compareTo(number) >= 0 && ((Comparable)((Object)number3)).compareTo(number2) <= 0) {
                return DataResult.success(number3);
            }
            return DataResult.error((String)function.apply(number3));
        };
    }

    private static Codec<Integer> intRangeWithMessage(int i, int j, Function<Integer, String> function) {
        Function<Integer, DataResult<Integer>> function2 = ExtraCodecs.checkRangeWithMessage(i, j, function);
        return Codec.INT.flatXmap(function2, function2);
    }

    public static <T> Function<List<T>, DataResult<List<T>>> nonEmptyListCheck() {
        return list -> {
            if (list.isEmpty()) {
                return DataResult.error("List must have contents");
            }
            return DataResult.success(list);
        };
    }

    static final class XorCodec<F, S>
    implements Codec<Either<F, S>> {
        private final Codec<F> first;
        private final Codec<S> second;

        public XorCodec(Codec<F> codec, Codec<S> codec2) {
            this.first = codec;
            this.second = codec2;
        }

        @Override
        public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> dynamicOps, T object) {
            DataResult<Pair<Either<F, S>, T>> dataResult = this.first.decode(dynamicOps, object).map((? super R pair) -> pair.mapFirst(Either::left));
            DataResult<Pair> dataResult2 = this.second.decode(dynamicOps, object).map((? super R pair) -> pair.mapFirst(Either::right));
            Optional<Pair> optional = dataResult.result();
            Optional<Pair> optional2 = dataResult2.result();
            if (optional.isPresent() && optional2.isPresent()) {
                return DataResult.error("Both alternatives read successfully, can not pick the correct one; first: " + optional.get() + " second: " + optional2.get(), optional.get());
            }
            return optional.isPresent() ? dataResult : dataResult2;
        }

        @Override
        public <T> DataResult<T> encode(Either<F, S> either, DynamicOps<T> dynamicOps, T object) {
            return either.map(object2 -> this.first.encode(object2, dynamicOps, object), object2 -> this.second.encode(object2, dynamicOps, object));
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            XorCodec xorCodec = (XorCodec)object;
            return Objects.equals(this.first, xorCodec.first) && Objects.equals(this.second, xorCodec.second);
        }

        public int hashCode() {
            return Objects.hash(this.first, this.second);
        }

        public String toString() {
            return "XorCodec[" + this.first + ", " + this.second + ']';
        }

        @Override
        public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
            return this.encode((Either)object, dynamicOps, object2);
        }
    }
}

