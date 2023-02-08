package net.minecraft;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class Optionull {
	@Nullable
	public static <T, R> R map(@Nullable T object, Function<T, R> function) {
		return (R)(object == null ? null : function.apply(object));
	}

	public static <T, R> R mapOrDefault(@Nullable T object, Function<T, R> function, R object2) {
		return (R)(object == null ? object2 : function.apply(object));
	}

	public static <T, R> R mapOrElse(@Nullable T object, Function<T, R> function, Supplier<R> supplier) {
		return (R)(object == null ? supplier.get() : function.apply(object));
	}

	@Nullable
	public static <T> T first(Collection<T> collection) {
		Iterator<T> iterator = collection.iterator();
		return (T)(iterator.hasNext() ? iterator.next() : null);
	}

	public static <T> T firstOrDefault(Collection<T> collection, T object) {
		Iterator<T> iterator = collection.iterator();
		return (T)(iterator.hasNext() ? iterator.next() : object);
	}

	public static <T> T firstOrElse(Collection<T> collection, Supplier<T> supplier) {
		Iterator<T> iterator = collection.iterator();
		return (T)(iterator.hasNext() ? iterator.next() : supplier.get());
	}

	public static <T> boolean isNullOrEmpty(@Nullable T[] objects) {
		return objects == null || objects.length == 0;
	}

	public static boolean isNullOrEmpty(@Nullable boolean[] bls) {
		return bls == null || bls.length == 0;
	}

	public static boolean isNullOrEmpty(@Nullable byte[] bs) {
		return bs == null || bs.length == 0;
	}

	public static boolean isNullOrEmpty(@Nullable char[] cs) {
		return cs == null || cs.length == 0;
	}

	public static boolean isNullOrEmpty(@Nullable short[] ss) {
		return ss == null || ss.length == 0;
	}

	public static boolean isNullOrEmpty(@Nullable int[] is) {
		return is == null || is.length == 0;
	}

	public static boolean isNullOrEmpty(@Nullable long[] ls) {
		return ls == null || ls.length == 0;
	}

	public static boolean isNullOrEmpty(@Nullable float[] fs) {
		return fs == null || fs.length == 0;
	}

	public static boolean isNullOrEmpty(@Nullable double[] ds) {
		return ds == null || ds.length == 0;
	}
}
