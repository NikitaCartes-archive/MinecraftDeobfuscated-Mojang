package net.minecraft.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public class ByIdMap {
	private static <T> IntFunction<T> createMap(ToIntFunction<T> toIntFunction, T[] objects) {
		if (objects.length == 0) {
			throw new IllegalArgumentException("Empty value list");
		} else {
			Int2ObjectMap<T> int2ObjectMap = new Int2ObjectOpenHashMap<>();

			for (T object : objects) {
				int i = toIntFunction.applyAsInt(object);
				T object2 = int2ObjectMap.put(i, object);
				if (object2 != null) {
					throw new IllegalArgumentException("Duplicate entry on id " + i + ": current=" + object + ", previous=" + object2);
				}
			}

			return int2ObjectMap;
		}
	}

	public static <T> IntFunction<T> sparse(ToIntFunction<T> toIntFunction, T[] objects, T object) {
		IntFunction<T> intFunction = createMap(toIntFunction, objects);
		return i -> Objects.requireNonNullElse(intFunction.apply(i), object);
	}

	private static <T> T[] createSortedArray(ToIntFunction<T> toIntFunction, T[] objects) {
		int i = objects.length;
		if (i == 0) {
			throw new IllegalArgumentException("Empty value list");
		} else {
			T[] objects2 = (T[])objects.clone();
			Arrays.fill(objects2, null);

			for (T object : objects) {
				int j = toIntFunction.applyAsInt(object);
				if (j < 0 || j >= i) {
					throw new IllegalArgumentException("Values are not continous, found index " + j + " for value " + object);
				}

				T object2 = objects2[j];
				if (object2 != null) {
					throw new IllegalArgumentException("Duplicate entry on id " + j + ": current=" + object + ", previous=" + object2);
				}

				objects2[j] = object;
			}

			for (int k = 0; k < i; k++) {
				if (objects2[k] == null) {
					throw new IllegalArgumentException("Missing value at index: " + k);
				}
			}

			return objects2;
		}
	}

	public static <T> IntFunction<T> continuous(ToIntFunction<T> toIntFunction, T[] objects, ByIdMap.OutOfBoundsStrategy outOfBoundsStrategy) {
		T[] objects2 = (T[])createSortedArray(toIntFunction, objects);
		int i = objects2.length;

		return switch (outOfBoundsStrategy) {
			case ZERO -> {
				T object = objects2[0];
				yield j -> j >= 0 && j < i ? objects2[j] : object;
			}
			case WRAP -> j -> objects2[Mth.positiveModulo(j, i)];
			case CLAMP -> j -> objects2[Mth.clamp(j, 0, i - 1)];
		};
	}

	public static enum OutOfBoundsStrategy {
		ZERO,
		WRAP,
		CLAMP;
	}
}
