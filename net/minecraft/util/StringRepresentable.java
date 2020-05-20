/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface StringRepresentable {
    public String getSerializedName();

    public static <E extends Enum<E>> Codec<E> fromEnum(Supplier<E[]> supplier, Function<? super String, ? extends E> function) {
        Enum[] enums = (Enum[])supplier.get();
        return StringRepresentable.fromStringResolver(Enum::ordinal, i -> enums[i], function);
    }

    public static <E extends StringRepresentable> Codec<E> fromStringResolver(final ToIntFunction<E> toIntFunction, final IntFunction<E> intFunction, final Function<? super String, ? extends E> function) {
        return new Codec<E>(){

            @Override
            public <T> DataResult<T> encode(E stringRepresentable, DynamicOps<T> dynamicOps, T object) {
                if (dynamicOps.compressMaps()) {
                    return dynamicOps.mergeToPrimitive(object, dynamicOps.createInt(toIntFunction.applyAsInt(stringRepresentable)));
                }
                return dynamicOps.mergeToPrimitive(object, dynamicOps.createString(stringRepresentable.getSerializedName()));
            }

            @Override
            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> dynamicOps, T object) {
                if (dynamicOps.compressMaps()) {
                    return dynamicOps.getNumberValue(object).flatMap((? super R number) -> Optional.ofNullable(intFunction.apply(number.intValue())).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown element id: " + number))).map((? super R stringRepresentable) -> Pair.of(stringRepresentable, dynamicOps.empty()));
                }
                return dynamicOps.getStringValue(object).flatMap((? super R string) -> Optional.ofNullable(function.apply(string)).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown element name: " + string))).map((? super R stringRepresentable) -> Pair.of(stringRepresentable, dynamicOps.empty()));
            }

            public String toString() {
                return "StringRepresentable[" + toIntFunction + "]";
            }

            @Override
            public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
                return this.encode((E)((StringRepresentable)object), (DynamicOps<T>)dynamicOps, (T)object2);
            }
        };
    }

    public static Keyable keys(final StringRepresentable[] stringRepresentables) {
        return new Keyable(){

            @Override
            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                if (dynamicOps.compressMaps()) {
                    return IntStream.range(0, stringRepresentables.length).mapToObj(dynamicOps::createInt);
                }
                return Arrays.stream(stringRepresentables).map(StringRepresentable::getSerializedName).map(dynamicOps::createString);
            }
        };
    }
}

