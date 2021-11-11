package net.minecraft.resources;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.server.packs.resources.ResourceManager;

public class RegistryReadOps<T> extends DelegatingOps<T> {
	private final RegistryResourceAccess resources;
	private final RegistryAccess registryAccess;
	private final Map<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> readCache;
	private final RegistryReadOps<JsonElement> jsonOps;

	public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess registryAccess) {
		return createAndLoad(dynamicOps, RegistryResourceAccess.forResourceManager(resourceManager), registryAccess);
	}

	public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> dynamicOps, RegistryResourceAccess registryResourceAccess, RegistryAccess registryAccess) {
		RegistryReadOps<T> registryReadOps = new RegistryReadOps<>(dynamicOps, registryResourceAccess, registryAccess, Maps.newIdentityHashMap());
		RegistryAccess.load(registryAccess, registryReadOps);
		return registryReadOps;
	}

	public static <T> RegistryReadOps<T> create(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess registryAccess) {
		return create(dynamicOps, RegistryResourceAccess.forResourceManager(resourceManager), registryAccess);
	}

	public static <T> RegistryReadOps<T> create(DynamicOps<T> dynamicOps, RegistryResourceAccess registryResourceAccess, RegistryAccess registryAccess) {
		return new RegistryReadOps<>(dynamicOps, registryResourceAccess, registryAccess, Maps.newIdentityHashMap());
	}

	private RegistryReadOps(
		DynamicOps<T> dynamicOps,
		RegistryResourceAccess registryResourceAccess,
		RegistryAccess registryAccess,
		IdentityHashMap<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> identityHashMap
	) {
		super(dynamicOps);
		this.resources = registryResourceAccess;
		this.registryAccess = registryAccess;
		this.readCache = identityHashMap;
		this.jsonOps = dynamicOps == JsonOps.INSTANCE ? this : new RegistryReadOps<>(JsonOps.INSTANCE, registryResourceAccess, registryAccess, identityHashMap);
	}

	protected <E> DataResult<Pair<Supplier<E>, T>> decodeElement(T object, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, boolean bl) {
		Optional<WritableRegistry<E>> optional = this.registryAccess.ownedRegistry(resourceKey);
		if (!optional.isPresent()) {
			return DataResult.error("Unknown registry: " + resourceKey);
		} else {
			WritableRegistry<E> writableRegistry = (WritableRegistry<E>)optional.get();
			DataResult<Pair<ResourceLocation, T>> dataResult = ResourceLocation.CODEC.decode(this.delegate, object);
			if (!dataResult.result().isPresent()) {
				return !bl ? DataResult.error("Inline definitions not allowed here") : codec.decode(this, object).map(pairx -> pairx.mapFirst(objectx -> () -> objectx));
			} else {
				Pair<ResourceLocation, T> pair = (Pair<ResourceLocation, T>)dataResult.result().get();
				ResourceKey<E> resourceKey2 = ResourceKey.create(resourceKey, pair.getFirst());
				return this.readAndRegisterElement(resourceKey, writableRegistry, codec, resourceKey2).map(supplier -> Pair.of(supplier, pair.getSecond()));
			}
		}
	}

	public <E> DataResult<MappedRegistry<E>> decodeElements(MappedRegistry<E> mappedRegistry, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
		Collection<ResourceKey<E>> collection = this.resources.listResources(resourceKey);
		DataResult<MappedRegistry<E>> dataResult = DataResult.success(mappedRegistry, Lifecycle.stable());

		for (ResourceKey<E> resourceKey2 : collection) {
			dataResult = dataResult.flatMap(
				mappedRegistryx -> this.readAndRegisterElement(resourceKey, mappedRegistryx, codec, resourceKey2).map(supplier -> mappedRegistryx)
			);
		}

		return dataResult.setPartial(mappedRegistry);
	}

	private <E> DataResult<Supplier<E>> readAndRegisterElement(
		ResourceKey<? extends Registry<E>> resourceKey, WritableRegistry<E> writableRegistry, Codec<E> codec, ResourceKey<E> resourceKey2
	) {
		RegistryReadOps.ReadCache<E> readCache = this.readCache(resourceKey);
		DataResult<Supplier<E>> dataResult = (DataResult<Supplier<E>>)readCache.values.get(resourceKey2);
		if (dataResult != null) {
			return dataResult;
		} else {
			readCache.values.put(resourceKey2, DataResult.success(createPlaceholderGetter(writableRegistry, resourceKey2)));
			Optional<DataResult<RegistryResourceAccess.ParsedEntry<E>>> optional = this.resources.parseElement(this.jsonOps, resourceKey, resourceKey2, codec);
			DataResult<Supplier<E>> dataResult2;
			if (optional.isEmpty()) {
				dataResult2 = DataResult.success(createRegistryGetter(writableRegistry, resourceKey2), Lifecycle.stable());
			} else {
				DataResult<RegistryResourceAccess.ParsedEntry<E>> dataResult3 = (DataResult<RegistryResourceAccess.ParsedEntry<E>>)optional.get();
				Optional<RegistryResourceAccess.ParsedEntry<E>> optional2 = dataResult3.result();
				if (optional2.isPresent()) {
					RegistryResourceAccess.ParsedEntry<E> parsedEntry = (RegistryResourceAccess.ParsedEntry<E>)optional2.get();
					writableRegistry.registerOrOverride(parsedEntry.fixedId(), resourceKey2, parsedEntry.value(), dataResult3.lifecycle());
				}

				dataResult2 = dataResult3.map(parsedEntryx -> createRegistryGetter(writableRegistry, resourceKey2));
			}

			readCache.values.put(resourceKey2, dataResult2);
			return dataResult2;
		}
	}

	private static <E> Supplier<E> createPlaceholderGetter(WritableRegistry<E> writableRegistry, ResourceKey<E> resourceKey) {
		return Suppliers.memoize(() -> {
			E object = writableRegistry.get(resourceKey);
			if (object == null) {
				throw new RuntimeException("Error during recursive registry parsing, element resolved too early: " + resourceKey);
			} else {
				return (T)object;
			}
		});
	}

	private static <E> Supplier<E> createRegistryGetter(Registry<E> registry, ResourceKey<E> resourceKey) {
		return new Supplier<E>() {
			public E get() {
				return registry.get(resourceKey);
			}

			public String toString() {
				return resourceKey.toString();
			}
		};
	}

	private <E> RegistryReadOps.ReadCache<E> readCache(ResourceKey<? extends Registry<E>> resourceKey) {
		return (RegistryReadOps.ReadCache<E>)this.readCache.computeIfAbsent(resourceKey, resourceKeyx -> new RegistryReadOps.ReadCache());
	}

	protected <E> DataResult<Registry<E>> registry(ResourceKey<? extends Registry<E>> resourceKey) {
		return (DataResult<Registry<E>>)this.registryAccess
			.ownedRegistry(resourceKey)
			.map(writableRegistry -> DataResult.success(writableRegistry, writableRegistry.elementsLifecycle()))
			.orElseGet(() -> DataResult.error("Unknown registry: " + resourceKey));
	}

	static final class ReadCache<E> {
		final Map<ResourceKey<E>, DataResult<Supplier<E>>> values = Maps.<ResourceKey<E>, DataResult<Supplier<E>>>newIdentityHashMap();
	}
}
