/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.resources.ResourceKey;

public final class RegistryFileCodec<E>
implements Codec<Supplier<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;
    private final Codec<E> elementCodec;

    public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
        return new RegistryFileCodec<E>(resourceKey, codec);
    }

    private RegistryFileCodec(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
        this.registryKey = resourceKey;
        this.elementCodec = codec;
    }

    @Override
    public <T> DataResult<T> encode(Supplier<E> supplier, DynamicOps<T> dynamicOps, T object) {
        if (dynamicOps instanceof RegistryWriteOps) {
            return ((RegistryWriteOps)dynamicOps).encode(supplier.get(), object, this.registryKey, this.elementCodec);
        }
        return this.elementCodec.encode(supplier.get(), dynamicOps, object);
    }

    @Override
    public <T> DataResult<Pair<Supplier<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
        if (dynamicOps instanceof RegistryReadOps) {
            return ((RegistryReadOps)dynamicOps).decodeElement(object, this.registryKey, this.elementCodec);
        }
        return this.elementCodec.decode(dynamicOps, object).map((? super R pair) -> pair.mapFirst(object -> () -> object));
    }

    public String toString() {
        return "RegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
    }

    @Override
    public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
        return this.encode((Supplier)object, dynamicOps, object2);
    }
}

