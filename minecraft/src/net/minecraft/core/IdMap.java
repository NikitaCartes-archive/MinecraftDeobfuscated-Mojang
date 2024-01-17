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

	default int getIdOrThrow(T object) {
		int i = this.getId(object);
		if (i == -1) {
			throw new IllegalArgumentException("Can't find id for '" + object + "' in map " + this);
		} else {
			return i;
		}
	}

	int size();
}
