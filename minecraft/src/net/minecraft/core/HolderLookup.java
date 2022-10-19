package net.minecraft.core;

import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;

public interface HolderLookup<T> {
	Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey);

	Stream<ResourceKey<T>> listElements();

	Optional<HolderSet.Named<T>> get(TagKey<T> tagKey);

	Stream<TagKey<T>> listTags();

	static <T> HolderLookup.RegistryLookup<T> forRegistry(Registry<T> registry) {
		return new HolderLookup.RegistryLookup<>(registry);
	}

	public static class RegistryLookup<T> implements HolderLookup<T> {
		protected final Registry<T> registry;

		public RegistryLookup(Registry<T> registry) {
			this.registry = registry;
		}

		@Override
		public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
			return this.registry.getHolder(resourceKey);
		}

		@Override
		public Stream<ResourceKey<T>> listElements() {
			return this.registry.entrySet().stream().map(Entry::getKey);
		}

		@Override
		public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
			return this.registry.getTag(tagKey);
		}

		@Override
		public Stream<TagKey<T>> listTags() {
			return this.registry.getTagNames();
		}

		public HolderLookup<T> filterElements(Predicate<T> predicate) {
			return new HolderLookup<T>() {
				@Override
				public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
					return RegistryLookup.this.registry.getHolder(resourceKey).filter(reference -> predicate.test(reference.value()));
				}

				@Override
				public Stream<ResourceKey<T>> listElements() {
					return RegistryLookup.this.registry.entrySet().stream().filter(entry -> predicate.test(entry.getValue())).map(Entry::getKey);
				}

				@Override
				public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
					return RegistryLookup.this.get(tagKey);
				}

				@Override
				public Stream<TagKey<T>> listTags() {
					return RegistryLookup.this.listTags();
				}
			};
		}

		public HolderLookup<T> filterFeatures(FeatureFlagSet featureFlagSet) {
			return (HolderLookup<T>)(FeatureElement.FILTERED_REGISTRIES.contains(this.registry.key())
				? this.filterElements(object -> ((FeatureElement)object).isEnabled(featureFlagSet))
				: this);
		}
	}
}
