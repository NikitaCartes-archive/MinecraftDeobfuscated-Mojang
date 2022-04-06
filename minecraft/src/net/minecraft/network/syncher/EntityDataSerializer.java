package net.minecraft.network.syncher;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public interface EntityDataSerializer<T> {
	void write(FriendlyByteBuf friendlyByteBuf, T object);

	T read(FriendlyByteBuf friendlyByteBuf);

	default EntityDataAccessor<T> createAccessor(int i) {
		return new EntityDataAccessor<>(i, this);
	}

	T copy(T object);

	static <T> EntityDataSerializer<T> simple(BiConsumer<FriendlyByteBuf, T> biConsumer, Function<FriendlyByteBuf, T> function) {
		return new EntityDataSerializer.ForValueType<T>() {
			@Override
			public void write(FriendlyByteBuf friendlyByteBuf, T object) {
				biConsumer.accept(friendlyByteBuf, object);
			}

			@Override
			public T read(FriendlyByteBuf friendlyByteBuf) {
				return (T)function.apply(friendlyByteBuf);
			}
		};
	}

	static <T> EntityDataSerializer<Optional<T>> optional(BiConsumer<FriendlyByteBuf, T> biConsumer, Function<FriendlyByteBuf, T> function) {
		return new EntityDataSerializer.ForValueType<Optional<T>>() {
			public void write(FriendlyByteBuf friendlyByteBuf, Optional<T> optional) {
				if (optional.isPresent()) {
					friendlyByteBuf.writeBoolean(true);
					biConsumer.accept(friendlyByteBuf, optional.get());
				} else {
					friendlyByteBuf.writeBoolean(false);
				}
			}

			public Optional<T> read(FriendlyByteBuf friendlyByteBuf) {
				return friendlyByteBuf.readBoolean() ? Optional.of(function.apply(friendlyByteBuf)) : Optional.empty();
			}
		};
	}

	static <T extends Enum<T>> EntityDataSerializer<T> simpleEnum(Class<T> class_) {
		return simple(FriendlyByteBuf::writeEnum, friendlyByteBuf -> friendlyByteBuf.readEnum(class_));
	}

	static <T> EntityDataSerializer<T> simpleId(IdMap<T> idMap) {
		return simple((friendlyByteBuf, object) -> friendlyByteBuf.writeId(idMap, (T)object), friendlyByteBuf -> friendlyByteBuf.readById(idMap));
	}

	public interface ForValueType<T> extends EntityDataSerializer<T> {
		@Override
		default T copy(T object) {
			return object;
		}
	}
}
