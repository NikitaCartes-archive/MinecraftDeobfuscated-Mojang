package net.minecraft.util;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class Graph {
	private Graph() {
	}

	public static <T> boolean depthFirstSearch(Map<T, Set<T>> map, Set<T> set, Set<T> set2, Consumer<T> consumer, T object) {
		if (set.contains(object)) {
			return false;
		} else if (set2.contains(object)) {
			return true;
		} else {
			set2.add(object);

			for (T object2 : (Set)map.getOrDefault(object, ImmutableSet.of())) {
				if (depthFirstSearch(map, set, set2, consumer, object2)) {
					return true;
				}
			}

			set2.remove(object);
			set.add(object);
			consumer.accept(object);
			return false;
		}
	}
}
