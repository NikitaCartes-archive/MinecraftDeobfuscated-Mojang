/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.ResourceKey;

public final class RegistryDataPackCodec<E>
implements Codec<MappedRegistry<E>> {
    private final Codec<MappedRegistry<E>> directCodec;
    private final ResourceKey<Registry<E>> registryKey;
    private final Codec<E> elementCodec;

    public static <E> RegistryDataPackCodec<E> create(ResourceKey<Registry<E>> resourceKey, Lifecycle lifecycle, Codec<E> codec) {
        return new RegistryDataPackCodec<E>(resourceKey, lifecycle, codec);
    }

    private RegistryDataPackCodec(ResourceKey<Registry<E>> resourceKey, Lifecycle lifecycle, Codec<E> codec) {
        this.directCodec = MappedRegistry.directCodec(resourceKey, lifecycle, codec);
        this.registryKey = resourceKey;
        this.elementCodec = codec;
    }

    @Override
    public <T> DataResult<T> encode(MappedRegistry<E> mappedRegistry, DynamicOps<T> dynamicOps, T object) {
        return this.directCodec.encode(mappedRegistry, dynamicOps, object);
    }

    @Override
    public <T> DataResult<Pair<MappedRegistry<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
        DataResult<Pair<MappedRegistry<E>, T>> dataResult = this.directCodec.decode(dynamicOps, object);
        if (dynamicOps instanceof RegistryReadOps) {
            return dataResult.flatMap((? super R pair) -> ((RegistryReadOps)dynamicOps).decodeElements((MappedRegistry)pair.getFirst(), this.registryKey, this.elementCodec).map((? super R mappedRegistry) -> Pair.of(mappedRegistry, pair.getSecond())));
        }
        return dataResult;
    }

    public String toString() {
        return "RegistryDapaPackCodec[" + this.directCodec + " " + this.registryKey + " " + this.elementCodec + "]";
    }

    @Override
    public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
        return this.encode((MappedRegistry)object, dynamicOps, object2);
    }
}

