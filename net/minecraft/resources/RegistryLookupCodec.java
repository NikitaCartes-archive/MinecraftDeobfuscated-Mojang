/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.ResourceKey;

public final class RegistryLookupCodec<E>
extends MapCodec<Registry<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;

    public static <E> RegistryLookupCodec<E> create(ResourceKey<? extends Registry<E>> resourceKey) {
        return new RegistryLookupCodec<E>(resourceKey);
    }

    private RegistryLookupCodec(ResourceKey<? extends Registry<E>> resourceKey) {
        this.registryKey = resourceKey;
    }

    @Override
    public <T> RecordBuilder<T> encode(Registry<E> registry, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
        return recordBuilder;
    }

    @Override
    public <T> DataResult<Registry<E>> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
        if (dynamicOps instanceof RegistryReadOps) {
            return ((RegistryReadOps)dynamicOps).registry(this.registryKey);
        }
        return DataResult.error("Not a registry ops");
    }

    public String toString() {
        return "RegistryLookupCodec[" + this.registryKey + "]";
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
        return Stream.empty();
    }

    @Override
    public /* synthetic */ RecordBuilder encode(Object object, DynamicOps dynamicOps, RecordBuilder recordBuilder) {
        return this.encode((Registry)object, dynamicOps, recordBuilder);
    }
}

