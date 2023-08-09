package net.minecraft.stats;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;

public class StatType<T> implements Iterable<Stat<T>> {
	private final Registry<T> registry;
	private final Map<T, Stat<T>> map = new IdentityHashMap();
	private final Component displayName;

	public StatType(Registry<T> registry, Component component) {
		this.registry = registry;
		this.displayName = component;
	}

	public boolean contains(T object) {
		return this.map.containsKey(object);
	}

	public Stat<T> get(T object, StatFormatter statFormatter) {
		return (Stat<T>)this.map.computeIfAbsent(object, objectx -> new Stat<>(this, (T)objectx, statFormatter));
	}

	public Registry<T> getRegistry() {
		return this.registry;
	}

	public Iterator<Stat<T>> iterator() {
		return this.map.values().iterator();
	}

	public Stat<T> get(T object) {
		return this.get(object, StatFormatter.DEFAULT);
	}

	public Component getDisplayName() {
		return this.displayName;
	}
}
