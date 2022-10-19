/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;

public interface HolderLookup<T> {
    public Optional<Holder.Reference<T>> get(ResourceKey<T> var1);

    public Stream<ResourceKey<T>> listElements();

    public Optional<HolderSet.Named<T>> get(TagKey<T> var1);

    public Stream<TagKey<T>> listTags();

    public static <T> RegistryLookup<T> forRegistry(Registry<T> registry) {
        return new RegistryLookup<T>(registry);
    }

    public static class RegistryLookup<T>
    implements HolderLookup<T> {
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
            return this.registry.entrySet().stream().map(Map.Entry::getKey);
        }

        @Override
        public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
            return this.registry.getTag(tagKey);
        }

        @Override
        public Stream<TagKey<T>> listTags() {
            return this.registry.getTagNames();
        }

        public HolderLookup<T> filterElements(final Predicate<T> predicate) {
            return new HolderLookup<T>(){

                @Override
                public Optional<Holder.Reference<T>> get(ResourceKey<T> resourceKey) {
                    return registry.getHolder(resourceKey).filter(reference -> predicate.test(reference.value()));
                }

                @Override
                public Stream<ResourceKey<T>> listElements() {
                    return registry.entrySet().stream().filter(entry -> predicate.test(entry.getValue())).map(Map.Entry::getKey);
                }

                @Override
                public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
                    return this.get(tagKey);
                }

                @Override
                public Stream<TagKey<T>> listTags() {
                    return this.listTags();
                }
            };
        }

        public HolderLookup<T> filterFeatures(FeatureFlagSet featureFlagSet) {
            if (FeatureElement.FILTERED_REGISTRIES.contains(this.registry.key())) {
                return this.filterElements(object -> ((FeatureElement)object).isEnabled(featureFlagSet));
            }
            return this;
        }
    }
}

