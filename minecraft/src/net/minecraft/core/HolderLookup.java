package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

	default HolderLookup<T> filterElements(Predicate<T> predicate) {
		return new HolderLookup.Delegate<T>(this) {
			@Override
			public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
				return this.parent.get(resourceKey).filter(reference -> predicate.test(reference.value()));
			}

			@Override
			public Stream<Holder.Reference<T>> listElements() {
				return this.parent.listElements().filter(reference -> predicate.test(reference.value()));
			}
		};
	}

	public static class Delegate<T> implements HolderLookup<T> {
		protected final HolderLookup<T> parent;

		public Delegate(HolderLookup<T> holderLookup) {
			this.parent = holderLookup;
		}

		@Override
		public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
			return this.parent.get(resourceKey);
		}

		@Override
		public Stream<Holder.Reference<T>> listElements() {
			return this.parent.listElements();
		}

		@Override
		public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
			return this.parent.get(tagKey);
		}

		@Override
		public Stream<HolderSet.Named<T>> listTags() {
			return this.parent.listTags();
		}
	}

	public interface Provider {
		<T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey);

		default <T> HolderLookup.RegistryLookup<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> resourceKey) {
			return (HolderLookup.RegistryLookup<T>)this.lookup(resourceKey)
				.orElseThrow(() -> new IllegalStateException("Registry " + resourceKey.location() + " not found"));
		}

		default HolderGetter.Provider asGetterLookup() {
			return new HolderGetter.Provider() {
				@Override
				public <T> Optional<HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
					return Provider.this.lookup(resourceKey).map(registryLookup -> registryLookup);
				}
			};
		}

		static HolderLookup.Provider create(Stream<HolderLookup.RegistryLookup<?>> stream) {
			final Map<ResourceKey<? extends Registry<?>>, HolderLookup.RegistryLookup<?>> map = (Map<ResourceKey<? extends Registry<?>>, HolderLookup.RegistryLookup<?>>)stream.collect(
				Collectors.toUnmodifiableMap(HolderLookup.RegistryLookup::key, registryLookup -> registryLookup)
			);
			return new HolderLookup.Provider() {
				@Override
				public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
					return Optional.ofNullable((HolderLookup.RegistryLookup)map.get(resourceKey));
				}
			};
		}
	}

	public interface RegistryLookup<T> extends HolderLookup<T>, HolderOwner<T> {
		ResourceKey<? extends Registry<? extends T>> key();

		Lifecycle elementsLifecycle();

		default HolderLookup<T> filterFeatures(FeatureFlagSet featureFlagSet) {
			return (HolderLookup<T>)(FeatureElement.FILTERED_REGISTRIES.contains(this.key())
				? this.filterElements(object -> ((FeatureElement)object).isEnabled(featureFlagSet))
				: this);
		}

		public abstract static class Delegate<T> implements HolderLookup.RegistryLookup<T> {
			protected abstract HolderLookup.RegistryLookup<T> parent();

			@Override
			public ResourceKey<? extends Registry<? extends T>> key() {
				return this.parent().key();
			}

			@Override
			public Lifecycle elementsLifecycle() {
				return this.parent().elementsLifecycle();
			}

			@Override
			public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
				return this.parent().get(resourceKey);
			}

			@Override
			public Stream<Holder.Reference<T>> listElements() {
				return this.parent().listElements();
			}

			@Override
			public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
				return this.parent().get(tagKey);
			}

			@Override
			public Stream<HolderSet.Named<T>> listTags() {
				return this.parent().listTags();
			}
		}
	}
}
