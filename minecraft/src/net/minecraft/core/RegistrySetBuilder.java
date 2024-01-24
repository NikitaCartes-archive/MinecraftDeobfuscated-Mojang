package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.BootstrapContext;
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
		ResourceKey<? extends Registry<? extends T>> resourceKey, Lifecycle lifecycle, Map<ResourceKey<T>, Holder.Reference<T>> map
	) {
		return new HolderLookup.RegistryLookup<T>() {
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

			@Override
			public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
				return Optional.empty();
			}

			@Override
			public Stream<HolderSet.Named<T>> listTags() {
				return Stream.empty();
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

	private static HolderLookup.Provider buildProviderWithContext(RegistryAccess registryAccess, Stream<HolderLookup.RegistryLookup<?>> stream) {
		Stream<HolderLookup.RegistryLookup<?>> stream2 = registryAccess.registries().map(registryEntry -> registryEntry.value().asLookup());
		return HolderLookup.Provider.create(Stream.concat(stream2, stream));
	}

	public HolderLookup.Provider build(RegistryAccess registryAccess) {
		RegistrySetBuilder.BuildState buildState = this.createState(registryAccess);
		Stream<HolderLookup.RegistryLookup<?>> stream = this.entries
			.stream()
			.map(registryStub -> registryStub.collectRegisteredValues(buildState).buildAsLookup(buildState.owner));
		HolderLookup.Provider provider = buildProviderWithContext(registryAccess, stream);
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
		RegistrySetBuilder.CompositeOwner compositeOwner = new RegistrySetBuilder.CompositeOwner();
		MutableObject<HolderLookup.Provider> mutableObject = new MutableObject<>();
		List<HolderLookup.RegistryLookup<?>> list = (List<HolderLookup.RegistryLookup<?>>)map.keySet()
			.stream()
			.map(resourceKey -> this.createLazyFullPatchedRegistries(compositeOwner, factory, resourceKey, provider2, provider, mutableObject))
			.peek(compositeOwner::add)
			.collect(Collectors.toUnmodifiableList());
		HolderLookup.Provider provider3 = buildProviderWithContext(registryAccess, list.stream());
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
			return lookupFromMap(resourceKey, lifecycle, map);
		}
	}

	public RegistrySetBuilder.PatchedRegistries buildPatch(RegistryAccess registryAccess, HolderLookup.Provider provider, Cloner.Factory factory) {
		RegistrySetBuilder.BuildState buildState = this.createState(registryAccess);
		Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryContents<?>> map = new HashMap();
		this.entries
			.stream()
			.map(registryStub -> registryStub.collectRegisteredValues(buildState))
			.forEach(registryContents -> map.put(registryContents.key, registryContents));
		Set<ResourceKey<? extends Registry<?>>> set = (Set<ResourceKey<? extends Registry<?>>>)registryAccess.listRegistries()
			.collect(Collectors.toUnmodifiableSet());
		provider.listRegistries()
			.filter(resourceKey -> !set.contains(resourceKey))
			.forEach(resourceKey -> map.putIfAbsent(resourceKey, new RegistrySetBuilder.RegistryContents(resourceKey, Lifecycle.stable(), Map.of())));
		Stream<HolderLookup.RegistryLookup<?>> stream = map.values().stream().map(registryContents -> registryContents.buildAsLookup(buildState.owner));
		HolderLookup.Provider provider2 = buildProviderWithContext(registryAccess, stream);
		buildState.reportUnclaimedRegisteredValues();
		buildState.throwOnError();
		HolderLookup.Provider provider3 = this.createLazyFullPatchedRegistries(registryAccess, provider, factory, map, provider2);
		return new RegistrySetBuilder.PatchedRegistries(provider3, provider2);
	}

	static record BuildState(
		RegistrySetBuilder.CompositeOwner owner,
		RegistrySetBuilder.UniversalLookup lookup,
		Map<ResourceLocation, HolderGetter<?>> registries,
		Map<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>> registeredValues,
		List<RuntimeException> errors
	) {

		public static RegistrySetBuilder.BuildState create(RegistryAccess registryAccess, Stream<ResourceKey<? extends Registry<?>>> stream) {
			RegistrySetBuilder.CompositeOwner compositeOwner = new RegistrySetBuilder.CompositeOwner();
			List<RuntimeException> list = new ArrayList();
			RegistrySetBuilder.UniversalLookup universalLookup = new RegistrySetBuilder.UniversalLookup(compositeOwner);
			Builder<ResourceLocation, HolderGetter<?>> builder = ImmutableMap.builder();
			registryAccess.registries()
				.forEach(registryEntry -> builder.put(registryEntry.key().location(), RegistrySetBuilder.wrapContextLookup(registryEntry.value().asLookup())));
			stream.forEach(resourceKey -> builder.put(resourceKey.location(), universalLookup));
			return new RegistrySetBuilder.BuildState(compositeOwner, universalLookup, builder.build(), new HashMap(), list);
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

	static class CompositeOwner implements HolderOwner<Object> {
		private final Set<HolderOwner<?>> owners = Sets.newIdentityHashSet();

		@Override
		public boolean canSerializeIn(HolderOwner<Object> holderOwner) {
			return this.owners.contains(holderOwner);
		}

		public void add(HolderOwner<?> holderOwner) {
			this.owners.add(holderOwner);
		}

		public <T> HolderOwner<T> cast() {
			return this;
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

		public HolderLookup.RegistryLookup<T> buildAsLookup(RegistrySetBuilder.CompositeOwner compositeOwner) {
			Map<ResourceKey<T>, Holder.Reference<T>> map = (Map<ResourceKey<T>, Holder.Reference<T>>)this.values
				.entrySet()
				.stream()
				.collect(
					Collectors.toUnmodifiableMap(
						Entry::getKey,
						entry -> {
							RegistrySetBuilder.ValueAndHolder<T> valueAndHolder = (RegistrySetBuilder.ValueAndHolder<T>)entry.getValue();
							Holder.Reference<T> reference = (Holder.Reference<T>)valueAndHolder.holder()
								.orElseGet(() -> Holder.Reference.createStandAlone(compositeOwner.cast(), (ResourceKey<T>)entry.getKey()));
							reference.bindValue(valueAndHolder.value().value());
							return reference;
						}
					)
				);
			HolderLookup.RegistryLookup<T> registryLookup = RegistrySetBuilder.lookupFromMap(this.key, this.lifecycle, map);
			compositeOwner.add(registryLookup);
			return registryLookup;
		}
	}

	static record RegistryStub<T>(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, RegistrySetBuilder.RegistryBootstrap<T> bootstrap) {
		void apply(RegistrySetBuilder.BuildState buildState) {
			this.bootstrap.run(buildState.bootstrapContext());
		}

		public RegistrySetBuilder.RegistryContents<T> collectRegisteredValues(RegistrySetBuilder.BuildState buildState) {
			Map<ResourceKey<T>, RegistrySetBuilder.ValueAndHolder<T>> map = new HashMap();
			Iterator<Entry<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>>> iterator = buildState.registeredValues.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>> entry = (Entry<ResourceKey<?>, RegistrySetBuilder.RegisteredValue<?>>)iterator.next();
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

	static record ValueAndHolder<T>(RegistrySetBuilder.RegisteredValue<T> value, Optional<Holder.Reference<T>> holder) {
	}
}
