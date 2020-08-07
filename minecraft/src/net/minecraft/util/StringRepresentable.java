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
	String getSerializedName();

	static <E extends Enum<E> & StringRepresentable> Codec<E> fromEnum(Supplier<E[]> supplier, Function<? super String, ? extends E> function) {
		E[] enums = (E[])supplier.get();
		return fromStringResolver(Enum::ordinal, i -> enums[i], function);
	}

	static <E extends StringRepresentable> Codec<E> fromStringResolver(
		ToIntFunction<E> toIntFunction, IntFunction<E> intFunction, Function<? super String, ? extends E> function
	) {
		return new Codec<E>() {
			public <T> DataResult<T> encode(E stringRepresentable, DynamicOps<T> dynamicOps, T object) {
				return dynamicOps.compressMaps()
					? dynamicOps.mergeToPrimitive(object, dynamicOps.createInt(toIntFunction.applyAsInt(stringRepresentable)))
					: dynamicOps.mergeToPrimitive(object, dynamicOps.createString(stringRepresentable.getSerializedName()));
			}

			@Override
			public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> dynamicOps, T object) {
				return dynamicOps.compressMaps()
					? dynamicOps.getNumberValue(object)
						.flatMap(
							number -> (DataResult)Optional.ofNullable(intFunction.apply(number.intValue()))
									.map(DataResult::success)
									.orElseGet(() -> DataResult.error("Unknown element id: " + number))
						)
						.map(stringRepresentable -> Pair.of(stringRepresentable, dynamicOps.empty()))
					: dynamicOps.getStringValue(object)
						.flatMap(
							string -> (DataResult)Optional.ofNullable(function.apply(string))
									.map(DataResult::success)
									.orElseGet(() -> DataResult.error("Unknown element name: " + string))
						)
						.map(stringRepresentable -> Pair.of(stringRepresentable, dynamicOps.empty()));
			}

			public String toString() {
				return "StringRepresentable[" + toIntFunction + "]";
			}
		};
	}

	static Keyable keys(StringRepresentable[] stringRepresentables) {
		return new Keyable() {
			@Override
			public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
				return dynamicOps.compressMaps()
					? IntStream.range(0, stringRepresentables.length).mapToObj(dynamicOps::createInt)
					: Arrays.stream(stringRepresentables).map(StringRepresentable::getSerializedName).map(dynamicOps::createString);
			}
		};
	}
}
