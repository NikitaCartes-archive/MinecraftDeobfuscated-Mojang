/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.resources;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;

public class RegistryLoader {
    private final RegistryResourceAccess resources;
    private final Map<ResourceKey<? extends Registry<?>>, ReadCache<?>> readCache = new IdentityHashMap();

    RegistryLoader(RegistryResourceAccess registryResourceAccess) {
        this.resources = registryResourceAccess;
    }

    public <E> DataResult<? extends Registry<E>> overrideRegistryFromResources(WritableRegistry<E> writableRegistry2, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, DynamicOps<JsonElement> dynamicOps) {
        Map map = this.resources.listResources(resourceKey);
        DataResult<WritableRegistry<Object>> dataResult = DataResult.success(writableRegistry2, Lifecycle.stable());
        for (Map.Entry entry : map.entrySet()) {
            dataResult = dataResult.flatMap(writableRegistry -> this.overrideElementFromResources((WritableRegistry)writableRegistry, resourceKey, codec, (ResourceKey)entry.getKey(), Optional.of((RegistryResourceAccess.EntryThunk)entry.getValue()), dynamicOps).map(holder -> writableRegistry));
        }
        return dataResult.setPartial(writableRegistry2);
    }

    <E> DataResult<Holder<E>> overrideElementFromResources(WritableRegistry<E> writableRegistry, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, ResourceKey<E> resourceKey2, DynamicOps<JsonElement> dynamicOps) {
        Optional<RegistryResourceAccess.EntryThunk<E>> optional = this.resources.getResource(resourceKey2);
        return this.overrideElementFromResources(writableRegistry, resourceKey, codec, resourceKey2, optional, dynamicOps);
    }

    private <E> DataResult<Holder<E>> overrideElementFromResources(WritableRegistry<E> writableRegistry, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, ResourceKey<E> resourceKey2, Optional<RegistryResourceAccess.EntryThunk<E>> optional, DynamicOps<JsonElement> dynamicOps) {
        DataResult<Holder<Object>> dataResult2;
        ReadCache<E> readCache = this.readCache(resourceKey);
        DataResult dataResult = readCache.values.get(resourceKey2);
        if (dataResult != null) {
            return dataResult;
        }
        Holder holder = writableRegistry.getOrCreateHolder(resourceKey2);
        readCache.values.put(resourceKey2, DataResult.success(holder));
        if (optional.isEmpty()) {
            dataResult2 = writableRegistry.containsKey(resourceKey2) ? DataResult.success(holder, Lifecycle.stable()) : DataResult.error("Missing referenced custom/removed registry entry for registry " + resourceKey + " named " + resourceKey2.location());
        } else {
            DataResult<RegistryResourceAccess.ParsedEntry<E>> dataResult3 = optional.get().parseElement(dynamicOps, codec);
            Optional<RegistryResourceAccess.ParsedEntry<E>> optional2 = dataResult3.result();
            if (optional2.isPresent()) {
                RegistryResourceAccess.ParsedEntry<E> parsedEntry2 = optional2.get();
                writableRegistry.registerOrOverride(parsedEntry2.fixedId(), resourceKey2, parsedEntry2.value(), dataResult3.lifecycle());
            }
            dataResult2 = dataResult3.map(parsedEntry -> holder);
        }
        readCache.values.put(resourceKey2, dataResult2);
        return dataResult2;
    }

    private <E> ReadCache<E> readCache(ResourceKey<? extends Registry<E>> resourceKey2) {
        return this.readCache.computeIfAbsent(resourceKey2, resourceKey -> new ReadCache());
    }

    public Bound bind(RegistryAccess.Writable writable) {
        return new Bound(writable, this);
    }

    static final class ReadCache<E> {
        final Map<ResourceKey<E>, DataResult<Holder<E>>> values = Maps.newIdentityHashMap();

        ReadCache() {
        }
    }

    public record Bound(RegistryAccess.Writable access, RegistryLoader loader) {
        public <E> DataResult<? extends Registry<E>> overrideRegistryFromResources(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, DynamicOps<JsonElement> dynamicOps) {
            WritableRegistry writableRegistry = this.access.ownedWritableRegistryOrThrow(resourceKey);
            return this.loader.overrideRegistryFromResources(writableRegistry, resourceKey, codec, dynamicOps);
        }

        public <E> DataResult<Holder<E>> overrideElementFromResources(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, ResourceKey<E> resourceKey2, DynamicOps<JsonElement> dynamicOps) {
            WritableRegistry writableRegistry = this.access.ownedWritableRegistryOrThrow(resourceKey);
            return this.loader.overrideElementFromResources(writableRegistry, resourceKey, codec, resourceKey2, dynamicOps);
        }
    }
}

