/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public interface RegistryAccess
extends HolderLookup.Provider {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Frozen EMPTY = new ImmutableRegistryAccess(Map.of()).freeze();

    public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> var1);

    @Override
    default public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
        return this.registry(resourceKey).map(Registry::asLookup);
    }

    default public <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return this.registry(resourceKey).orElseThrow(() -> new IllegalStateException("Missing registry: " + resourceKey));
    }

    public Stream<RegistryEntry<?>> registries();

    public static Frozen fromRegistryOfRegistries(final Registry<? extends Registry<?>> registry) {
        return new Frozen(){

            public <T> Optional<Registry<T>> registry(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                Registry registry2 = registry;
                return registry2.getOptional(resourceKey);
            }

            @Override
            public Stream<RegistryEntry<?>> registries() {
                return registry.entrySet().stream().map(RegistryEntry::fromMapEntry);
            }

            @Override
            public Frozen freeze() {
                return this;
            }
        };
    }

    default public Frozen freeze() {
        class FrozenAccess
        extends ImmutableRegistryAccess
        implements Frozen {
            protected FrozenAccess(Stream<RegistryEntry<?>> stream) {
                super(stream);
            }
        }
        return new FrozenAccess(this.registries().map(RegistryEntry::freeze));
    }

    default public Lifecycle allRegistriesLifecycle() {
        return this.registries().map(registryEntry -> registryEntry.value.registryLifecycle()).reduce(Lifecycle.stable(), Lifecycle::add);
    }

    public record RegistryEntry<T>(ResourceKey<? extends Registry<T>> key, Registry<T> value) {
        private static <T, R extends Registry<? extends T>> RegistryEntry<T> fromMapEntry(Map.Entry<? extends ResourceKey<? extends Registry<?>>, R> entry) {
            return RegistryEntry.fromUntyped(entry.getKey(), (Registry)entry.getValue());
        }

        private static <T> RegistryEntry<T> fromUntyped(ResourceKey<? extends Registry<?>> resourceKey, Registry<?> registry) {
            return new RegistryEntry(resourceKey, registry);
        }

        private RegistryEntry<T> freeze() {
            return new RegistryEntry<T>(this.key, this.value.freeze());
        }
    }

    public static class ImmutableRegistryAccess
    implements RegistryAccess {
        private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> registries;

        public ImmutableRegistryAccess(List<? extends Registry<?>> list) {
            this.registries = list.stream().collect(Collectors.toUnmodifiableMap(Registry::key, registry -> registry));
        }

        public ImmutableRegistryAccess(Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> map) {
            this.registries = Map.copyOf(map);
        }

        public ImmutableRegistryAccess(Stream<RegistryEntry<?>> stream) {
            this.registries = stream.collect(ImmutableMap.toImmutableMap(RegistryEntry::key, RegistryEntry::value));
        }

        @Override
        public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
            return Optional.ofNullable(this.registries.get(resourceKey)).map(registry -> registry);
        }

        @Override
        public Stream<RegistryEntry<?>> registries() {
            return this.registries.entrySet().stream().map(RegistryEntry::fromMapEntry);
        }
    }

    public static interface Frozen
    extends RegistryAccess {
    }
}

