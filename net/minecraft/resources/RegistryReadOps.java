/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.resources;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.DelegatingOps;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class RegistryReadOps<T>
extends DelegatingOps<T> {
    private final RegistryResourceAccess resources;
    private final RegistryAccess registryAccess;
    private final Map<ResourceKey<? extends Registry<?>>, ReadCache<?>> readCache;
    private final RegistryReadOps<JsonElement> jsonOps;

    public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess registryAccess) {
        return RegistryReadOps.createAndLoad(dynamicOps, RegistryResourceAccess.forResourceManager(resourceManager), registryAccess);
    }

    public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> dynamicOps, RegistryResourceAccess registryResourceAccess, RegistryAccess registryAccess) {
        RegistryReadOps<T> registryReadOps = new RegistryReadOps<T>(dynamicOps, registryResourceAccess, registryAccess, Maps.newIdentityHashMap());
        RegistryAccess.load(registryAccess, registryReadOps);
        return registryReadOps;
    }

    public static <T> RegistryReadOps<T> create(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess registryAccess) {
        return RegistryReadOps.create(dynamicOps, RegistryResourceAccess.forResourceManager(resourceManager), registryAccess);
    }

    public static <T> RegistryReadOps<T> create(DynamicOps<T> dynamicOps, RegistryResourceAccess registryResourceAccess, RegistryAccess registryAccess) {
        return new RegistryReadOps<T>(dynamicOps, registryResourceAccess, registryAccess, Maps.newIdentityHashMap());
    }

    private RegistryReadOps(DynamicOps<T> dynamicOps, RegistryResourceAccess registryResourceAccess, RegistryAccess registryAccess, IdentityHashMap<ResourceKey<? extends Registry<?>>, ReadCache<?>> identityHashMap) {
        super(dynamicOps);
        this.resources = registryResourceAccess;
        this.registryAccess = registryAccess;
        this.readCache = identityHashMap;
        this.jsonOps = dynamicOps == JsonOps.INSTANCE ? this : new RegistryReadOps<JsonElement>(JsonOps.INSTANCE, registryResourceAccess, registryAccess, identityHashMap);
    }

    protected <E> DataResult<Pair<Supplier<E>, T>> decodeElement(T object, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, boolean bl) {
        Optional optional = this.registryAccess.ownedRegistry(resourceKey);
        if (!optional.isPresent()) {
            return DataResult.error("Unknown registry: " + resourceKey);
        }
        WritableRegistry writableRegistry = optional.get();
        DataResult dataResult = ResourceLocation.CODEC.decode(this.delegate, object);
        if (!dataResult.result().isPresent()) {
            if (!bl) {
                return DataResult.error("Inline definitions not allowed here");
            }
            return codec.decode(this, object).map(pair -> pair.mapFirst(object -> () -> object));
        }
        Pair pair2 = dataResult.result().get();
        ResourceKey resourceKey2 = ResourceKey.create(resourceKey, (ResourceLocation)pair2.getFirst());
        return this.readAndRegisterElement(resourceKey, writableRegistry, codec, resourceKey2).map(supplier -> Pair.of(supplier, pair2.getSecond()));
    }

    public <E> DataResult<MappedRegistry<E>> decodeElements(MappedRegistry<E> mappedRegistry2, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
        Collection collection = this.resources.listResources(resourceKey);
        DataResult<MappedRegistry<Object>> dataResult = DataResult.success(mappedRegistry2, Lifecycle.stable());
        for (ResourceKey resourceKey2 : collection) {
            dataResult = dataResult.flatMap(mappedRegistry -> this.readAndRegisterElement(resourceKey, (WritableRegistry)mappedRegistry, codec, resourceKey2).map(supplier -> mappedRegistry));
        }
        return dataResult.setPartial(mappedRegistry2);
    }

    private <E> DataResult<Supplier<E>> readAndRegisterElement(ResourceKey<? extends Registry<E>> resourceKey, WritableRegistry<E> writableRegistry, Codec<E> codec, ResourceKey<E> resourceKey2) {
        DataResult<Supplier<E>> dataResult2;
        ReadCache<E> readCache = this.readCache(resourceKey);
        DataResult dataResult = readCache.values.get(resourceKey2);
        if (dataResult != null) {
            return dataResult;
        }
        readCache.values.put(resourceKey2, DataResult.success(RegistryReadOps.createPlaceholderGetter(writableRegistry, resourceKey2)));
        Optional<DataResult<RegistryResourceAccess.ParsedEntry<E>>> optional = this.resources.parseElement(this.jsonOps, resourceKey, resourceKey2, codec);
        if (optional.isEmpty()) {
            dataResult2 = DataResult.success(RegistryReadOps.createRegistryGetter(writableRegistry, resourceKey2), Lifecycle.stable());
        } else {
            DataResult<RegistryResourceAccess.ParsedEntry<E>> dataResult3 = optional.get();
            Optional<RegistryResourceAccess.ParsedEntry<E>> optional2 = dataResult3.result();
            if (optional2.isPresent()) {
                RegistryResourceAccess.ParsedEntry<E> parsedEntry2 = optional2.get();
                writableRegistry.registerOrOverride(parsedEntry2.fixedId(), resourceKey2, parsedEntry2.value(), dataResult3.lifecycle());
            }
            dataResult2 = dataResult3.map(parsedEntry -> RegistryReadOps.createRegistryGetter(writableRegistry, resourceKey2));
        }
        readCache.values.put(resourceKey2, dataResult2);
        return dataResult2;
    }

    private static <E> Supplier<E> createPlaceholderGetter(WritableRegistry<E> writableRegistry, ResourceKey<E> resourceKey) {
        return Suppliers.memoize(() -> {
            Object object = writableRegistry.get(resourceKey);
            if (object == null) {
                throw new RuntimeException("Error during recursive registry parsing, element resolved too early: " + resourceKey);
            }
            return object;
        });
    }

    private static <E> Supplier<E> createRegistryGetter(final Registry<E> registry, final ResourceKey<E> resourceKey) {
        return new Supplier<E>(){

            @Override
            public E get() {
                return registry.get(resourceKey);
            }

            public String toString() {
                return resourceKey.toString();
            }
        };
    }

    private <E> ReadCache<E> readCache(ResourceKey<? extends Registry<E>> resourceKey2) {
        return this.readCache.computeIfAbsent(resourceKey2, resourceKey -> new ReadCache());
    }

    protected <E> DataResult<Registry<E>> registry(ResourceKey<? extends Registry<E>> resourceKey) {
        return this.registryAccess.ownedRegistry(resourceKey).map(writableRegistry -> DataResult.success(writableRegistry, writableRegistry.elementsLifecycle())).orElseGet(() -> DataResult.error("Unknown registry: " + resourceKey));
    }

    static final class ReadCache<E> {
        final Map<ResourceKey<E>, DataResult<Supplier<E>>> values = Maps.newIdentityHashMap();

        ReadCache() {
        }
    }
}

