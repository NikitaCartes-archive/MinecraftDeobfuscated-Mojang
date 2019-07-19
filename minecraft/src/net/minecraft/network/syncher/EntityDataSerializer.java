package net.minecraft.network.syncher;

import net.minecraft.network.FriendlyByteBuf;

public interface EntityDataSerializer<T> {
	void write(FriendlyByteBuf friendlyByteBuf, T object);

	T read(FriendlyByteBuf friendlyByteBuf);

	default EntityDataAccessor<T> createAccessor(int i) {
		return new EntityDataAccessor<>(i, this);
	}

	T copy(T object);
}
