/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;

public interface StringRepresentable {
    public String getSerializedName();

    public static <E extends Enum<E>> Codec<E> fromEnum(Supplier<E[]> supplier, Function<String, E> function) {
        Enum[] enums = (Enum[])supplier.get();
        return ExtraCodecs.orCompressed(ExtraCodecs.stringResolverCodec(object -> ((StringRepresentable)object).getSerializedName(), function), ExtraCodecs.idResolverCodec(object -> ((Enum)object).ordinal(), i -> i >= 0 && i < enums.length ? enums[i] : null, -1));
    }

    public static Keyable keys(final StringRepresentable[] stringRepresentables) {
        return new Keyable(){

            @Override
            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return Arrays.stream(stringRepresentables).map(StringRepresentable::getSerializedName).map(dynamicOps::createString);
            }
        };
    }
}

