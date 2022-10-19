/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.DelegatingOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T>
extends DelegatingOps<T> {
    private final RegistryAccess registryAccess;

    public static <T> RegistryOps<T> create(DynamicOps<T> dynamicOps, RegistryAccess registryAccess) {
        return new RegistryOps<T>(dynamicOps, registryAccess);
    }

    private RegistryOps(DynamicOps<T> dynamicOps, RegistryAccess registryAccess) {
        super(dynamicOps);
        this.registryAccess = registryAccess;
    }

    public <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return this.registryAccess.registry(resourceKey);
    }

    public static <E> MapCodec<Registry<E>> retrieveRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return ExtraCodecs.retrieveContext(dynamicOps -> {
            if (dynamicOps instanceof RegistryOps) {
                RegistryOps registryOps = (RegistryOps)dynamicOps;
                return registryOps.registry(resourceKey).map(registry -> DataResult.success(registry, registry.elementsLifecycle())).orElseGet(() -> DataResult.error("Unknown registry: " + resourceKey));
            }
            return DataResult.error("Not a registry ops");
        });
    }
}

