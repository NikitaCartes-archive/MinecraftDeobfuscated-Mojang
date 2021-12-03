package net.minecraft.core;

import javax.annotation.Nullable;

public interface IdMap<T> extends Iterable<T> {
	int DEFAULT = -1;

	int getId(T object);

	@Nullable
	T byId(int i);

	default T byIdOrThrow(int i) {
		T object = this.byId(i);
		if (object == null) {
			throw new IllegalArgumentException("No value with id " + i);
		} else {
			return object;
		}
	}

	int size();
}
