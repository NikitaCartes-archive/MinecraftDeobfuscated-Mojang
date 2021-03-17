/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.DelegatingOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class RegistryWriteOps<T>
extends DelegatingOps<T> {
    private final RegistryAccess registryAccess;

    public static <T> RegistryWriteOps<T> create(DynamicOps<T> dynamicOps, RegistryAccess registryAccess) {
        return new RegistryWriteOps<T>(dynamicOps, registryAccess);
    }

    private RegistryWriteOps(DynamicOps<T> dynamicOps, RegistryAccess registryAccess) {
        super(dynamicOps);
        this.registryAccess = registryAccess;
    }

    protected <E> DataResult<T> encode(E object, T object2, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
        WritableRegistry writableRegistry;
        Optional<ResourceKey<E>> optional2;
        Optional optional = this.registryAccess.ownedRegistry(resourceKey);
        if (optional.isPresent() && (optional2 = (writableRegistry = optional.get()).getResourceKey(object)).isPresent()) {
            ResourceKey<E> resourceKey2 = optional2.get();
            return ResourceLocation.CODEC.encode(resourceKey2.location(), this.delegate, object2);
        }
        return codec.encode(object, this, object2);
    }
}

