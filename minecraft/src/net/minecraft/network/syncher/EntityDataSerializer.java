package net.minecraft.network.syncher;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface EntityDataSerializer<T> {
	StreamCodec<? super RegistryFriendlyByteBuf, T> codec();

	default EntityDataAccessor<T> createAccessor(int i) {
		return new EntityDataAccessor<>(i, this);
	}

	T copy(T object);

	static <T> EntityDataSerializer<T> forValueType(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return () -> streamCodec;
	}

	public interface ForValueType<T> extends EntityDataSerializer<T> {
		@Override
		default T copy(T object) {
			return object;
		}
	}
}
