package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;

public class MappedRegistry<T> implements WritableRegistry<T> {
	private static final Logger LOGGER = LogUtils.getLogger();
	final ResourceKey<? extends Registry<T>> key;
	private final ObjectList<Holder.Reference<T>> byId = new ObjectArrayList<>(256);
	private final Reference2IntMap<T> toId = Util.make(
		new Reference2IntOpenHashMap<>(), reference2IntOpenHashMap -> reference2IntOpenHashMap.defaultReturnValue(-1)
	);
	private final Map<ResourceLocation, Holder.Reference<T>> byLocation = new HashMap();
	private final Map<ResourceKey<T>, Holder.Reference<T>> byKey = new HashMap();
	private final Map<T, Holder.Reference<T>> byValue = new IdentityHashMap();
	private final Map<ResourceKey<T>, RegistrationInfo> registrationInfos = new IdentityHashMap();
	private Lifecycle registryLifecycle;
	private volatile Map<TagKey<T>, HolderSet.Named<T>> tags = new IdentityHashMap();
	private boolean frozen;
	@Nullable
	private Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;
	private final HolderLookup.RegistryLookup<T> lookup = new HolderLookup.RegistryLookup<T>() {
		@Override
		public ResourceKey<? extends Registry<? extends T>> key() {
			return MappedRegistry.this.key;
		}

		@Override
		public Lifecycle registryLifecycle() {
			return MappedRegistry.this.registryLifecycle();
		}

		@Override
		public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
			return MappedRegistry.this.getHolder(resourceKey);
		}

		@Override
		public Stream<Holder.Reference<T>> listElements() {
			return MappedRegistry.this.holders();
		}

