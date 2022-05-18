package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class MappedRegistry<T> extends WritableRegistry<T> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ObjectList<Holder.Reference<T>> byId = new ObjectArrayList<>(256);
	private final Object2IntMap<T> toId = Util.make(
		new Object2IntOpenCustomHashMap<>(Util.identityStrategy()), object2IntOpenCustomHashMap -> object2IntOpenCustomHashMap.defaultReturnValue(-1)
	);
	private final Map<ResourceLocation, Holder.Reference<T>> byLocation = new HashMap();
	private final Map<ResourceKey<T>, Holder.Reference<T>> byKey = new HashMap();
	private final Map<T, Holder.Reference<T>> byValue = new IdentityHashMap();
	private final Map<T, Lifecycle> lifecycles = new IdentityHashMap();
	private Lifecycle elementsLifecycle;
	private volatile Map<TagKey<T>, HolderSet.Named<T>> tags = new IdentityHashMap();
	private boolean frozen;
	@Nullable
	private final Function<T, Holder.Reference<T>> customHolderProvider;
	@Nullable
	private Map<T, Holder.Reference<T>> intrusiveHolderCache;
	@Nullable
	private List<Holder.Reference<T>> holdersInOrder;
	private int nextId;

	public MappedRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, @Nullable Function<T, Holder.Reference<T>> function) {
		super(resourceKey, lifecycle);
		this.elementsLifecycle = lifecycle;
		this.customHolderProvider = function;
		if (function != null) {
			this.intrusiveHolderCache = new IdentityHashMap();
		}
	}

	private List<Holder.Reference<T>> holdersInOrder() {
		if (this.holdersInOrder == null) {
			this.holdersInOrder = this.byId.stream().filter(Objects::nonNull).toList();
		}

		return this.holdersInOrder;
	}

	private void validateWrite(ResourceKey<T> resourceKey) {
		if (this.frozen) {
			throw new IllegalStateException("Registry is already frozen (trying to add key " + resourceKey + ")");
		}
	}

	@Override
	public Holder<T> registerMapping(int i, ResourceKey<T> resourceKey, T object, Lifecycle lifecycle) {
		return this.registerMapping(i, resourceKey, object, lifecycle, true);
	}

	private Holder<T> registerMapping(int i, ResourceKey<T> resourceKey, T object, Lifecycle lifecycle, boolean bl) {
		this.validateWrite(resourceKey);
		Validate.notNull(resourceKey);
		Validate.notNull(object);
		this.byId.size(Math.max(this.byId.size(), i + 1));
		this.toId.put(object, i);
		this.holdersInOrder = null;
		if (bl && this.byKey.containsKey(resourceKey)) {
			Util.logAndPauseIfInIde("Adding duplicate key '" + resourceKey + "' to registry");
		}

		if (this.byValue.containsKey(object)) {
			Util.logAndPauseIfInIde("Adding duplicate value '" + object + "' to registry");
		}

		this.lifecycles.put(object, lifecycle);
		this.elementsLifecycle = this.elementsLifecycle.add(lifecycle);
		if (this.nextId <= i) {
			this.nextId = i + 1;
		}

		Holder.Reference<T> reference;
		if (this.customHolderProvider != null) {
			reference = (Holder.Reference<T>)this.customHolderProvider.apply(object);
			Holder.Reference<T> reference2 = (Holder.Reference<T>)this.byKey.put(resourceKey, reference);
			if (reference2 != null && reference2 != reference) {
				throw new IllegalStateException("Invalid holder present for key " + resourceKey);
			}
		} else {
			reference = (Holder.Reference<T>)this.byKey.computeIfAbsent(resourceKey, resourceKeyx -> Holder.Reference.createStandAlone(this, resourceKeyx));
		}

		this.byLocation.put(resourceKey.location(), reference);
		this.byValue.put(object, reference);
		reference.bind(resourceKey, object);
		this.byId.set(i, reference);
		return reference;
	}

	@Override
	public Holder<T> register(ResourceKey<T> resourceKey, T object, Lifecycle lifecycle) {
		return this.registerMapping(this.nextId, resourceKey, object, lifecycle);
	}

	@Override
	public Holder<T> registerOrOverride(OptionalInt optionalInt, ResourceKey<T> resourceKey, T object, Lifecycle lifecycle) {
		this.validateWrite(resourceKey);
		Validate.notNull(resourceKey);
		Validate.notNull(object);
		Holder<T> holder = (Holder<T>)this.byKey.get(resourceKey);
		T object2 = holder != null && holder.isBound() ? holder.value() : null;
		int i;
		if (object2 == null) {
			i = optionalInt.orElse(this.nextId);
		} else {
			i = this.toId.getInt(object2);
			if (optionalInt.isPresent() && optionalInt.getAsInt() != i) {
				throw new IllegalStateException("ID mismatch");
			}

			this.lifecycles.remove(object2);
			this.toId.removeInt(object2);
			this.byValue.remove(object2);
		}

		return this.registerMapping(i, resourceKey, object, lifecycle, false);
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
		return i >= 0 && i < this.byId.size() ? getValueFromNullable((Holder.Reference<T>)this.byId.get(i)) : null;
	}

	@Override
	public Optional<Holder<T>> getHolder(int i) {
		return i >= 0 && i < this.byId.size() ? Optional.ofNullable((Holder)this.byId.get(i)) : Optional.empty();
	}

	@Override
	public Optional<Holder<T>> getHolder(ResourceKey<T> resourceKey) {
		return Optional.ofNullable((Holder)this.byKey.get(resourceKey));
	}

	@Override
	public Holder<T> getOrCreateHolderOrThrow(ResourceKey<T> resourceKey) {
		return (Holder<T>)this.byKey.computeIfAbsent(resourceKey, resourceKeyx -> {
			if (this.customHolderProvider != null) {
				throw new IllegalStateException("This registry can't create new holders without value");
			} else {
				this.validateWrite(resourceKeyx);
				return Holder.Reference.createStandAlone(this, resourceKeyx);
			}
		});
	}

	@Override
	public DataResult<Holder<T>> getOrCreateHolder(ResourceKey<T> resourceKey) {
		Holder.Reference<T> reference = (Holder.Reference<T>)this.byKey.get(resourceKey);
		if (reference == null) {
			if (this.customHolderProvider != null) {
				return DataResult.error("This registry can't create new holders without value (requested key: " + resourceKey + ")");
			}

			if (this.frozen) {
				return DataResult.error("Registry is already frozen (requested key: " + resourceKey + ")");
			}

			reference = Holder.Reference.createStandAlone(this, resourceKey);
			this.byKey.put(resourceKey, reference);
		}

		return DataResult.success(reference);
	}

	@Override
	public int size() {
		return this.byKey.size();
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
		return Iterators.transform(this.holdersInOrder().iterator(), Holder::value);
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
		return this.holdersInOrder().stream();
	}

	@Override
	public boolean isKnownTagName(TagKey<T> tagKey) {
		return this.tags.containsKey(tagKey);
	}

	@Override
	public Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags() {
		return this.tags.entrySet().stream().map(entry -> Pair.of((TagKey)entry.getKey(), (HolderSet.Named)entry.getValue()));
	}

	@Override
	public HolderSet.Named<T> getOrCreateTag(TagKey<T> tagKey) {
		HolderSet.Named<T> named = (HolderSet.Named<T>)this.tags.get(tagKey);
		if (named == null) {
			named = this.createTag(tagKey);
			Map<TagKey<T>, HolderSet.Named<T>> map = new IdentityHashMap(this.tags);
			map.put(tagKey, named);
			this.tags = map;
		}

		return named;
	}

	private HolderSet.Named<T> createTag(TagKey<T> tagKey) {
		return new HolderSet.Named<>(this, tagKey);
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
	public Optional<Holder<T>> getRandom(RandomSource randomSource) {
		return Util.getRandomSafe(this.holdersInOrder(), randomSource).map(Holder::hackyErase);
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
		this.frozen = true;
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
			if (this.intrusiveHolderCache != null) {
				List<Holder.Reference<T>> list2 = this.intrusiveHolderCache.values().stream().filter(reference -> !reference.isBound()).toList();
				if (!list2.isEmpty()) {
					throw new IllegalStateException("Some intrusive holders were not added to registry: " + list2);
				}

				this.intrusiveHolderCache = null;
			}

			return this;
		}
	}

	@Override
	public Holder.Reference<T> createIntrusiveHolder(T object) {
		if (this.customHolderProvider == null) {
			throw new IllegalStateException("This registry can't create intrusive holders");
		} else if (!this.frozen && this.intrusiveHolderCache != null) {
			return (Holder.Reference<T>)this.intrusiveHolderCache.computeIfAbsent(object, objectx -> Holder.Reference.createIntrusive(this, (T)objectx));
		} else {
			throw new IllegalStateException("Registry is already frozen");
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
				if (!holder.isValidInRegistry(this)) {
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

		Map<TagKey<T>, HolderSet.Named<T>> map3 = new IdentityHashMap(this.tags);
		map.forEach((tagKey, list) -> ((HolderSet.Named)map3.computeIfAbsent(tagKey, this::createTag)).bind(list));
		map2.forEach(Holder.Reference::bindTags);
		this.tags = map3;
	}

	@Override
	public void resetTags() {
		this.tags.values().forEach(named -> named.bind(List.of()));
		this.byKey.values().forEach(reference -> reference.bindTags(Set.of()));
	}
}
