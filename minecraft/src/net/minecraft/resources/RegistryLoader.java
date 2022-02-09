package net.minecraft.resources;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;

public class RegistryLoader {
	private final RegistryResourceAccess resources;
	private final Map<ResourceKey<? extends Registry<?>>, RegistryLoader.ReadCache<?>> readCache = new IdentityHashMap();

	RegistryLoader(RegistryResourceAccess registryResourceAccess) {
		this.resources = registryResourceAccess;
	}

	public <E> DataResult<? extends Registry<E>> overrideRegistryFromResources(
		WritableRegistry<E> writableRegistry, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, DynamicOps<JsonElement> dynamicOps
	) {
		Collection<ResourceKey<E>> collection = this.resources.listResources(resourceKey);
		DataResult<WritableRegistry<E>> dataResult = DataResult.success(writableRegistry, Lifecycle.stable());

		for (ResourceKey<E> resourceKey2 : collection) {
			dataResult = dataResult.flatMap(
				writableRegistryx -> this.overrideElementFromResources(writableRegistryx, resourceKey, codec, resourceKey2, dynamicOps).map(holder -> writableRegistryx)
			);
		}

		return dataResult.setPartial(writableRegistry);
	}

	<E> DataResult<Holder<E>> overrideElementFromResources(
		WritableRegistry<E> writableRegistry,
		ResourceKey<? extends Registry<E>> resourceKey,
		Codec<E> codec,
		ResourceKey<E> resourceKey2,
		DynamicOps<JsonElement> dynamicOps
	) {
		RegistryLoader.ReadCache<E> readCache = this.readCache(resourceKey);
		DataResult<Holder<E>> dataResult = (DataResult<Holder<E>>)readCache.values.get(resourceKey2);
		if (dataResult != null) {
			return dataResult;
		} else {
			Holder<E> holder = writableRegistry.getOrCreateHolder(resourceKey2);
			readCache.values.put(resourceKey2, DataResult.success(holder));
			Optional<DataResult<RegistryResourceAccess.ParsedEntry<E>>> optional = this.resources.parseElement(dynamicOps, resourceKey, resourceKey2, codec);
			DataResult<Holder<E>> dataResult2;
			if (optional.isEmpty()) {
				if (writableRegistry.containsKey(resourceKey2)) {
					dataResult2 = DataResult.success(holder, Lifecycle.stable());
				} else {
					dataResult2 = DataResult.error("Missing referenced custom/removed registry entry for registry " + resourceKey + " named " + resourceKey2.location());
				}
			} else {
				DataResult<RegistryResourceAccess.ParsedEntry<E>> dataResult3 = (DataResult<RegistryResourceAccess.ParsedEntry<E>>)optional.get();
				Optional<RegistryResourceAccess.ParsedEntry<E>> optional2 = dataResult3.result();
				if (optional2.isPresent()) {
					RegistryResourceAccess.ParsedEntry<E> parsedEntry = (RegistryResourceAccess.ParsedEntry<E>)optional2.get();
					writableRegistry.registerOrOverride(parsedEntry.fixedId(), resourceKey2, parsedEntry.value(), dataResult3.lifecycle());
				}

				dataResult2 = dataResult3.map(parsedEntryx -> holder);
			}

			readCache.values.put(resourceKey2, dataResult2);
			return dataResult2;
		}
	}

	private <E> RegistryLoader.ReadCache<E> readCache(ResourceKey<? extends Registry<E>> resourceKey) {
		return (RegistryLoader.ReadCache<E>)this.readCache.computeIfAbsent(resourceKey, resourceKeyx -> new RegistryLoader.ReadCache());
	}

	public RegistryLoader.Bound bind(RegistryAccess.Writable writable) {
		return new RegistryLoader.Bound(writable, this);
	}

	public static record Bound(RegistryAccess.Writable access, RegistryLoader loader) {
		public <E> DataResult<? extends Registry<E>> overrideRegistryFromResources(
			ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, DynamicOps<JsonElement> dynamicOps
		) {
			WritableRegistry<E> writableRegistry = this.access.ownedWritableRegistryOrThrow(resourceKey);
			return this.loader.overrideRegistryFromResources(writableRegistry, resourceKey, codec, dynamicOps);
		}

		public <E> DataResult<Holder<E>> overrideElementFromResources(
			ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, ResourceKey<E> resourceKey2, DynamicOps<JsonElement> dynamicOps
		) {
			WritableRegistry<E> writableRegistry = this.access.ownedWritableRegistryOrThrow(resourceKey);
			return this.loader.overrideElementFromResources(writableRegistry, resourceKey, codec, resourceKey2, dynamicOps);
		}
	}

	static final class ReadCache<E> {
		final Map<ResourceKey<E>, DataResult<Holder<E>>> values = Maps.<ResourceKey<E>, DataResult<Holder<E>>>newIdentityHashMap();
	}
}
