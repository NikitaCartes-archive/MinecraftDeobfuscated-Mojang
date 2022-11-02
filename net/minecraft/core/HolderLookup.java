/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;

public interface HolderLookup<T>
extends HolderGetter<T> {
    public Stream<Holder.Reference<T>> listElements();

    default public Stream<ResourceKey<T>> listElementIds() {
        return this.listElements().map(Holder.Reference::key);
    }

    public Stream<HolderSet.Named<T>> listTags();

    default public Stream<TagKey<T>> listTagIds() {
        return this.listTags().map(HolderSet.Named::key);
    }

    default public HolderLookup<T> filterElements(final Predicate<T> predicate) {
        return new Delegate<T>(this){

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

    public static interface Provider {
        public <T> Optional<RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> var1);

        default public <T> RegistryLookup<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> resourceKey) {
            return this.lookup(resourceKey).orElseThrow(() -> new IllegalStateException("Registry " + resourceKey.location() + " not found"));
        }

        default public HolderGetter.Provider asGetterLookup() {
            return new HolderGetter.Provider(){

                @Override
                public <T> Optional<HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                    return this.lookup(resourceKey).map(registryLookup -> registryLookup);
                }
            };
        }

        public static Provider create(Stream<RegistryLookup<?>> stream) {
            final Map<ResourceKey, RegistryLookup> map = stream.collect(Collectors.toUnmodifiableMap(RegistryLookup::key, registryLookup -> registryLookup));
            return new Provider(){

                @Override
                public <T> Optional<RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                    return Optional.ofNullable((RegistryLookup)map.get(resourceKey));
                }
            };
        }
    }

    public static class Delegate<T>
    implements HolderLookup<T> {
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

    public static interface RegistryLookup<T>
    extends HolderLookup<T>,
    HolderOwner<T> {
        public ResourceKey<? extends Registry<? extends T>> key();

        public Lifecycle elementsLifecycle();

        default public HolderLookup<T> filterFeatures(FeatureFlagSet featureFlagSet) {
            if (FeatureElement.FILTERED_REGISTRIES.contains(this.key())) {
                return this.filterElements(object -> ((FeatureElement)object).isEnabled(featureFlagSet));
            }
            return this;
        }

        public static abstract class Delegate<T>
        implements RegistryLookup<T> {
            protected abstract RegistryLookup<T> parent();

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

