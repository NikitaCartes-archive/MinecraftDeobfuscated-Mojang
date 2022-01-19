package net.minecraft.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.RegistryDataPackCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class MappedRegistry<T> extends WritableRegistry<T> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ObjectList<T> byId = new ObjectArrayList<>(256);
	private final Object2IntMap<T> toId = Util.make(
		new Object2IntOpenCustomHashMap<>(Util.identityStrategy()), object2IntOpenCustomHashMap -> object2IntOpenCustomHashMap.defaultReturnValue(-1)
	);
	private final BiMap<ResourceLocation, T> storage = HashBiMap.create();
	private final BiMap<ResourceKey<T>, T> keyStorage = HashBiMap.create();
	private final Map<T, Lifecycle> lifecycles = Maps.<T, Lifecycle>newIdentityHashMap();
	private Lifecycle elementsLifecycle;
	@Nullable
	protected Object[] randomCache;
	private int nextId;

	public MappedRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle) {
		super(resourceKey, lifecycle);
		this.elementsLifecycle = lifecycle;
	}

	public static <T> MapCodec<MappedRegistry.RegistryEntry<T>> withNameAndId(ResourceKey<? extends Registry<T>> resourceKey, MapCodec<T> mapCodec) {
		return RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						ResourceLocation.CODEC.xmap(ResourceKey.elementKey(resourceKey), ResourceKey::location).fieldOf("name").forGetter(MappedRegistry.RegistryEntry::key),
						Codec.INT.fieldOf("id").forGetter(MappedRegistry.RegistryEntry::id),
						mapCodec.forGetter(MappedRegistry.RegistryEntry::value)
					)
					.apply(instance, MappedRegistry.RegistryEntry::new)
		);
	}

	@Override
	public <V extends T> V registerMapping(int i, ResourceKey<T> resourceKey, V object, Lifecycle lifecycle) {
		return this.registerMapping(i, resourceKey, object, lifecycle, true);
	}

	private <V extends T> V registerMapping(int i, ResourceKey<T> resourceKey, V object, Lifecycle lifecycle, boolean bl) {
		Validate.notNull(resourceKey);
		Validate.notNull(object);
		this.byId.size(Math.max(this.byId.size(), i + 1));
		this.byId.set(i, object);
		this.toId.put((T)object, i);
		this.randomCache = null;
		if (bl && this.keyStorage.containsKey(resourceKey)) {
			Util.logAndPauseIfInIde("Adding duplicate key '" + resourceKey + "' to registry");
		}

		if (this.storage.containsValue(object)) {
			Util.logAndPauseIfInIde("Adding duplicate value '" + object + "' to registry");
		}

		this.storage.put(resourceKey.location(), (T)object);
		this.keyStorage.put(resourceKey, (T)object);
		this.lifecycles.put(object, lifecycle);
		this.elementsLifecycle = this.elementsLifecycle.add(lifecycle);
		if (this.nextId <= i) {
			this.nextId = i + 1;
		}

		return object;
	}

	@Override
	public <V extends T> V register(ResourceKey<T> resourceKey, V object, Lifecycle lifecycle) {
		return this.registerMapping(this.nextId, resourceKey, object, lifecycle);
	}

	@Override
	public <V extends T> V registerOrOverride(OptionalInt optionalInt, ResourceKey<T> resourceKey, V object, Lifecycle lifecycle) {
		Validate.notNull(resourceKey);
		Validate.notNull(object);
		T object2 = (T)this.keyStorage.get(resourceKey);
		int i;
		if (object2 == null) {
			i = optionalInt.isPresent() ? optionalInt.getAsInt() : this.nextId;
		} else {
			i = this.toId.getInt(object2);
			if (optionalInt.isPresent() && optionalInt.getAsInt() != i) {
				throw new IllegalStateException("ID mismatch");
			}

			this.toId.removeInt(object2);
			this.lifecycles.remove(object2);
		}

		return this.registerMapping(i, resourceKey, object, lifecycle, false);
	}

	@Nullable
	@Override
	public ResourceLocation getKey(T object) {
		return (ResourceLocation)this.storage.inverse().get(object);
	}

	@Override
	public Optional<ResourceKey<T>> getResourceKey(T object) {
		return Optional.ofNullable((ResourceKey)this.keyStorage.inverse().get(object));
	}

	@Override
	public int getId(@Nullable T object) {
		return this.toId.getInt(object);
	}

	@Nullable
	@Override
	public T get(@Nullable ResourceKey<T> resourceKey) {
		return (T)this.keyStorage.get(resourceKey);
	}

	@Nullable
	@Override
	public T byId(int i) {
		return (T)(i >= 0 && i < this.byId.size() ? this.byId.get(i) : null);
	}

	@Override
	public int size() {
		return this.storage.size();
	}

	@Override
	public Lifecycle lifecycle(T object) {
		return (Lifecycle)this.lifecycles.get(object);
	}

	@Override
	public Lifecycle elementsLifecycle() {
		return this.elementsLifecycle;
	}

	public Iterator<T> iterator() {
		return Iterators.filter(this.byId.iterator(), Objects::nonNull);
	}

	@Nullable
	@Override
	public T get(@Nullable ResourceLocation resourceLocation) {
		return (T)this.storage.get(resourceLocation);
	}

	@Override
	public Set<ResourceLocation> keySet() {
		return Collections.unmodifiableSet(this.storage.keySet());
	}

	@Override
	public Set<Entry<ResourceKey<T>, T>> entrySet() {
		return Collections.unmodifiableMap(this.keyStorage).entrySet();
	}

	@Override
	public boolean isEmpty() {
		return this.storage.isEmpty();
	}

	@Nullable
	@Override
	public T getRandom(Random random) {
		if (this.randomCache == null) {
			Collection<?> collection = this.storage.values();
			if (collection.isEmpty()) {
				return null;
			}

			this.randomCache = collection.toArray(Object[]::new);
		}

		return Util.getRandom((T[])this.randomCache, random);
	}

	@Override
	public boolean containsKey(ResourceLocation resourceLocation) {
		return this.storage.containsKey(resourceLocation);
	}

	@Override
	public boolean containsKey(ResourceKey<T> resourceKey) {
		return this.keyStorage.containsKey(resourceKey);
	}

	public static <T> Codec<MappedRegistry<T>> networkCodec(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, Codec<T> codec) {
		return withNameAndId(resourceKey, codec.fieldOf("element")).codec().listOf().xmap(list -> {
			MappedRegistry<T> mappedRegistry = new MappedRegistry<>(resourceKey, lifecycle);

			for (MappedRegistry.RegistryEntry<T> registryEntry : list) {
				mappedRegistry.registerMapping(registryEntry.id(), registryEntry.key(), registryEntry.value(), lifecycle);
			}

			return mappedRegistry;
		}, mappedRegistry -> {
			Builder<MappedRegistry.RegistryEntry<T>> builder = ImmutableList.builder();

			for (T object : mappedRegistry) {
				builder.add(new MappedRegistry.RegistryEntry<>((ResourceKey<T>)mappedRegistry.getResourceKey(object).get(), mappedRegistry.getId(object), object));
			}

			return builder.build();
		});
	}

	public static <T> Codec<MappedRegistry<T>> dataPackCodec(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, Codec<T> codec) {
		return RegistryDataPackCodec.create(resourceKey, lifecycle, codec);
	}

	public static <T> Codec<MappedRegistry<T>> directCodec(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, Codec<T> codec) {
		return Codec.unboundedMap(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(resourceKey), ResourceKey::location), codec).xmap(map -> {
			MappedRegistry<T> mappedRegistry = new MappedRegistry<>(resourceKey, lifecycle);
			map.forEach((resourceKeyxx, object) -> mappedRegistry.register(resourceKeyxx, object, lifecycle));
			return mappedRegistry;
		}, mappedRegistry -> ImmutableMap.copyOf(mappedRegistry.keyStorage));
	}

	static record RegistryEntry<T>(ResourceKey<T> key, int id, T value) {
	}
}
