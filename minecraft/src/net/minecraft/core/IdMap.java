package net.minecraft.core;

import javax.annotation.Nullable;

public interface IdMap<T> extends Iterable<T> {
	int getId(T object);

	@Nullable
	T byId(int i);
}
