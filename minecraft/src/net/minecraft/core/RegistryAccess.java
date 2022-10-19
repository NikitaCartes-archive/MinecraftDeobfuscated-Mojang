package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public interface RegistryAccess {
	Logger LOGGER = LogUtils.getLogger();
	RegistryAccess.Frozen EMPTY = new RegistryAccess.ImmutableRegistryAccess(Map.of()).freeze();

	<E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> resourceKey);

	default <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> resourceKey) {
		return (Registry<E>)this.registry(resourceKey).orElseThrow(() -> new IllegalStateException("Missing registry: " + resourceKey));
	}

	Stream<RegistryAccess.RegistryEntry<?>> registries();

	static RegistryAccess.Frozen fromRegistryOfRegistries(Registry<? extends Registry<?>> registry) {
		return new RegistryAccess.Frozen() {
			@Override
			public <T> Optional<Registry<T>> registry(ResourceKey<? extends Registry<? extends T>> resourceKey) {
				Registry<Registry<T>> registry = (Registry<Registry<T>>)registry;
				return registry.getOptional((ResourceKey<Registry<T>>)resourceKey);
			}

			@Override
			public Stream<RegistryAccess.RegistryEntry<?>> registries() {
				return registry.entrySet().stream().map(RegistryAccess.RegistryEntry::fromMapEntry);
			}

			@Override
			public RegistryAccess.Frozen freeze() {
				return this;
			}
		};
	}

	default RegistryAccess.Frozen freeze() {
		class FrozenAccess extends RegistryAccess.ImmutableRegistryAccess implements RegistryAccess.Frozen {
			protected FrozenAccess(Stream<RegistryAccess.RegistryEntry<?>> stream) {
				super(stream);
			}
		}

		return new FrozenAccess(this.registries().map(RegistryAccess.RegistryEntry::freeze));
	}

	default Lifecycle allElementsLifecycle() {
		return (Lifecycle)this.registries().map(registryEntry -> registryEntry.value.elementsLifecycle()).reduce(Lifecycle.stable(), Lifecycle::add);
	}

	public interface Frozen extends RegistryAccess {
	}

	public static class ImmutableRegistryAccess implements RegistryAccess {
		private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> registries;

		public ImmutableRegistryAccess(List<? extends Registry<?>> list) {
			this.registries = (Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>>)list.stream()
				.collect(Collectors.toUnmodifiableMap(Registry::key, registry -> registry));
		}

		public ImmutableRegistryAccess(Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> map) {
			this.registries = Map.copyOf(map);
		}

		public ImmutableRegistryAccess(Stream<RegistryAccess.RegistryEntry<?>> stream) {
			this.registries = (Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>>)stream.collect(
				ImmutableMap.toImmutableMap(RegistryAccess.RegistryEntry::key, RegistryAccess.RegistryEntry::value)
			);
		}

		@Override
		public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
			return Optional.ofNullable((Registry)this.registries.get(resourceKey)).map(registry -> registry);
		}

		@Override
		public Stream<RegistryAccess.RegistryEntry<?>> registries() {
			return this.registries.entrySet().stream().map(RegistryAccess.RegistryEntry::fromMapEntry);
		}
	}

	public static record RegistryEntry<T>(ResourceKey<? extends Registry<T>> key, Registry<T> value) {

		private static <T, R extends Registry<? extends T>> RegistryAccess.RegistryEntry<T> fromMapEntry(Entry<? extends ResourceKey<? extends Registry<?>>, R> entry) {
			return fromUntyped((ResourceKey<? extends Registry<?>>)entry.getKey(), (Registry<?>)entry.getValue());
		}

		private static <T> RegistryAccess.RegistryEntry<T> fromUntyped(ResourceKey<? extends Registry<?>> resourceKey, Registry<?> registry) {
			return new RegistryAccess.RegistryEntry<>((ResourceKey<? extends Registry<T>>)resourceKey, (Registry<T>)registry);
		}

		private RegistryAccess.RegistryEntry<T> freeze() {
			return new RegistryAccess.RegistryEntry<>(this.key, this.value.freeze());
		}
	}
}
