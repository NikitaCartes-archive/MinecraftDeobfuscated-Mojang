package net.minecraft.core;

import javax.annotation.Nullable;

public interface IdMap<T> extends Iterable<T> {
	int DEFAULT = -1;

	int getId(T object);

	@Nullable
	T byId(int i);

	int size();
}
