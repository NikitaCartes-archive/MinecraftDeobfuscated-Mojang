/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class MapFiller {
    public static <K, V> Map<K, V> linkedHashMapFrom(Iterable<K> iterable, Iterable<V> iterable2) {
        return MapFiller.from(iterable, iterable2, Maps.newLinkedHashMap());
    }

    public static <K, V> Map<K, V> from(Iterable<K> iterable, Iterable<V> iterable2, Map<K, V> map) {
        Iterator<V> iterator = iterable2.iterator();
        for (K object : iterable) {
            map.put(object, iterator.next());
        }
        if (iterator.hasNext()) {
            throw new NoSuchElementException();
        }
        return map;
    }
}

