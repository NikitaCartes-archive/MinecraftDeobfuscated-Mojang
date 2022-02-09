package net.minecraft.resources;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T> extends DelegatingOps<T> {
	private final Optional<RegistryLoader.Bound> loader;
	private final RegistryAccess registryAccess;
	private final DynamicOps<JsonElement> asJson;

	public static <T> RegistryOps<T> create(DynamicOps<T> dynamicOps, RegistryAccess registryAccess) {
		return new RegistryOps<>(dynamicOps, registryAccess, Optional.empty());
	}

	public static <T> RegistryOps<T> createAndLoad(DynamicOps<T> dynamicOps, RegistryAccess.Writable writable, ResourceManager resourceManager) {
		return createAndLoad(dynamicOps, writable, RegistryResourceAccess.forResourceManager(resourceManager));
	}

	public static <T> RegistryOps<T> createAndLoad(DynamicOps<T> dynamicOps, RegistryAccess.Writable writable, RegistryResourceAccess registryResourceAccess) {
		RegistryLoader registryLoader = new RegistryLoader(registryResourceAccess);
		RegistryOps<T> registryOps = new RegistryOps<>(dynamicOps, writable, Optional.of(registryLoader.bind(writable)));
		RegistryAccess.load(writable, registryOps.getAsJson(), registryLoader);
		return registryOps;
	}

	private RegistryOps(DynamicOps<T> dynamicOps, RegistryAccess registryAccess, Optional<RegistryLoader.Bound> optional) {
		super(dynamicOps);
		this.loader = optional;
		this.registryAccess = registryAccess;
		this.asJson = dynamicOps == JsonOps.INSTANCE ? this : new RegistryOps<>(JsonOps.INSTANCE, registryAccess, optional);
	}

	public <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
		return this.registryAccess.registry(resourceKey);
	}

	public Optional<RegistryLoader.Bound> registryLoader() {
		return this.loader;
	}

	public DynamicOps<JsonElement> getAsJson() {
		return this.asJson;
	}

	public static <E> MapCodec<Registry<E>> retrieveRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
		return ExtraCodecs.retrieveContext(
			dynamicOps -> dynamicOps instanceof RegistryOps<?> registryOps
					? (DataResult)registryOps.registry(resourceKey)
						.map(registry -> DataResult.success(registry, registry.elementsLifecycle()))
						.orElseGet(() -> DataResult.error("Unknown registry: " + resourceKey))
					: DataResult.error("Not a registry ops")
		);
	}
}
