/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class ExtraCodecs {
    public static final Codec<DoubleStream> DOUBLE_STREAM = new PrimitiveCodec<DoubleStream>(){

        @Override
        public <T> DataResult<DoubleStream> read(DynamicOps<T> dynamicOps, T object) {
            return ExtraCodecs.asDoubleStream(dynamicOps, object);
        }

        @Override
        public <T> T write(DynamicOps<T> dynamicOps, DoubleStream doubleStream) {
            return ExtraCodecs.createDoubleList(dynamicOps, doubleStream);
        }

        public String toString() {
            return "DoubleStream";
        }

        @Override
        public /* synthetic */ Object write(DynamicOps dynamicOps, Object object) {
            return this.write(dynamicOps, (DoubleStream)object);
        }
    };

    public static <T> DataResult<DoubleStream> asDoubleStream(DynamicOps<T> dynamicOps, T object) {
        return dynamicOps.getStream(object).flatMap(stream -> {
            List list = stream.collect(Collectors.toList());
            if (list.stream().allMatch(object -> dynamicOps.getNumberValue(object).result().isPresent())) {
                return DataResult.success(list.stream().mapToDouble(object -> dynamicOps.getNumberValue(object).result().get().doubleValue()));
            }
            return DataResult.error("Some elements are not doubles: " + object);
        });
    }

    public static <T> T createDoubleList(DynamicOps<T> dynamicOps, DoubleStream doubleStream) {
        return (T)dynamicOps.createList(doubleStream.mapToObj(dynamicOps::createDouble));
    }
}

