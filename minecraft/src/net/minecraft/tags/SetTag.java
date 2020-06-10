package net.minecraft.tags;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SetTag<T> implements Tag<T> {
	private static final SetTag<Object> EMPTY = new SetTag<>(Collections.emptySet(), Object.class);
	private final ImmutableList<T> valuesList;
	private final Set<T> values;
	@VisibleForTesting
	protected final Class<?> closestCommonSuperType;

	protected SetTag(Set<T> set, Class<?> class_) {
		this.closestCommonSuperType = class_;
		this.values = set;
		this.valuesList = ImmutableList.copyOf(set);
	}

	public static <T> SetTag<T> empty() {
		return (SetTag<T>)EMPTY;
	}

	public static <T> SetTag<T> create(Set<T> set) {
		return set.isEmpty() ? empty() : new SetTag<>(set, findCommonSuperClass(set));
	}

	@Override
	public boolean contains(T object) {
		return this.closestCommonSuperType.isInstance(object) && this.values.contains(object);
	}

	@Override
	public List<T> getValues() {
		return this.valuesList;
	}

	private static <T> Class<?> findCommonSuperClass(Set<T> set) {
		if (set.isEmpty()) {
			throw new IllegalArgumentException("Expected non-empty set. Got: " + set);
		} else {
			Class<?> class_ = null;

			for (T object : set) {
				if (class_ == null) {
					class_ = object.getClass();
				} else {
					class_ = findClosestAncestor(class_, object.getClass());
				}
			}

			return class_;
		}
	}

	private static Class<?> findClosestAncestor(Class<?> class_, Class<?> class2) {
		while (!class_.isAssignableFrom(class2)) {
			class_ = class_.getSuperclass();
		}

		return class_;
	}
}
