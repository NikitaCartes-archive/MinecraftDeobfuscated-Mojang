/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class MappedRegistry<T>
extends WritableRegistry<T> {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected final CrudeIncrementalIntIdentityHashBiMap<T> map = new CrudeIncrementalIntIdentityHashBiMap(256);
    protected final BiMap<ResourceLocation, T> storage = HashBiMap.create();
    protected final BiMap<ResourceKey<T>, T> keyStorage = HashBiMap.create();
    protected Object[] randomCache;
    private int nextId;

    public MappedRegistry(ResourceKey<Registry<T>> resourceKey, Lifecycle lifecycle) {
        super(resourceKey, lifecycle);
    }

    @Override
    public <V extends T> V registerMapping(int i, ResourceKey<T> resourceKey, V object) {
        this.map.addMapping(object, i);
        Validate.notNull(resourceKey);
        Validate.notNull(object);
        this.randomCache = null;
        if (this.keyStorage.containsKey(resourceKey)) {
            LOGGER.debug("Adding duplicate key '{}' to registry", (Object)resourceKey);
        }
        this.storage.put(resourceKey.location(), object);
        this.keyStorage.put(resourceKey, object);
        if (this.nextId <= i) {
            this.nextId = i + 1;
        }
        return object;
    }

    @Override
    public <V extends T> V register(ResourceKey<T> resourceKey, V object) {
        return this.registerMapping(this.nextId, resourceKey, object);
    }

    @Override
    @Nullable
    public ResourceLocation getKey(T object) {
        return (ResourceLocation)this.storage.inverse().get(object);
    }

    @Override
    public ResourceKey<T> getResourceKey(T object) {
        ResourceKey resourceKey = (ResourceKey)this.keyStorage.inverse().get(object);
        if (resourceKey == null) {
            throw new IllegalStateException("Unregistered registry element: " + object + " in " + this);
        }
        return resourceKey;
    }

    @Override
    public int getId(@Nullable T object) {
        return this.map.getId(object);
    }

    @Override
    @Nullable
    public T get(@Nullable ResourceKey<T> resourceKey) {
        return (T)this.keyStorage.get(resourceKey);
    }

    @Override
    @Nullable
    public T byId(int i) {
        return this.map.byId(i);
    }

    @Override
    public Iterator<T> iterator() {
        return this.map.iterator();
    }

    @Override
    @Nullable
    public T get(@Nullable ResourceLocation resourceLocation) {
        return (T)this.storage.get(resourceLocation);
    }

    @Override
    public Optional<T> getOptional(@Nullable ResourceLocation resourceLocation) {
        return Optional.ofNullable(this.storage.get(resourceLocation));
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return Collections.unmodifiableSet(this.storage.keySet());
    }

    @Nullable
    public T getRandom(Random random) {
        if (this.randomCache == null) {
            Collection collection = this.storage.values();
            if (collection.isEmpty()) {
                return null;
            }
            this.randomCache = collection.toArray(new Object[collection.size()]);
        }
        return (T)Util.getRandom(this.randomCache, random);
    }

    @Override
    public boolean containsKey(ResourceLocation resourceLocation) {
        return this.storage.containsKey(resourceLocation);
    }

    @Override
    public boolean containsKey(ResourceKey<T> resourceKey) {
        return this.keyStorage.containsKey(resourceKey);
    }

    @Override
    public boolean containsId(int i) {
        return this.map.contains(i);
    }

    public static <T> Codec<MappedRegistry<T>> codec(ResourceKey<Registry<T>> resourceKey, Lifecycle lifecycle, Codec<T> codec) {
        return Codec.mapPair(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(resourceKey), ResourceKey::location).fieldOf("key"), codec.fieldOf("element")).codec().listOf().xmap(list -> {
            MappedRegistry mappedRegistry = new MappedRegistry(resourceKey, lifecycle);
            for (Pair pair : list) {
                mappedRegistry.register((ResourceKey)pair.getFirst(), pair.getSecond());
            }
            return mappedRegistry;
        }, mappedRegistry -> {
            ImmutableList.Builder builder = ImmutableList.builder();
            for (Object object : mappedRegistry) {
                builder.add(Pair.of(mappedRegistry.getResourceKey(object), object));
            }
            return builder.build();
        });
    }
}

