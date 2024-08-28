package net.minecraft.core;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;

public interface HolderLookup<T> extends HolderGetter<T> {
	Stream<Holder.Reference<T>> listElements();

	default Stream<ResourceKey<T>> listElementIds() {
		return this.listElements().map(Holder.Reference::key);
	}

	Stream<HolderSet.Named<T>> listTags();

	default Stream<TagKey<T>> listTagIds() {
		return this.listTags().map(HolderSet.Named::key);
	}

	public interface Provider extends HolderGetter.Provider {
		Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys();

		default Stream<HolderLookup.RegistryLookup<?>> listRegistries() {
			return this.listRegistryKeys().map(this::lookupOrThrow);
		}

		@Override
		<T> Optional<? extends HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey);

		default <T> HolderLookup.RegistryLookup<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> resourceKey) {
			return (HolderLookup.RegistryLookup<T>)this.lookup(resourceKey)
				.orElseThrow(() -> new IllegalStateException("Registry " + resourceKey.location() + " not found"));
		}

		default <V> RegistryOps<V> createSerializationContext(DynamicOps<V> dynamicOps) {
			return RegistryOps.create(dynamicOps, this);
		}

		static HolderLookup.Provider create(Stream<HolderLookup.RegistryLookup<?>> stream) {
			final Map<ResourceKey<? extends Registry<?>>, HolderLookup.RegistryLookup<?>> map = (Map<ResourceKey<? extends Registry<?>>, HolderLookup.RegistryLookup<?>>)stream.collect(
				Collectors.toUnmodifiableMap(HolderLookup.RegistryLookup::key, registryLookup -> registryLookup)
			);
			return new HolderLookup.Provider() {
				@Override
				public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
					return map.keySet().stream();
				}

				@Override
				public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
					return Optional.ofNullable((HolderLookup.RegistryLookup)map.get(resourceKey));
				}
			};
		}

		default Lifecycle allRegistriesLifecycle() {
			return (Lifecycle)this.listRegistries().map(HolderLookup.RegistryLookup::registryLifecycle).reduce(Lifecycle.stable(), Lifecycle::add);
		}
	}

	public interface RegistryLookup<T> extends HolderLookup<T>, HolderOwner<T> {
		ResourceKey<? extends Registry<? extends T>> key();

		Lifecycle registryLifecycle();

		default HolderLookup.RegistryLookup<T> filterFeatures(FeatureFlagSet featureFlagSet) {
			return FeatureElement.FILTERED_REGISTRIES.contains(this.key()) ? this.filterElements(object -> ((FeatureElement)object).isEnabled(featureFlagSet)) : this;
		}

		default HolderLookup.RegistryLookup<T> filterElements(Predicate<T> predicate) {
			return new HolderLookup.RegistryLookup.Delegate<T>() {
				@Override
				public HolderLookup.RegistryLookup<T> parent() {
					return RegistryLookup.this;
				}

				@Override
				public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
					return this.parent().get(resourceKey).filter(reference -> predicate.test(reference.value()));
				}

				@Override
				public Stream<Holder.Reference<T>> listElements() {
					return this.parent().listElements().filter(reference -> predicate.test(reference.value()));
				}
			};
		}

		public interface Delegate<T> extends HolderLookup.RegistryLookup<T> {
			HolderLookup.RegistryLookup<T> parent();

			@Override
			default ResourceKey<? extends Registry<? extends T>> key() {
				return this.parent().key();
			}

			@Override
			default Lifecycle registryLifecycle() {
				return this.parent().registryLifecycle();
			}

			@Override
			default Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
				return this.parent().get(resourceKey);
			}

			@Override
			default Stream<Holder.Reference<T>> listElements() {
				return this.parent().listElements();
			}

			@Override
			default Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
				return this.parent().get(tagKey);
			}

			@Override
			default Stream<HolderSet.Named<T>> listTags() {
				return this.parent().listTags();
			}
		}
	}
}
