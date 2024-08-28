package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
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
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.RandomSource;

public class MappedRegistry<T> implements WritableRegistry<T> {
	private final ResourceKey<? extends Registry<T>> key;
	private final ObjectList<Holder.Reference<T>> byId = new ObjectArrayList<>(256);
	private final Reference2IntMap<T> toId = Util.make(
		new Reference2IntOpenHashMap<>(), reference2IntOpenHashMap -> reference2IntOpenHashMap.defaultReturnValue(-1)
	);
	private final Map<ResourceLocation, Holder.Reference<T>> byLocation = new HashMap();
	private final Map<ResourceKey<T>, Holder.Reference<T>> byKey = new HashMap();
	private final Map<T, Holder.Reference<T>> byValue = new IdentityHashMap();
	private final Map<ResourceKey<T>, RegistrationInfo> registrationInfos = new IdentityHashMap();
	private Lifecycle registryLifecycle;
	private final Map<TagKey<T>, HolderSet.Named<T>> frozenTags = new IdentityHashMap();
	MappedRegistry.TagSet<T> allTags = MappedRegistry.TagSet.unbound();
	private boolean frozen;
	@Nullable
	private Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;

	@Override
	public Stream<HolderSet.Named<T>> listTags() {
		return this.getTags();
	}

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
			throw (IllegalStateException)Util.pauseInIde((T)(new IllegalStateException("Adding duplicate key '" + resourceKey + "' to registry")));
		} else if (this.byValue.containsKey(object)) {
			throw (IllegalStateException)Util.pauseInIde((T)(new IllegalStateException("Adding duplicate value '" + object + "' to registry")));
		} else {
			Holder.Reference<T> reference;
			if (this.unregisteredIntrusiveHolders != null) {
				reference = (Holder.Reference<T>)this.unregisteredIntrusiveHolders.remove(object);
				if (reference == null) {
					throw new AssertionError("Missing intrusive holder for " + resourceKey + ":" + object);
				}

				reference.bindKey(resourceKey);
			} else {
				reference = (Holder.Reference<T>)this.byKey.computeIfAbsent(resourceKey, resourceKeyx -> Holder.Reference.createStandAlone(this, resourceKeyx));
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
	public T getValue(@Nullable ResourceKey<T> resourceKey) {
		return getValueFromNullable((Holder.Reference<T>)this.byKey.get(resourceKey));
	}

	@Nullable
	@Override
	public T byId(int i) {
		return (T)(i >= 0 && i < this.byId.size() ? ((Holder.Reference)this.byId.get(i)).value() : null);
	}

	@Override
	public Optional<Holder.Reference<T>> get(int i) {
		return i >= 0 && i < this.byId.size() ? Optional.ofNullable((Holder.Reference)this.byId.get(i)) : Optional.empty();
	}

	@Override
	public Optional<Holder.Reference<T>> get(ResourceLocation resourceLocation) {
		return Optional.ofNullable((Holder.Reference)this.byLocation.get(resourceLocation));
	}

	@Override
	public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
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
				return Holder.Reference.createStandAlone(this, resourceKeyx);
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
	public T getValue(@Nullable ResourceLocation resourceLocation) {
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
	public Stream<Holder.Reference<T>> listElements() {
		return this.byId.stream();
	}

	@Override
	public Stream<HolderSet.Named<T>> getTags() {
		return this.allTags.getTags();
	}

	HolderSet.Named<T> getOrCreateTagForRegistration(TagKey<T> tagKey) {
		return (HolderSet.Named<T>)this.frozenTags.computeIfAbsent(tagKey, this::createTag);
	}

	private HolderSet.Named<T> createTag(TagKey<T> tagKey) {
		return new HolderSet.Named<>(this, tagKey);
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

				if (this.allTags.isBound()) {
					throw new IllegalStateException("Tags already present before freezing");
				} else {
					List<ResourceLocation> list2 = this.frozenTags
						.entrySet()
						.stream()
						.filter(entry -> !((HolderSet.Named)entry.getValue()).isBound())
						.map(entry -> ((TagKey)entry.getKey()).location())
						.sorted()
						.toList();
					if (!list2.isEmpty()) {
						throw new IllegalStateException("Unbound tags in registry " + this.key() + ": " + list2);
					} else {
						this.allTags = MappedRegistry.TagSet.fromMap(this.frozenTags);
						this.refreshTagsInHolders();
						return this;
					}
				}
			}
		}
	}

	@Override
	public Holder.Reference<T> createIntrusiveHolder(T object) {
		if (this.unregisteredIntrusiveHolders == null) {
			throw new IllegalStateException("This registry can't create intrusive holders");
		} else {
			this.validateWrite();
			return (Holder.Reference<T>)this.unregisteredIntrusiveHolders.computeIfAbsent(object, objectx -> Holder.Reference.createIntrusive(this, (T)objectx));
		}
	}

	@Override
	public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
		return this.allTags.get(tagKey);
	}

	private Holder.Reference<T> validateAndUnwrapTagElement(TagKey<T> tagKey, Holder<T> holder) {
		if (!holder.canSerializeIn(this)) {
			throw new IllegalStateException("Can't create named set " + tagKey + " containing value " + holder + " from outside registry " + this);
		} else if (holder instanceof Holder.Reference) {
			return (Holder.Reference<T>)holder;
		} else {
			throw new IllegalStateException("Found direct holder " + holder + " value in tag " + tagKey);
		}
	}

	@Override
	public void bindTag(TagKey<T> tagKey, List<Holder<T>> list) {
		this.validateWrite();
		this.getOrCreateTagForRegistration(tagKey).bind(list);
	}

	void refreshTagsInHolders() {
		Map<Holder.Reference<T>, List<TagKey<T>>> map = new IdentityHashMap();
		this.byKey.values().forEach(reference -> map.put(reference, new ArrayList()));
		this.allTags.forEach((tagKey, named) -> {
			for (Holder<T> holder : named) {
				Holder.Reference<T> reference = this.validateAndUnwrapTagElement(tagKey, holder);
				((List)map.get(reference)).add(tagKey);
			}
		});
		map.forEach(Holder.Reference::bindTags);
	}

	public void bindAllTagsToEmpty() {
		this.validateWrite();
		this.frozenTags.values().forEach(named -> named.bind(List.of()));
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
				return MappedRegistry.this.getOrCreateTagForRegistration(tagKey);
			}
		};
	}

	@Override
	public Registry.PendingTags<T> prepareTagReload(TagLoader.LoadResult<T> loadResult) {
		if (!this.frozen) {
			throw new IllegalStateException("Invalid method used for tag loading");
		} else {
			Builder<TagKey<T>, HolderSet.Named<T>> builder = ImmutableMap.builder();
			final Map<TagKey<T>, List<Holder<T>>> map = new HashMap();
			loadResult.tags().forEach((tagKey, list) -> {
				HolderSet.Named<T> named = (HolderSet.Named<T>)this.frozenTags.get(tagKey);
				if (named == null) {
					named = this.createTag(tagKey);
				}

				builder.put(tagKey, named);
				map.put(tagKey, List.copyOf(list));
			});
			final ImmutableMap<TagKey<T>, HolderSet.Named<T>> immutableMap = builder.build();
			final HolderLookup.RegistryLookup<T> registryLookup = new HolderLookup.RegistryLookup.Delegate<T>() {
				@Override
				public HolderLookup.RegistryLookup<T> parent() {
					return MappedRegistry.this;
				}

				@Override
				public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
					return Optional.ofNullable(immutableMap.get(tagKey));
				}

				@Override
				public Stream<HolderSet.Named<T>> listTags() {
					return immutableMap.values().stream();
				}
			};
			return new Registry.PendingTags<T>() {
				@Override
				public ResourceKey<? extends Registry<? extends T>> key() {
					return MappedRegistry.this.key();
				}

				@Override
				public HolderLookup.RegistryLookup<T> lookup() {
					return registryLookup;
				}

				@Override
				public void apply() {
					immutableMap.forEach((tagKey, named) -> {
						List<Holder<T>> list = (List<Holder<T>>)map.getOrDefault(tagKey, List.of());
						named.bind(list);
					});
					MappedRegistry.this.allTags = MappedRegistry.TagSet.fromMap(immutableMap);
					MappedRegistry.this.refreshTagsInHolders();
				}
			};
		}
	}

	interface TagSet<T> {
		static <T> MappedRegistry.TagSet<T> unbound() {
			return new MappedRegistry.TagSet<T>() {
				@Override
				public boolean isBound() {
					return false;
				}

				@Override
				public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
					throw new IllegalStateException("Tags not bound, trying to access " + tagKey);
				}

				@Override
				public void forEach(BiConsumer<? super TagKey<T>, ? super HolderSet.Named<T>> biConsumer) {
					throw new IllegalStateException("Tags not bound");
				}

				@Override
				public Stream<HolderSet.Named<T>> getTags() {
					throw new IllegalStateException("Tags not bound");
				}
			};
		}

		static <T> MappedRegistry.TagSet<T> fromMap(Map<TagKey<T>, HolderSet.Named<T>> map) {
			return new MappedRegistry.TagSet<T>() {
				@Override
				public boolean isBound() {
					return true;
				}

				@Override
				public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
					return Optional.ofNullable((HolderSet.Named)map.get(tagKey));
				}

				@Override
				public void forEach(BiConsumer<? super TagKey<T>, ? super HolderSet.Named<T>> biConsumer) {
					map.forEach(biConsumer);
				}

				@Override
				public Stream<HolderSet.Named<T>> getTags() {
					return map.values().stream();
				}
			};
		}

		boolean isBound();

		Optional<HolderSet.Named<T>> get(TagKey<T> tagKey);

		void forEach(BiConsumer<? super TagKey<T>, ? super HolderSet.Named<T>> biConsumer);

		Stream<HolderSet.Named<T>> getTags();
	}
}
