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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

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

	public HolderLookup.Provider build(RegistryAccess registryAccess) {
		RegistrySetBuilder.BuildState buildState = this.createState(registryAccess);
		Stream<HolderLookup.RegistryLookup<?>> stream = registryAccess.registries().map(registryEntry -> registryEntry.value().asLookup());
		Stream<HolderLookup.RegistryLookup<?>> stream2 = this.entries.stream().map(registryStub -> registryStub.collectChanges(buildState).buildAsLookup());
		HolderLookup.Provider provider = HolderLookup.Provider.create(Stream.concat(stream, stream2.peek(buildState::addOwner)));
		buildState.reportRemainingUnreferencedValues();
		buildState.throwOnError();
		return provider;
	}

	public HolderLookup.Provider buildPatch(RegistryAccess registryAccess, HolderLookup.Provider provider) {
		RegistrySetBuilder.BuildState buildState = this.createState(registryAccess);
		Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryContents<?>> map = new HashMap();
		buildState.collectReferencedRegistries().forEach(registryContents -> map.put(registryContents.key, registryContents));
		this.entries
			.stream()
			.map(registryStub -> registryStub.collectChanges(buildState))
			.forEach(registryContents -> map.put(registryContents.key, registryContents));
		Stream<HolderLookup.RegistryLookup<?>> stream = registryAccess.registries().map(registryEntry -> registryEntry.value().asLookup());
		HolderLookup.Provider provider2 = HolderLookup.Provider.create(
			Stream.concat(stream, map.values().stream().map(RegistrySetBuilder.RegistryContents::buildAsLookup).peek(buildState::addOwner))
		);
		buildState.fillMissingHolders(provider);
		buildState.reportRemainingUnreferencedValues();
		buildState.throwOnError();
		return provider2;
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

		public <T> BootstapContext<T> bootstapContext() {
			return new BootstapContext<T>() {
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

		public void reportRemainingUnreferencedValues() {
			for (ResourceKey<Object> resourceKey : this.lookup.holders.keySet()) {
				this.errors.add(new IllegalStateException("Unreferenced key: " + resourceKey));
			}

			this.registeredValues
				.forEach(
					(resourceKeyx, registeredValue) -> this.errors.add(new IllegalStateException("Orpaned value " + registeredValue.value + " for key " + resourceKeyx))
				);
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

		public void addOwner(HolderOwner<?> holderOwner) {
			this.owner.add(holderOwner);
		}

		public void fillMissingHolders(HolderLookup.Provider provider) {
			Map<ResourceLocation, Optional<? extends HolderLookup<Object>>> map = new HashMap();
			Iterator<Entry<ResourceKey<Object>, Holder.Reference<Object>>> iterator = this.lookup.holders.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<ResourceKey<Object>, Holder.Reference<Object>> entry = (Entry<ResourceKey<Object>, Holder.Reference<Object>>)iterator.next();
				ResourceKey<Object> resourceKey = (ResourceKey<Object>)entry.getKey();
				Holder.Reference<Object> reference = (Holder.Reference<Object>)entry.getValue();
				((Optional)map.computeIfAbsent(resourceKey.registry(), resourceLocation -> provider.lookup(ResourceKey.createRegistryKey(resourceLocation))))
					.flatMap(holderLookup -> holderLookup.get(resourceKey))
					.ifPresent(reference2 -> {
						reference.bindValue(reference2.value());
						iterator.remove();
					});
			}
		}

		public Stream<RegistrySetBuilder.RegistryContents<?>> collectReferencedRegistries() {
			return this.lookup
				.holders
				.keySet()
				.stream()
				.map(ResourceKey::registry)
				.distinct()
				.map(resourceLocation -> new RegistrySetBuilder.RegistryContents(ResourceKey.createRegistryKey(resourceLocation), Lifecycle.stable(), Map.of()));
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

	static record RegisteredValue<T>(T value, Lifecycle lifecycle) {
	}

	@FunctionalInterface
	public interface RegistryBootstrap<T> {
		void run(BootstapContext<T> bootstapContext);
	}

	static record RegistryContents<T>(
		ResourceKey<? extends Registry<? extends T>> key, Lifecycle lifecycle, Map<ResourceKey<T>, RegistrySetBuilder.ValueAndHolder<T>> values
	) {

		public HolderLookup.RegistryLookup<T> buildAsLookup() {
			return new HolderLookup.RegistryLookup<T>() {
				private final Map<ResourceKey<T>, Holder.Reference<T>> entries = (Map<ResourceKey<T>, Holder.Reference<T>>)RegistryContents.this.values
					.entrySet()
					.stream()
					.collect(
						Collectors.toUnmodifiableMap(
							Entry::getKey,
							entry -> {
								RegistrySetBuilder.ValueAndHolder<T> valueAndHolder = (RegistrySetBuilder.ValueAndHolder<T>)entry.getValue();
								Holder.Reference<T> reference = (Holder.Reference<T>)valueAndHolder.holder()
									.orElseGet(() -> Holder.Reference.createStandAlone(this, (ResourceKey<T>)entry.getKey()));
								reference.bindValue(valueAndHolder.value().value());
								return reference;
							}
						)
					);

				@Override
				public ResourceKey<? extends Registry<? extends T>> key() {
					return RegistryContents.this.key;
				}

				@Override
				public Lifecycle registryLifecycle() {
					return RegistryContents.this.lifecycle;
				}

				@Override
				public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
					return Optional.ofNullable((Holder.Reference)this.entries.get(resourceKey));
				}

				@Override
				public Stream<Holder.Reference<T>> listElements() {
					return this.entries.values().stream();
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
	}

	static record RegistryStub<T>(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, RegistrySetBuilder.RegistryBootstrap<T> bootstrap) {
		void apply(RegistrySetBuilder.BuildState buildState) {
			this.bootstrap.run(buildState.bootstapContext());
		}

		public RegistrySetBuilder.RegistryContents<T> collectChanges(RegistrySetBuilder.BuildState buildState) {
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
