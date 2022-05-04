/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.syncher;

import java.util.Optional;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;

public interface EntityDataSerializer<T> {
    public void write(FriendlyByteBuf var1, T var2);

    public T read(FriendlyByteBuf var1);

    default public EntityDataAccessor<T> createAccessor(int i) {
        return new EntityDataAccessor(i, this);
    }

    public T copy(T var1);

    public static <T> EntityDataSerializer<T> simple(final FriendlyByteBuf.Writer<T> writer, final FriendlyByteBuf.Reader<T> reader) {
        return new ForValueType<T>(){

            @Override
            public void write(FriendlyByteBuf friendlyByteBuf, T object) {
                writer.accept(friendlyByteBuf, object);
            }

            @Override
            public T read(FriendlyByteBuf friendlyByteBuf) {
                return reader.apply(friendlyByteBuf);
            }
        };
    }

    public static <T> EntityDataSerializer<Optional<T>> optional(FriendlyByteBuf.Writer<T> writer, FriendlyByteBuf.Reader<T> reader) {
        return EntityDataSerializer.simple(writer.asOptional(), reader.asOptional());
    }

    public static <T extends Enum<T>> EntityDataSerializer<T> simpleEnum(Class<T> class_) {
        return EntityDataSerializer.simple(FriendlyByteBuf::writeEnum, friendlyByteBuf -> friendlyByteBuf.readEnum(class_));
    }

    public static <T> EntityDataSerializer<T> simpleId(IdMap<T> idMap) {
        return EntityDataSerializer.simple((friendlyByteBuf, object) -> friendlyByteBuf.writeId(idMap, object), friendlyByteBuf -> friendlyByteBuf.readById(idMap));
    }

    public static interface ForValueType<T>
    extends EntityDataSerializer<T> {
        @Override
        default public T copy(T object) {
            return object;
        }
    }
}

