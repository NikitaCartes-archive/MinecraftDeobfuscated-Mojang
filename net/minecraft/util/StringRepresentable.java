/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

public interface StringRepresentable {
    public static final int PRE_BUILT_MAP_THRESHOLD = 16;

    public String getSerializedName();

    public static <E extends Enum<E>> EnumCodec<E> fromEnum(Supplier<E[]> supplier) {
        Enum[] enums = (Enum[])supplier.get();
        if (enums.length > 16) {
            Map<String, Enum> map = Arrays.stream(enums).collect(Collectors.toMap(object -> ((StringRepresentable)object).getSerializedName(), enum_ -> enum_));
            return new EnumCodec(enums, string -> string == null ? null : (Enum)map.get(string));
        }
        return new EnumCodec(enums, string -> {
            for (Enum enum_ : enums) {
                if (!((StringRepresentable)((Object)enum_)).getSerializedName().equals(string)) continue;
                return enum_;
            }
            return null;
        });
    }

    public static Keyable keys(final StringRepresentable[] stringRepresentables) {
        return new Keyable(){

            @Override
            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return Arrays.stream(stringRepresentables).map(StringRepresentable::getSerializedName).map(dynamicOps::createString);
            }
        };
    }

    @Deprecated
    public static class EnumCodec<E extends Enum<E>>
    implements Codec<E> {
        private Codec<E> codec;
        private Function<String, E> resolver;

        public EnumCodec(E[] enums, Function<String, E> function) {
            this.codec = ExtraCodecs.orCompressed(ExtraCodecs.stringResolverCodec(object -> ((StringRepresentable)object).getSerializedName(), function), ExtraCodecs.idResolverCodec(object -> ((Enum)object).ordinal(), i -> i >= 0 && i < enums.length ? enums[i] : null, -1));
            this.resolver = function;
        }

        @Override
        public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> dynamicOps, T object) {
            return this.codec.decode(dynamicOps, object);
        }

        @Override
        public <T> DataResult<T> encode(E enum_, DynamicOps<T> dynamicOps, T object) {
            return this.codec.encode(enum_, dynamicOps, object);
        }

        @Nullable
        public E byName(@Nullable String string) {
            return (E)((Enum)this.resolver.apply(string));
        }

        @Override
        public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
            return this.encode((E)((Enum)object), (DynamicOps<T>)dynamicOps, (T)object2);
        }
    }
}

