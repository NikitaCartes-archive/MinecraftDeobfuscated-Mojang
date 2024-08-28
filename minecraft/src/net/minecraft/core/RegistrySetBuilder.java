package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.mutable.MutableObject;

public class RegistrySetBuilder {
	private final List<RegistrySetBuilder.RegistryStub<?>> entries = new ArrayList();

	static <T> HolderGetter<T> wrapContextLookup(HolderLookup.RegistryLookup<T> registryLookup) {
		return new RegistrySetBuilder.EmptyTagLookup<T>(registryLookup) {
			@Override
			public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
				return registryLookup.get(resourceKey);
			}
		};
	}

	static <T> HolderLookup.RegistryLookup<T> lookupFromMap(
		ResourceKey<? extends Registry<? extends T>> resourceKey, Lifecycle lifecycle, HolderOwner<T> holderOwner, Map<ResourceKey<T>, Holder.Reference<T>> map
	) {
		return new RegistrySetBuilder.EmptyTagRegistryLookup<T>(holderOwner) {
			@Override
			public ResourceKey<? extends Registry<? extends T>> key() {
				return resourceKey;
			}

			@Override
			public Lifecycle registryLifecycle() {
				return lifecycle;
			}

			@Override
			public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
				return Optional.ofNullable((Holder.Reference)map.get(resourceKey));
			}

			@Override
			public Stream<Holder.Reference<T>> listElements() {
				return map.values().stream();
			}
		};
	}

	public <T> RegistrySetBuilder add(
		ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, RegistrySetBuilder.RegistryBootstrap<T> registryBootstrap
	) {
		this.entries.add(new RegistrySetBuilder.RegistryStub<>(resourceKey, lifecycle, registryBootstrap));
		return this;
	}

	public <T> RegistrySetBuilder add(ResourceKey<? extends Registry<T>> resourceKey, RegistrySetBuilder.RegistryBootstrap<T> registryBootstrap) {
		return this.add(resourceKey, Lifecycle.stable(), registryBootstrap);
	}

	private RegistrySetBuilder.BuildState createState(RegistryAccess registryAccess) {
		RegistrySetBuilder.BuildState buildState = RegistrySetBuilder.BuildState.create(
			registryAccess, this.entries.stream().map(RegistrySetBuilder.RegistryStub::key)
		);
		this.entries.forEach(registryStub -> registryStub.apply(buildState));
		return buildState;
	}

	private static HolderLookup.Provider buildProviderWithContext(
		RegistrySetBuilder.UniversalOwner universalOwner, RegistryAccess registryAccess, Stream<HolderLookup.RegistryLookup<?>> stream
	) {
		record Entry<T>(HolderLookup.RegistryLookup<T> lookup, RegistryOps.RegistryInfo<T> opsInfo) {
			public static <T> Entry<T> createForContextRegistry(HolderLookup.RegistryLookup<T> registryLookup) {
				return new Entry<>(
					new RegistrySetBuilder.EmptyTagLookupWrapper<>(registryLookup, registryLookup), RegistryOps.RegistryInfo.fromRegistryLookup(registryLookup)
				);
			}

			public static <T> Entry<T> createForNewRegistry(RegistrySetBuilder.UniversalOwner universalOwner, HolderLookup.RegistryLookup<T> registryLookup) {
				return new Entry<>(
					new RegistrySetBuilder.EmptyTagLookupWrapper<>(universalOwner.cast(), registryLookup),
					new RegistryOps.RegistryInfo<>(universalOwner.cast(), registryLookup, registryLookup.registryLifecycle())
				);
			}
		}

		final Map<ResourceKey<? extends Registry<?>>, Entry<?>> map = new HashMap();
		registryAccess.registries().forEach(registryEntry -> map.put(registryEntry.key(), Entry.createForContextRegistry(registryEntry.value())));
		stream.forEach(registryLookup -> map.put(registryLookup.key(), Entry.createForNewRegistry(universalOwner, registryLookup)));
		return new HolderLookup.Provider() {
			@Override
			public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
				return map.keySet().stream();
			}

			<T> Optional<Entry<T>> getEntry(ResourceKey<? extends Registry<? extends T>> resourceKey) {
				return Optional.ofNullable((Entry)map.get(resourceKey));
			}

			@Override
			public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
				return this.getEntry(resourceKey).map(Entry::lookup);
			}

			@Override
			public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> dynamicOps) {
				return RegistryOps.create(dynamicOps, new RegistryOps.RegistryInfoLookup() {
					@Override
					public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
						return getEntry(resourceKey).map(Entry::opsInfo);
					}
				});
			}
		};
	}

	public HolderLookup.Provider build(RegistryAccess registryAccess) {
		RegistrySetBuilder.BuildState buildState = this.createState(registryAccess);
		Stream<HolderLookup.RegistryLookup<?>> stream = this.entries
			.stream()
			.map(registryStub -> registryStub.collectRegisteredValues(buildState).buildAsLookup(buildState.owner));
		HolderLookup.Provider provider = buildProviderWithContext(buildState.owner, registryAccess, stream);
		buildState.reportNotCollectedHolders();
		buildState.reportUnclaimedRegisteredValues();
		buildState.throwOnError();
		return provider;
	}

	private HolderLookup.Provider createLazyFullPatchedRegistries(
		RegistryAccess registryAccess,
		HolderLookup.Provider provider,
		Cloner.Factory factory,
		Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryContents<?>> map,
		HolderLookup.Provider provider2
	) {
		RegistrySetBuilder.UniversalOwner universalOwner = new RegistrySetBuilder.UniversalOwner();
		MutableObject<HolderLookup.Provider> mutableObject = new MutableObject<>();
		List<HolderLookup.RegistryLookup<?>> list = (List<HolderLookup.RegistryLookup<?>>)map.keySet()
			.stream()
			.map(resourceKey -> this.createLazyFullPatchedRegistries(universalOwner, factory, resourceKey, provider2, provider, mutableObject))
			.collect(Collectors.toUnmodifiableList());
		HolderLookup.Provider provider3 = buildProviderWithContext(universalOwner, registryAccess, list.stream());
		mutableObject.setValue(provider3);
		return provider3;
	}

	private <T> HolderLookup.RegistryLookup<T> createLazyFullPatchedRegistries(
		HolderOwner<T> holderOwner,
		Cloner.Factory factory,
		ResourceKey<? extends Registry<? extends T>> resourceKey,
		HolderLookup.Provider provider,
		HolderLookup.Provider provider2,
		MutableObject<HolderLookup.Provider> mutableObject
	) {
		Cloner<T> cloner = factory.cloner(resourceKey);
		if (cloner == null) {
			throw new NullPointerException("No cloner for " + resourceKey.location());
		} else {
			Map<ResourceKey<T>, Holder.Reference<T>> map = new HashMap();
			HolderLookup.RegistryLookup<T> registryLookup = provider.lookupOrThrow(resourceKey);
			registryLookup.listElements().forEach(reference -> {
				ResourceKey<T> resourceKeyx = reference.key();
				RegistrySetBuilder.LazyHolder<T> lazyHolder = new RegistrySetBuilder.LazyHolder<>(holderOwner, resourceKeyx);
				lazyHolder.supplier = () -> cloner.clone((T)reference.value(), provider, mutableObject.getValue());
				map.put(resourceKeyx, lazyHolder);
			});
			HolderLookup.RegistryLookup<T> registryLookup2 = provider2.lookupOrThrow(resourceKey);
			registryLookup2.listElements().forEach(reference -> {
				ResourceKey<T> resourceKeyx = reference.key();
				map.computeIfAbsent(resourceKeyx, resourceKey2 -> {
					RegistrySetBuilder.LazyHolder<T> lazyHolder = new RegistrySetBuilder.LazyHolder<>(holderOwner, resourceKeyx);
					lazyHolder.supplier = () -> cloner.clone((T)reference.value(), provider2, mutableObject.getValue());
					return lazyHolder;
				});
			});
			Lifecycle lifecycle = registryLookup.registryLifecycle().add(registryLookup2.registryLifecycle());
			return lookupFromMap(resourceKey, lifecycle, holderOwner, map);
		}
	}

	public RegistrySetBuilder.PatchedRegistries buildPatch(RegistryAccess registryAccess, HolderLookup.Provider provider, Cloner.Factory factory) {
		RegistrySetBuilder.BuildState buildState = this.createState(registryAccess);
		Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryContents<?>> map = new HashMap();
		this.entries
			.stream()
			.map(registryStub -> registryStub.collectRegisteredValues(buildState))
			.forEach(registryContents -> map.put(registryContents.key, registryContents));
		Set<ResourceKey<? extends Registry<?>>> set = (Set<ResourceKey<? extends Registry<?>>>)registryAccess.listRegistryKeys()
			.collect(Collectors.toUnmodifiableSet());
		provider.listRegistryKeys()
			.filter(resourceKey -> !set.contains(resourceKey))
			.forEach(resourceKey -> map.putIfAbsent(resourceKey, new RegistrySetBuilder.RegistryContents(resourceKey, Lifecycle.stable(), Map.of())));
		Stream<HolderLookup.RegistryLookup<?>> stream = map.values().stream().map(registryContents -> registryContents.buildAsLookup(buildState.owner));
		HolderLookup.Provider provider2 = buildProviderWithContext(buildState.owner, registryAccess, stream);
		buildState.reportUnclaimedRegisteredValues();
		buildState.throwOnError();
		HolderLookup.Provider provider3 = this.createLazyFullPatchedRegistries(registryAccess, provider, factory, map, provider2);
		return new RegistrySetBuilder.PatchedRegistries(provider3, provider2);
	}

	static record BuildState(
		RegistrySetBuilder.UniversalOwner owner,
		RegistrySetBuilder.UniversalLookup lookup,
		Map<ResourceLocation, HolderGetter<?>> registries,
		Map<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>> registeredValues,
		List<RuntimeException> errors
	) {

		public static RegistrySetBuilder.BuildState create(RegistryAccess registryAccess, Stream<ResourceKey<? extends Registry<?>>> stream) {
			RegistrySetBuilder.UniversalOwner universalOwner = new RegistrySetBuilder.UniversalOwner();
			List<RuntimeException> list = new ArrayList();
			RegistrySetBuilder.UniversalLookup universalLookup = new RegistrySetBuilder.UniversalLookup(universalOwner);
			Builder<ResourceLocation, HolderGetter<?>> builder = ImmutableMap.builder();
			registryAccess.registries()
				.forEach(registryEntry -> builder.put(registryEntry.key().location(), RegistrySetBuilder.wrapContextLookup(registryEntry.value())));
			stream.forEach(resourceKey -> builder.put(resourceKey.location(), universalLookup));
			return new RegistrySetBuilder.BuildState(universalOwner, universalLookup, builder.build(), new HashMap(), list);
		}

		public <T> BootstrapContext<T> bootstrapContext() {
			return new BootstrapContext<T>() {
				@Override
				public Holder.Reference<T> register(ResourceKey<T> resourceKey, T object, Lifecycle lifecycle) {
					RegistrySetBuilder.RegisteredValue<?> registeredValue = (RegistrySetBuilder.RegisteredValue<?>)BuildState.this.registeredValues
						.put(resourceKey, new RegistrySetBuilder.RegisteredValue(object, lifecycle));
					if (registeredValue != null) {
						BuildState.this.errors.add(new IllegalStateException("Duplicate registration for " + resourceKey + ", new=" + object + ", old=" + registeredValue.value));
					}

					return BuildState.this.lookup.getOrCreate(resourceKey);
				}

				@Override
				public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> resourceKey) {
					return (HolderGetter<S>)BuildState.this.registries.getOrDefault(resourceKey.location(), BuildState.this.lookup);
				}
			};
		}

		public void reportUnclaimedRegisteredValues() {
			this.registeredValues
				.forEach((resourceKey, registeredValue) -> this.errors.add(new IllegalStateException("Orpaned value " + registeredValue.value + " for key " + resourceKey)));
		}

		public void reportNotCollectedHolders() {
			for (ResourceKey<Object> resourceKey : this.lookup.holders.keySet()) {
				this.errors.add(new IllegalStateException("Unreferenced key: " + resourceKey));
			}
		}

		public void throwOnError() {
			if (!this.errors.isEmpty()) {
				IllegalStateException illegalStateException = new IllegalStateException("Errors during registry creation");

				for (RuntimeException runtimeException : this.errors) {
					illegalStateException.addSuppressed(runtimeException);
				}

				throw illegalStateException;
			}
		}
	}

	abstract static class EmptyTagLookup<T> implements HolderGetter<T> {
		protected final HolderOwner<T> owner;

		protected EmptyTagLookup(HolderOwner<T> holderOwner) {
			this.owner = holderOwner;
		}

		@Override
		public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
			return Optional.of(HolderSet.emptyNamed(this.owner, tagKey));
		}
	}

	static class EmptyTagLookupWrapper<T> extends RegistrySetBuilder.EmptyTagRegistryLookup<T> implements HolderLookup.RegistryLookup.Delegate<T> {
		private final HolderLookup.RegistryLookup<T> parent;

		EmptyTagLookupWrapper(HolderOwner<T> holderOwner, HolderLookup.RegistryLookup<T> registryLookup) {
			super(holderOwner);
			this.parent = registryLookup;
		}

		@Override
		public HolderLookup.RegistryLookup<T> parent() {
			return this.parent;
		}
	}

	abstract static class EmptyTagRegistryLookup<T> extends RegistrySetBuilder.EmptyTagLookup<T> implements HolderLookup.RegistryLookup<T> {
		protected EmptyTagRegistryLookup(HolderOwner<T> holderOwner) {
			super(holderOwner);
		}

		@Override
		public Stream<HolderSet.Named<T>> listTags() {
			throw new UnsupportedOperationException("Tags are not available in datagen");
		}
	}

	static class LazyHolder<T> extends Holder.Reference<T> {
		@Nullable
		Supplier<T> supplier;

		protected LazyHolder(HolderOwner<T> holderOwner, @Nullable ResourceKey<T> resourceKey) {
			super(Holder.Reference.Type.STAND_ALONE, holderOwner, resourceKey, null);
		}

		@Override
		protected void bindValue(T object) {
			super.bindValue(object);
			this.supplier = null;
		}

		@Override
		public T value() {
			if (this.supplier != null) {
				this.bindValue((T)this.supplier.get());
			}

			return super.value();
		}
	}

	public static record PatchedRegistries(HolderLookup.Provider full, HolderLookup.Provider patches) {
	}

	static record RegisteredValue<T>(T value, Lifecycle lifecycle) {
	}

	@FunctionalInterface
	public interface RegistryBootstrap<T> {
		void run(BootstrapContext<T> bootstrapContext);
	}

	static record RegistryContents<T>(
		ResourceKey<? extends Registry<? extends T>> key, Lifecycle lifecycle, Map<ResourceKey<T>, RegistrySetBuilder.ValueAndHolder<T>> values
	) {

		public HolderLookup.RegistryLookup<T> buildAsLookup(RegistrySetBuilder.UniversalOwner universalOwner) {
			Map<ResourceKey<T>, Holder.Reference<T>> map = (Map<ResourceKey<T>, Holder.Reference<T>>)this.values
				.entrySet()
				.stream()
				.collect(
					Collectors.toUnmodifiableMap(
						java.util.Map.Entry::getKey,
						entry -> {
							RegistrySetBuilder.ValueAndHolder<T> valueAndHolder = (RegistrySetBuilder.ValueAndHolder<T>)entry.getValue();
							Holder.Reference<T> reference = (Holder.Reference<T>)valueAndHolder.holder()
								.orElseGet(() -> Holder.Reference.createStandAlone(universalOwner.cast(), (ResourceKey<T>)entry.getKey()));
							reference.bindValue(valueAndHolder.value().value());
							return reference;
						}
					)
				);
			return RegistrySetBuilder.lookupFromMap(this.key, this.lifecycle, universalOwner.cast(), map);
		}
	}

	static record RegistryStub<T>(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, RegistrySetBuilder.RegistryBootstrap<T> bootstrap) {
		void apply(RegistrySetBuilder.BuildState buildState) {
			this.bootstrap.run(buildState.bootstrapContext());
		}

		public RegistrySetBuilder.RegistryContents<T> collectRegisteredValues(RegistrySetBuilder.BuildState buildState) {
			Map<ResourceKey<T>, RegistrySetBuilder.ValueAndHolder<T>> map = new HashMap();
			Iterator<java.util.Map.Entry<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>>> iterator = buildState.registeredValues.entrySet().iterator();

			while (iterator.hasNext()) {
				java.util.Map.Entry<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>> entry = (java.util.Map.Entry<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>>)iterator.next();
				ResourceKey<?> resourceKey = (ResourceKey<?>)entry.getKey();
				if (resourceKey.isFor(this.key)) {
					RegistrySetBuilder.RegisteredValue<T> registeredValue = (RegistrySetBuilder.RegisteredValue<T>)entry.getValue();
					Holder.Reference<T> reference = (Holder.Reference<T>)buildState.lookup.holders.remove(resourceKey);
					map.put(resourceKey, new RegistrySetBuilder.ValueAndHolder<>(registeredValue, Optional.ofNullable(reference)));
					iterator.remove();
				}
			}

			return new RegistrySetBuilder.RegistryContents<>(this.key, this.lifecycle, map);
		}
	}

	static class UniversalLookup extends RegistrySetBuilder.EmptyTagLookup<Object> {
		final Map<ResourceKey<Object>, Holder.Reference<Object>> holders = new HashMap();

		public UniversalLookup(HolderOwner<Object> holderOwner) {
			super(holderOwner);
		}

		@Override
		public Optional<Holder.Reference<Object>> get(ResourceKey<Object> resourceKey) {
			return Optional.of(this.getOrCreate(resourceKey));
		}

		<T> Holder.Reference<T> getOrCreate(ResourceKey<T> resourceKey) {
			return (Holder.Reference<T>)this.holders.computeIfAbsent(resourceKey, resourceKeyx -> Holder.Reference.createStandAlone(this.owner, resourceKeyx));
		}
	}

	static class UniversalOwner implements HolderOwner<Object> {
		public <T> HolderOwner<T> cast() {
			return this;
		}
	}

	static record ValueAndHolder<T>(RegistrySetBuilder.RegisteredValue<T> value, Optional<Holder.Reference<T>> holder) {
	}
}