		@Override
		public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
			return MappedRegistry.this.getTag(tagKey);
		}

		@Override
		public Stream<HolderSet.Named<T>> listTags() {
			return MappedRegistry.this.getTags().map(Pair::getSecond);
		}
	};
	private final Object tagAdditionLock = new Object();

	public MappedRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle) {
		this(resourceKey, lifecycle, false);
	}

	public MappedRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, boolean bl) {
		this.key = resourceKey;
		this.registryLifecycle = lifecycle;
		if (bl) {
			this.unregisteredIntrusiveHolders = new IdentityHashMap();
		}
	}

	@Override
	public ResourceKey<? extends Registry<T>> key() {
		return this.key;
	}

	public String toString() {
		return "Registry[" + this.key + " (" + this.registryLifecycle + ")]";
	}

	private void validateWrite() {
		if (this.frozen) {
			throw new IllegalStateException("Registry is already frozen");
		}
	}

	private void validateWrite(ResourceKey<T> resourceKey) {
		if (this.frozen) {
			throw new IllegalStateException("Registry is already frozen (trying to add key " + resourceKey + ")");
		}
	}

	@Override
	public Holder.Reference<T> register(ResourceKey<T> resourceKey, T object, RegistrationInfo registrationInfo) {
		this.validateWrite(resourceKey);
		Objects.requireNonNull(resourceKey);
		Objects.requireNonNull(object);
		if (this.byLocation.containsKey(resourceKey.location())) {
			Util.pauseInIde((T)(new IllegalStateException("Adding duplicate key '" + resourceKey + "' to registry")));
		}

		if (this.byValue.containsKey(object)) {
			Util.pauseInIde((T)(new IllegalStateException("Adding duplicate value '" + object + "' to registry")));
		}

		Holder.Reference<T> reference;
		if (this.unregisteredIntrusiveHolders != null) {
			reference = (Holder.Reference<T>)this.unregisteredIntrusiveHolders.remove(object);
			if (reference == null) {
				throw new AssertionError("Missing intrusive holder for " + resourceKey + ":" + object);
			}

			reference.bindKey(resourceKey);
		} else {
			reference = (Holder.Reference<T>)this.byKey
				.computeIfAbsent(resourceKey, resourceKeyx -> Holder.Reference.createStandAlone(this.holderOwner(), resourceKeyx));
		}

		this.byKey.put(resourceKey, reference);
		this.byLocation.put(resourceKey.location(), reference);
		this.byValue.put(object, reference);
		int i = this.byId.size();
		this.byId.add(reference);
		this.toId.put(object, i);
		this.registrationInfos.put(resourceKey, registrationInfo);
		this.registryLifecycle = this.registryLifecycle.add(registrationInfo.lifecycle());
		return reference;
	}

	@Nullable
	@Override
	public ResourceLocation getKey(T object) {
		Holder.Reference<T> reference = (Holder.Reference<T>)this.byValue.get(object);
		return reference != null ? reference.key().location() : null;
	}

	@Override
	public Optional<ResourceKey<T>> getResourceKey(T object) {
		return Optional.ofNullable((Holder.Reference)this.byValue.get(object)).map(Holder.Reference::key);
	}

	@Override
	public int getId(@Nullable T object) {
		return this.toId.getInt(object);
	}

	@Nullable
	@Override
	public T get(@Nullable ResourceKey<T> resourceKey) {
		return getValueFromNullable((Holder.Reference<T>)this.byKey.get(resourceKey));
	}

	@Nullable
	@Override
	public T byId(int i) {
		return (T)(i >= 0 && i < this.byId.size() ? ((Holder.Reference)this.byId.get(i)).value() : null);
	}

	@Override
	public Optional<Holder.Reference<T>> getHolder(int i) {
		return i >= 0 && i < this.byId.size() ? Optional.ofNullable((Holder.Reference)this.byId.get(i)) : Optional.empty();
	}

	@Override
	public Optional<Holder.Reference<T>> getHolder(ResourceLocation resourceLocation) {
		return Optional.ofNullable((Holder.Reference)this.byLocation.get(resourceLocation));
	}

	@Override
	public Optional<Holder.Reference<T>> getHolder(ResourceKey<T> resourceKey) {
		return Optional.ofNullable((Holder.Reference)this.byKey.get(resourceKey));
	}

	@Override
	public Optional<Holder.Reference<T>> getAny() {
		return this.byId.isEmpty() ? Optional.empty() : Optional.of((Holder.Reference)this.byId.getFirst());
	}

	@Override
	public Holder<T> wrapAsHolder(T object) {
		Holder.Reference<T> reference = (Holder.Reference<T>)this.byValue.get(object);
		return (Holder<T>)(reference != null ? reference : Holder.direct(object));
	}

	Holder.Reference<T> getOrCreateHolderOrThrow(ResourceKey<T> resourceKey) {
		return (Holder.Reference<T>)this.byKey.computeIfAbsent(resourceKey, resourceKeyx -> {
			if (this.unregisteredIntrusiveHolders != null) {
				throw new IllegalStateException("This registry can't create new holders without value");
			} else {
				this.validateWrite(resourceKeyx);
				return Holder.Reference.createStandAlone(this.holderOwner(), resourceKeyx);
			}
		});
	}

	@Override
	public int size() {
		return this.byKey.size();
	}

	@Override
	public Optional<RegistrationInfo> registrationInfo(ResourceKey<T> resourceKey) {
		return Optional.ofNullable((RegistrationInfo)this.registrationInfos.get(resourceKey));
	}

	@Override
	public Lifecycle registryLifecycle() {
		return this.registryLifecycle;
	}

	public Iterator<T> iterator() {
		return Iterators.transform(this.byId.iterator(), Holder::value);
	}

	@Nullable
	@Override
	public T get(@Nullable ResourceLocation resourceLocation) {
		Holder.Reference<T> reference = (Holder.Reference<T>)this.byLocation.get(resourceLocation);
		return getValueFromNullable(reference);
	}

	@Nullable
	private static <T> T getValueFromNullable(@Nullable Holder.Reference<T> reference) {
		return reference != null ? reference.value() : null;
	}

	@Override
	public Set<ResourceLocation> keySet() {
		return Collections.unmodifiableSet(this.byLocation.keySet());
	}

	@Override
	public Set<ResourceKey<T>> registryKeySet() {
		return Collections.unmodifiableSet(this.byKey.keySet());
	}

	@Override
	public Set<Entry<ResourceKey<T>, T>> entrySet() {
		return Collections.unmodifiableSet(Maps.transformValues(this.byKey, Holder::value).entrySet());
	}

	@Override
	public Stream<Holder.Reference<T>> holders() {
		return this.byId.stream();
	}

	@Override
	public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags() {
		return this.tags.entrySet().stream().map(entry -> Pair.of((TagKey)entry.getKey(), (HolderSet.Named)entry.getValue()));
	}

	@Override
	public HolderSet.Named<T> getOrCreateTag(TagKey<T> tagKey) {
		HolderSet.Named<T> named = (HolderSet.Named<T>)this.tags.get(tagKey);
		if (named != null) {
			return named;
		} else {
			synchronized (this.tagAdditionLock) {
				named = (HolderSet.Named<T>)this.tags.get(tagKey);
				if (named != null) {
					return named;
				} else {
					named = this.createTag(tagKey);
					Map<TagKey<T>, HolderSet.Named<T>> map = new IdentityHashMap(this.tags);
					map.put(tagKey, named);
					this.tags = map;
					return named;
				}
			}
		}
	}

	private HolderSet.Named<T> createTag(TagKey<T> tagKey) {
		return new HolderSet.Named<>(this.holderOwner(), tagKey);
	}

	@Override
	public Stream<TagKey<T>> getTagNames() {
		return this.tags.keySet().stream();
	}

	@Override
	public boolean isEmpty() {
		return this.byKey.isEmpty();
	}

	@Override
	public Optional<Holder.Reference<T>> getRandom(RandomSource randomSource) {
		return Util.getRandomSafe(this.byId, randomSource);
	}

	@Override
	public boolean containsKey(ResourceLocation resourceLocation) {
		return this.byLocation.containsKey(resourceLocation);
	}

	@Override
	public boolean containsKey(ResourceKey<T> resourceKey) {
		return this.byKey.containsKey(resourceKey);
	}

	@Override
	public Registry<T> freeze() {
		if (this.frozen) {
			return this;
		} else {
			this.frozen = true;
			this.byValue.forEach((object, reference) -> reference.bindValue(object));
			List<ResourceLocation> list = this.byKey
				.entrySet()
				.stream()
				.filter(entry -> !((Holder.Reference)entry.getValue()).isBound())
				.map(entry -> ((ResourceKey)entry.getKey()).location())
				.sorted()
				.toList();
			if (!list.isEmpty()) {
				throw new IllegalStateException("Unbound values in registry " + this.key() + ": " + list);
			} else {
				if (this.unregisteredIntrusiveHolders != null) {
					if (!this.unregisteredIntrusiveHolders.isEmpty()) {
						throw new IllegalStateException("Some intrusive holders were not registered: " + this.unregisteredIntrusiveHolders.values());
					}

					this.unregisteredIntrusiveHolders = null;
				}

				return this;
			}
		}
	}

	@Override
	public Holder.Reference<T> createIntrusiveHolder(T object) {
		if (this.unregisteredIntrusiveHolders == null) {
			throw new IllegalStateException("This registry can't create intrusive holders");
		} else {
			this.validateWrite();
			return (Holder.Reference<T>)this.unregisteredIntrusiveHolders
				.computeIfAbsent(object, objectx -> Holder.Reference.createIntrusive(this.asLookup(), (T)objectx));
		}
	}

	@Override
	public Optional<HolderSet.Named<T>> getTag(TagKey<T> tagKey) {
		return Optional.ofNullable((HolderSet.Named)this.tags.get(tagKey));
	}

	@Override
	public void bindTags(Map<TagKey<T>, List<Holder<T>>> map) {
		Map<Holder.Reference<T>, List<TagKey<T>>> map2 = new IdentityHashMap();
		this.byKey.values().forEach(reference -> map2.put(reference, new ArrayList()));
		map.forEach((tagKey, list) -> {
			for (Holder<T> holder : list) {
				if (!holder.canSerializeIn(this.asLookup())) {
					throw new IllegalStateException("Can't create named set " + tagKey + " containing value " + holder + " from outside registry " + this);
				}

				if (!(holder instanceof Holder.Reference<T> reference)) {
					throw new IllegalStateException("Found direct holder " + holder + " value in tag " + tagKey);
				}

				((List)map2.get(reference)).add(tagKey);
			}
		});
		Set<TagKey<T>> set = Sets.<TagKey<T>>difference(this.tags.keySet(), map.keySet());
		if (!set.isEmpty()) {
			LOGGER.warn(
				"Not all defined tags for registry {} are present in data pack: {}",
				this.key(),
				set.stream().map(tagKey -> tagKey.location().toString()).sorted().collect(Collectors.joining(", "))
			);
		}

		synchronized (this.tagAdditionLock) {
			Map<TagKey<T>, HolderSet.Named<T>> map3 = new IdentityHashMap(this.tags);
			map.forEach((tagKey, list) -> ((HolderSet.Named)map3.computeIfAbsent(tagKey, this::createTag)).bind(list));
			map2.forEach(Holder.Reference::bindTags);
			this.tags = map3;
		}
	}

	@Override
	public void resetTags() {
		this.tags.values().forEach(named -> named.bind(List.of()));
		this.byKey.values().forEach(reference -> reference.bindTags(Set.of()));
	}

	@Override
	public HolderGetter<T> createRegistrationLookup() {
		this.validateWrite();
		return new HolderGetter<T>() {
			@Override
			public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
				return Optional.of(this.getOrThrow(resourceKey));
			}

			@Override
			public Holder.Reference<T> getOrThrow(ResourceKey<T> resourceKey) {
				return MappedRegistry.this.getOrCreateHolderOrThrow(resourceKey);
			}

			@Override
			public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
				return Optional.of(this.getOrThrow(tagKey));
			}

			@Override
			public HolderSet.Named<T> getOrThrow(TagKey<T> tagKey) {
				return MappedRegistry.this.getOrCreateTag(tagKey);
			}
		};
	}

	@Override
	public HolderOwner<T> holderOwner() {
		return this.lookup;
	}

	@Override
	public HolderLookup.RegistryLookup<T> asLookup() {
		return this.lookup;
	}
}
