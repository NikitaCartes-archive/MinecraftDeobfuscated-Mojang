package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T> extends DelegatingOps<T> {
	private final RegistryAccess registryAccess;

	public static <T> RegistryOps<T> create(DynamicOps<T> dynamicOps, RegistryAccess registryAccess) {
		return new RegistryOps<>(dynamicOps, registryAccess);
	}

	private RegistryOps(DynamicOps<T> dynamicOps, RegistryAccess registryAccess) {
		super(dynamicOps);
		this.registryAccess = registryAccess;
	}

	public <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
		return this.registryAccess.registry(resourceKey);
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
