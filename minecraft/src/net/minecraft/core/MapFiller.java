package net.minecraft.core;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class MapFiller {
	public static <K, V> Map<K, V> linkedHashMapFrom(Iterable<K> iterable, Iterable<V> iterable2) {
		return from(iterable, iterable2, Maps.<K, V>newLinkedHashMap());
	}

	public static <K, V> Map<K, V> from(Iterable<K> iterable, Iterable<V> iterable2, Map<K, V> map) {
		Iterator<V> iterator = iterable2.iterator();

		for (K object : iterable) {
			map.put(object, iterator.next());
		}

		if (iterator.hasNext()) {
			throw new NoSuchElementException();
		} else {
			return map;
		}
	}
}
