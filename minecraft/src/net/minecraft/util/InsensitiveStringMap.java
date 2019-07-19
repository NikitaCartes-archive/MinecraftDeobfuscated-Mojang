package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class InsensitiveStringMap<V> implements Map<String, V> {
	private final Map<String, V> map = Maps.<String, V>newLinkedHashMap();

	public int size() {
		return this.map.size();
	}

	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	public boolean containsKey(Object object) {
		return this.map.containsKey(object.toString().toLowerCase(Locale.ROOT));
	}

	public boolean containsValue(Object object) {
		return this.map.containsValue(object);
	}

	public V get(Object object) {
		return (V)this.map.get(object.toString().toLowerCase(Locale.ROOT));
	}

	public V put(String string, V object) {
		return (V)this.map.put(string.toLowerCase(Locale.ROOT), object);
	}

	public V remove(Object object) {
		return (V)this.map.remove(object.toString().toLowerCase(Locale.ROOT));
	}

	public void putAll(Map<? extends String, ? extends V> map) {
		for (Entry<? extends String, ? extends V> entry : map.entrySet()) {
			this.put((String)entry.getKey(), (V)entry.getValue());
		}
	}

	public void clear() {
		this.map.clear();
	}

	public Set<String> keySet() {
		return this.map.keySet();
	}

	public Collection<V> values() {
		return this.map.values();
	}

	public Set<Entry<String, V>> entrySet() {
		return this.map.entrySet();
	}
}
