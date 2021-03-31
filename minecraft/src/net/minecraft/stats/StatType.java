package net.minecraft.stats;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class StatType<T> implements Iterable<Stat<T>> {
	private final Registry<T> registry;
	private final Map<T, Stat<T>> map = new IdentityHashMap();
	@Nullable
	private Component displayName;

	public StatType(Registry<T> registry) {
		this.registry = registry;
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

	public String getTranslationKey() {
		return "stat_type." + Registry.STAT_TYPE.getKey(this).toString().replace(':', '.');
	}

	public Component getDisplayName() {
		if (this.displayName == null) {
			this.displayName = new TranslatableComponent(this.getTranslationKey());
		}

		return this.displayName;
	}
}
