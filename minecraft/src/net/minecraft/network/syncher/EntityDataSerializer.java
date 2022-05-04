package net.minecraft.network.syncher;

import java.util.Optional;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;

public interface EntityDataSerializer<T> {
	void write(FriendlyByteBuf friendlyByteBuf, T object);

	T read(FriendlyByteBuf friendlyByteBuf);

	default EntityDataAccessor<T> createAccessor(int i) {
		return new EntityDataAccessor<>(i, this);
	}

	T copy(T object);

	static <T> EntityDataSerializer<T> simple(FriendlyByteBuf.Writer<T> writer, FriendlyByteBuf.Reader<T> reader) {
		return new EntityDataSerializer.ForValueType<T>() {
			@Override
			public void write(FriendlyByteBuf friendlyByteBuf, T object) {
				writer.accept(friendlyByteBuf, object);
			}

			@Override
			public T read(FriendlyByteBuf friendlyByteBuf) {
				return (T)reader.apply(friendlyByteBuf);
			}
		};
	}

	static <T> EntityDataSerializer<Optional<T>> optional(FriendlyByteBuf.Writer<T> writer, FriendlyByteBuf.Reader<T> reader) {
		return simple(writer.asOptional(), reader.asOptional());
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
