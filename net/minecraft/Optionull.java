/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public class Optionull {
    @Nullable
    public static <T, R> R map(@Nullable T object, Function<T, R> function) {
        return object == null ? null : (R)function.apply(object);
    }

    public static <T, R> R mapOrDefault(@Nullable T object, Function<T, R> function, R object2) {
        return object == null ? object2 : function.apply(object);
    }

    public static <T, R> R mapOrElse(@Nullable T object, Function<T, R> function, Supplier<R> supplier) {
        return object == null ? supplier.get() : function.apply(object);
    }

    @Nullable
    public static <T> T first(Collection<T> collection) {
        Iterator<T> iterator = collection.iterator();
        return iterator.hasNext() ? (T)iterator.next() : null;
    }

    public static <T> T firstOrDefault(Collection<T> collection, T object) {
        Iterator<T> iterator = collection.iterator();
        return iterator.hasNext() ? iterator.next() : object;
    }

    public static <T> T firstOrElse(Collection<T> collection, Supplier<T> supplier) {
        Iterator<T> iterator = collection.iterator();
        return iterator.hasNext() ? iterator.next() : supplier.get();
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

