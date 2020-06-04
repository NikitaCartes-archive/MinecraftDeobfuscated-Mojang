package net.minecraft.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.RegistryDataPackCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MappedRegistry<T> extends WritableRegistry<T> {
	protected static final Logger LOGGER = LogManager.getLogger();
	protected final CrudeIncrementalIntIdentityHashBiMap<T> map = new CrudeIncrementalIntIdentityHashBiMap<>(256);
	protected final BiMap<ResourceLocation, T> storage = HashBiMap.create();
	private final BiMap<ResourceKey<T>, T> keyStorage = HashBiMap.create();
	private final Set<ResourceKey<T>> persistent = Sets.newIdentityHashSet();
	protected Object[] randomCache;
	private int nextId;

	public MappedRegistry(ResourceKey<Registry<T>> resourceKey, Lifecycle lifecycle) {
		super(resourceKey, lifecycle);
	}

	@Override
	public <V extends T> V registerMapping(int i, ResourceKey<T> resourceKey, V object) {
		this.map.addMapping((T)object, i);
		Validate.notNull(resourceKey);
		Validate.notNull(object);
		this.randomCache = null;
		if (this.keyStorage.containsKey(resourceKey)) {
			LOGGER.debug("Adding duplicate key '{}' to registry", resourceKey);
		}

		this.storage.put(resourceKey.location(), (T)object);
		this.keyStorage.put(resourceKey, (T)object);
		if (this.nextId <= i) {
			this.nextId = i + 1;
		}

		return object;
	}

	@Override
	public <V extends T> V register(ResourceKey<T> resourceKey, V object) {
		return this.registerMapping(this.nextId, resourceKey, object);
	}

	@Nullable
	@Override
	public ResourceLocation getKey(T object) {
		return (ResourceLocation)this.storage.inverse().get(object);
	}

	@Override
	public Optional<ResourceKey<T>> getResourceKey(T object) {
		return Optional.ofNullable(this.keyStorage.inverse().get(object));
	}

	@Override
	public int getId(@Nullable T object) {
		return this.map.getId(object);
	}

	@Nullable
	@Override
	public T get(@Nullable ResourceKey<T> resourceKey) {
		return (T)this.keyStorage.get(resourceKey);
	}

	@Nullable
	@Override
	public T byId(int i) {
		return this.map.byId(i);
	}

	public Iterator<T> iterator() {
		return this.map.iterator();
	}

	@Nullable
	@Override
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

	public Set<Entry<ResourceKey<T>, T>> entrySet() {
		return Collections.unmodifiableMap(this.keyStorage).entrySet();
	}

	@Nullable
	public T getRandom(Random random) {
		if (this.randomCache == null) {
			Collection<?> collection = this.storage.values();
			if (collection.isEmpty()) {
				return null;
			}

			this.randomCache = collection.toArray(new Object[collection.size()]);
		}

		return Util.getRandom((T[])this.randomCache, random);
	}

	@Override
	public boolean containsKey(ResourceLocation resourceLocation) {
		return this.storage.containsKey(resourceLocation);
	}

	@Override
	public boolean containsId(int i) {
		return this.map.contains(i);
	}

	public boolean persistent(ResourceKey<T> resourceKey) {
		return this.persistent.contains(resourceKey);
	}

	public void setPersistent(ResourceKey<T> resourceKey) {
		this.persistent.add(resourceKey);
	}

	public static <T> Codec<MappedRegistry<T>> networkCodec(ResourceKey<Registry<T>> resourceKey, Lifecycle lifecycle, Codec<T> codec) {
		return Codec.mapPair(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(resourceKey), ResourceKey::location).fieldOf("key"), codec.fieldOf("element"))
			.codec()
			.listOf()
			.xmap(list -> {
				MappedRegistry<T> mappedRegistry = new MappedRegistry<>(resourceKey, lifecycle);

				for (Pair<ResourceKey<T>, T> pair : list) {
					mappedRegistry.register(pair.getFirst(), pair.getSecond());
				}

				return mappedRegistry;
			}, mappedRegistry -> {
				com.google.common.collect.ImmutableList.Builder<Pair<ResourceKey<T>, T>> builder = ImmutableList.builder();

				for (T object : mappedRegistry.map) {
					builder.add(Pair.of((ResourceKey<T>)mappedRegistry.getResourceKey(object).get(), object));
				}

				return builder.build();
			});
	}

	public static <T> Codec<MappedRegistry<T>> dataPackCodec(ResourceKey<Registry<T>> resourceKey, Lifecycle lifecycle, Codec<T> codec) {
		return RegistryDataPackCodec.create(resourceKey, lifecycle, codec);
	}

	public static <T> Codec<MappedRegistry<T>> directCodec(ResourceKey<Registry<T>> resourceKey, Lifecycle lifecycle, Codec<T> codec) {
		return Codec.unboundedMap(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(resourceKey), ResourceKey::location), codec).xmap(map -> {
			MappedRegistry<T> mappedRegistry = new MappedRegistry<>(resourceKey, lifecycle);
			map.forEach((resourceKeyxx, object) -> {
				mappedRegistry.registerMapping(mappedRegistry.nextId, resourceKeyxx, object);
				mappedRegistry.setPersistent(resourceKeyxx);
			});
			return mappedRegistry;
		}, mappedRegistry -> {
			Builder<ResourceKey<T>, T> builder = ImmutableMap.builder();
			mappedRegistry.keyStorage.entrySet().stream().filter(entry -> mappedRegistry.persistent((ResourceKey<T>)entry.getKey())).forEach(builder::put);
			return builder.build();
		});
	}
}
