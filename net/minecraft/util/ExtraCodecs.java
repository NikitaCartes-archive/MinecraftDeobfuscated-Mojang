/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import org.apache.commons.lang3.mutable.MutableObject;

public class ExtraCodecs {
    public static final Codec<Integer> NON_NEGATIVE_INT = ExtraCodecs.intRangeWithMessage(0, Integer.MAX_VALUE, integer -> "Value must be non-negative: " + integer);
    public static final Codec<Integer> POSITIVE_INT = ExtraCodecs.intRangeWithMessage(1, Integer.MAX_VALUE, integer -> "Value must be positive: " + integer);
    public static final Codec<Float> POSITIVE_FLOAT = ExtraCodecs.floatRangeMinExclusiveWithMessage(0.0f, Float.MAX_VALUE, float_ -> "Value must be positive: " + float_);

    public static <F, S> Codec<Either<F, S>> xor(Codec<F> codec, Codec<S> codec2) {
        return new XorCodec<F, S>(codec, codec2);
    }

    public static <P, I> Codec<I> intervalCodec(Codec<P> codec, String string, String string2, BiFunction<P, P, DataResult<I>> biFunction, Function<I, P> function, Function<I, P> function2) {
        Codec<Object> codec2 = Codec.list(codec).comapFlatMap(list2 -> Util.fixedSize(list2, 2).flatMap(list -> {
            Object object = list.get(0);
            Object object2 = list.get(1);
            return (DataResult)biFunction.apply(object, object2);
        }), object -> ImmutableList.of(function.apply(object), function2.apply(object)));
        Codec<Object> codec3 = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)codec.fieldOf(string)).forGetter(Pair::getFirst), ((MapCodec)codec.fieldOf(string2)).forGetter(Pair::getSecond)).apply((Applicative<Pair, ?>)instance, Pair::of)).comapFlatMap(pair -> (DataResult)biFunction.apply(pair.getFirst(), pair.getSecond()), object -> Pair.of(function.apply(object), function2.apply(object)));
        Codec<Object> codec4 = new EitherCodec<Object, Object>(codec2, codec3).xmap(either -> either.map(object -> object, object -> object), Either::left);
        return Codec.either(codec, codec4).comapFlatMap(either -> either.map(object -> (DataResult)biFunction.apply(object, object), DataResult::success), object -> {
            Object object3;
            Object object2 = function.apply(object);
            if (Objects.equals(object2, object3 = function2.apply(object))) {
                return Either.left(object2);
            }
            return Either.right(object);
        });
    }

    public static <A> Codec.ResultFunction<A> orElsePartial(final A object) {
        return new Codec.ResultFunction<A>(){

            @Override
            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> dynamicOps, T object2, DataResult<Pair<A, T>> dataResult) {
                MutableObject mutableObject = new MutableObject();
                Optional optional = dataResult.resultOrPartial(mutableObject::setValue);
                if (optional.isPresent()) {
                    return dataResult;
                }
                return DataResult.error("(" + (String)mutableObject.getValue() + " -> using default)", Pair.of(object, object2));
            }

            @Override
            public <T> DataResult<T> coApply(DynamicOps<T> dynamicOps, A object2, DataResult<T> dataResult) {
                return dataResult;
            }

            public String toString() {
                return "OrElsePartial[" + object + "]";
            }
        };
    }

    public static <E> Codec<E> idResolverCodec(ToIntFunction<E> toIntFunction, IntFunction<E> intFunction, int i) {
        return Codec.INT.flatXmap(integer -> Optional.ofNullable(intFunction.apply((int)integer)).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown element id: " + integer)), object -> {
            int j = toIntFunction.applyAsInt(object);
            return j == i ? DataResult.error("Element with unknown id: " + object) : DataResult.success(j);
        });
    }

    public static <E> Codec<E> stringResolverCodec(Function<E, String> function, Function<String, E> function2) {
        return Codec.STRING.flatXmap(string -> Optional.ofNullable(function2.apply((String)string)).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown element name:" + string)), object -> Optional.ofNullable((String)function.apply(object)).map(DataResult::success).orElseGet(() -> DataResult.error("Element with unknown name: " + object)));
    }

    public static <E> Codec<E> orCompressed(final Codec<E> codec, final Codec<E> codec2) {
        return new Codec<E>(){

            @Override
            public <T> DataResult<T> encode(E object, DynamicOps<T> dynamicOps, T object2) {
                if (dynamicOps.compressMaps()) {
                    return codec2.encode(object, dynamicOps, object2);
                }
                return codec.encode(object, dynamicOps, object2);
            }

            @Override
            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> dynamicOps, T object) {
                if (dynamicOps.compressMaps()) {
                    return codec2.decode(dynamicOps, object);
                }
                return codec.decode(dynamicOps, object);
            }

            public String toString() {
                return codec + " orCompressed " + codec2;
            }
        };
    }

    public static <E> Codec<E> overrideLifecycle(Codec<E> codec, final Function<E, Lifecycle> function, final Function<E, Lifecycle> function2) {
        return codec.mapResult(new Codec.ResultFunction<E>(){

            @Override
            public <T> DataResult<Pair<E, T>> apply(DynamicOps<T> dynamicOps, T object, DataResult<Pair<E, T>> dataResult) {
                return dataResult.result().map(pair -> dataResult.setLifecycle((Lifecycle)function.apply(pair.getFirst()))).orElse(dataResult);
            }

            @Override
            public <T> DataResult<T> coApply(DynamicOps<T> dynamicOps, E object, DataResult<T> dataResult) {
                return dataResult.setLifecycle((Lifecycle)function2.apply(object));
            }

            public String toString() {
                return "WithLifecycle[" + function + " " + function2 + "]";
            }
        });
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

    private static <N extends Number> Function<N, DataResult<N>> checkRangeMinExclusiveWithMessage(N number, N number2, Function<N, String> function) {
        return number3 -> {
            if (((Comparable)((Object)number3)).compareTo(number) > 0 && ((Comparable)((Object)number3)).compareTo(number2) <= 0) {
                return DataResult.success(number3);
            }
            return DataResult.error((String)function.apply(number3));
        };
    }

    private static Codec<Float> floatRangeMinExclusiveWithMessage(float f, float g, Function<Float, String> function) {
        Function<Float, DataResult<Float>> function2 = ExtraCodecs.checkRangeMinExclusiveWithMessage(Float.valueOf(f), Float.valueOf(g), function);
        return Codec.FLOAT.flatXmap(function2, function2);
    }

    public static <T> Function<List<T>, DataResult<List<T>>> nonEmptyListCheck() {
        return list -> {
            if (list.isEmpty()) {
                return DataResult.error("List must have contents");
            }
            return DataResult.success(list);
        };
    }

    public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> codec) {
        return codec.flatXmap(ExtraCodecs.nonEmptyListCheck(), ExtraCodecs.nonEmptyListCheck());
    }

    public static <T> Function<List<Supplier<T>>, DataResult<List<Supplier<T>>>> nonNullSupplierListCheck() {
        return list -> {
            ArrayList<CallSite> list2 = Lists.newArrayList();
            for (int i = 0; i < list.size(); ++i) {
                Supplier supplier = (Supplier)list.get(i);
                try {
                    if (supplier.get() != null) continue;
                    list2.add((CallSite)((Object)("Missing value [" + i + "] : " + supplier)));
                    continue;
                } catch (Exception exception) {
                    list2.add((CallSite)((Object)("Invalid value [" + i + "]: " + supplier + ", message: " + exception.getMessage())));
                }
            }
            if (!list2.isEmpty()) {
                return DataResult.error(String.join((CharSequence)"; ", list2));
            }
            return DataResult.success(list, Lifecycle.stable());
        };
    }

    public static <T> Function<Supplier<T>, DataResult<Supplier<T>>> nonNullSupplierCheck() {
        return supplier -> {
            try {
                if (supplier.get() == null) {
                    return DataResult.error("Missing value: " + supplier);
                }
            } catch (Exception exception) {
                return DataResult.error("Invalid value: " + supplier + ", message: " + exception.getMessage());
            }
            return DataResult.success(supplier, Lifecycle.stable());
        };
    }

    public static <A> Codec<A> lazyInitializedCodec(Supplier<Codec<A>> supplier) {
        return new LazyInitializedCodec<A>(supplier);
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
            return "XorCodec[" + this.first + ", " + this.second + "]";
        }

        @Override
        public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
            return this.encode((Either)object, dynamicOps, object2);
        }
    }

    static final class EitherCodec<F, S>
    implements Codec<Either<F, S>> {
        private final Codec<F> first;
        private final Codec<S> second;

        public EitherCodec(Codec<F> codec, Codec<S> codec2) {
            this.first = codec;
            this.second = codec2;
        }

        @Override
        public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> dynamicOps, T object) {
            DataResult<Pair<Either<F, Pair>, T>> dataResult = this.first.decode(dynamicOps, object).map((? super R pair) -> pair.mapFirst(Either::left));
            if (!dataResult.error().isPresent()) {
                return dataResult;
            }
            DataResult<Pair<Either<F, S>, T>> dataResult2 = this.second.decode(dynamicOps, object).map((? super R pair) -> pair.mapFirst(Either::right));
            if (!dataResult2.error().isPresent()) {
                return dataResult2;
            }
            return dataResult.apply2((pair, pair2) -> pair2, dataResult2);
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
            EitherCodec eitherCodec = (EitherCodec)object;
            return Objects.equals(this.first, eitherCodec.first) && Objects.equals(this.second, eitherCodec.second);
        }

        public int hashCode() {
            return Objects.hash(this.first, this.second);
        }

        public String toString() {
            return "EitherCodec[" + this.first + ", " + this.second + "]";
        }

        @Override
        public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
            return this.encode((Either)object, dynamicOps, object2);
        }
    }

    record LazyInitializedCodec<A>(Supplier<Codec<A>> delegate) implements Codec<A>
    {
        LazyInitializedCodec {
            supplier = Suppliers.memoize(supplier::get);
        }

        @Override
        public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> dynamicOps, T object) {
            return this.delegate.get().decode(dynamicOps, object);
        }

        @Override
        public <T> DataResult<T> encode(A object, DynamicOps<T> dynamicOps, T object2) {
            return this.delegate.get().encode(object, dynamicOps, object2);
        }
    }
}

