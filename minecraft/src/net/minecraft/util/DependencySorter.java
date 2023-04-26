package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DependencySorter<K, V extends DependencySorter.Entry<K>> {
	private final Map<K, V> contents = new HashMap();

	public DependencySorter<K, V> addEntry(K object, V entry) {
		this.contents.put(object, entry);
		return this;
	}

	private void visitDependenciesAndElement(Multimap<K, K> multimap, Set<K> set, K object, BiConsumer<K, V> biConsumer) {
		if (set.add(object)) {
			multimap.get(object).forEach(objectx -> this.visitDependenciesAndElement(multimap, set, (K)objectx, biConsumer));
			V entry = (V)this.contents.get(object);
			if (entry != null) {
				biConsumer.accept(object, entry);
			}
		}
	}

	private static <K> boolean isCyclic(Multimap<K, K> multimap, K object, K object2) {
		Collection<K> collection = multimap.get(object2);
		return collection.contains(object) ? true : collection.stream().anyMatch(object2x -> isCyclic(multimap, object, (K)object2x));
	}

	private static <K> void addDependencyIfNotCyclic(Multimap<K, K> multimap, K object, K object2) {
		if (!isCyclic(multimap, object, object2)) {
			multimap.put(object, object2);
		}
	}

	public void orderByDependencies(BiConsumer<K, V> biConsumer) {
		Multimap<K, K> multimap = HashMultimap.create();
		this.contents.forEach((object, entry) -> entry.visitRequiredDependencies(object2 -> addDependencyIfNotCyclic(multimap, (K)object, (K)object2)));
		this.contents.forEach((object, entry) -> entry.visitOptionalDependencies(object2 -> addDependencyIfNotCyclic(multimap, (K)object, (K)object2)));
		Set<K> set = new HashSet();
		this.contents.keySet().forEach(object -> this.visitDependenciesAndElement(multimap, set, (K)object, biConsumer));
	}

	public interface Entry<K> {
		void visitRequiredDependencies(Consumer<K> consumer);

		void visitOptionalDependencies(Consumer<K> consumer);
	}
}
