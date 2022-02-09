/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.resources;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.DelegatingOps;
import net.minecraft.resources.RegistryLoader;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T>
extends DelegatingOps<T> {
    private final Optional<RegistryLoader.Bound> loader;
    private final RegistryAccess registryAccess;
    private final DynamicOps<JsonElement> asJson;

    public static <T> RegistryOps<T> create(DynamicOps<T> dynamicOps, RegistryAccess registryAccess) {
        return new RegistryOps<T>(dynamicOps, registryAccess, Optional.empty());
    }

    public static <T> RegistryOps<T> createAndLoad(DynamicOps<T> dynamicOps, RegistryAccess.Writable writable, ResourceManager resourceManager) {
        return RegistryOps.createAndLoad(dynamicOps, writable, RegistryResourceAccess.forResourceManager(resourceManager));
    }

    public static <T> RegistryOps<T> createAndLoad(DynamicOps<T> dynamicOps, RegistryAccess.Writable writable, RegistryResourceAccess registryResourceAccess) {
        RegistryLoader registryLoader = new RegistryLoader(registryResourceAccess);
        RegistryOps<T> registryOps = new RegistryOps<T>(dynamicOps, writable, Optional.of(registryLoader.bind(writable)));
        RegistryAccess.load(writable, registryOps.getAsJson(), registryLoader);
        return registryOps;
    }

    private RegistryOps(DynamicOps<T> dynamicOps, RegistryAccess registryAccess, Optional<RegistryLoader.Bound> optional) {
        super(dynamicOps);
        this.loader = optional;
        this.registryAccess = registryAccess;
        this.asJson = dynamicOps == JsonOps.INSTANCE ? this : new RegistryOps<JsonElement>(JsonOps.INSTANCE, registryAccess, optional);
    }

    public <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return this.registryAccess.registry(resourceKey);
    }

    public Optional<RegistryLoader.Bound> registryLoader() {
        return this.loader;
    }

    public DynamicOps<JsonElement> getAsJson() {
        return this.asJson;
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

