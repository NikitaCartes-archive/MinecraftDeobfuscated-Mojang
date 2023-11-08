package net.minecraft.resources;

import com.google.common.collect.MapMaker;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;

public class ResourceKey<T> {
	private static final ConcurrentMap<ResourceKey.InternKey, ResourceKey<?>> VALUES = new MapMaker().weakValues().makeMap();
	private final ResourceLocation registryName;
	private final ResourceLocation location;

	public static <T> Codec<ResourceKey<T>> codec(ResourceKey<? extends Registry<T>> resourceKey) {
		return ResourceLocation.CODEC.xmap(resourceLocation -> create(resourceKey, resourceLocation), ResourceKey::location);
	}

	public static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> resourceKey, ResourceLocation resourceLocation) {
		return create(resourceKey.location, resourceLocation);
	}

	public static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation resourceLocation) {
		return create(Registries.ROOT_REGISTRY_NAME, resourceLocation);
	}

	private static <T> ResourceKey<T> create(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		return (ResourceKey<T>)VALUES.computeIfAbsent(
			new ResourceKey.InternKey(resourceLocation, resourceLocation2), internKey -> new ResourceKey(internKey.registry, internKey.location)
		);
	}

	private ResourceKey(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		this.registryName = resourceLocation;
		this.location = resourceLocation2;
	}

	public String toString() {
		return "ResourceKey[" + this.registryName + " / " + this.location + "]";
	}

	public boolean isFor(ResourceKey<? extends Registry<?>> resourceKey) {
		return this.registryName.equals(resourceKey.location());
	}

	public <E> Optional<ResourceKey<E>> cast(ResourceKey<? extends Registry<E>> resourceKey) {
		return this.isFor(resourceKey) ? Optional.of(this) : Optional.empty();
	}

	public ResourceLocation location() {
		return this.location;
	}

	public ResourceLocation registry() {
		return this.registryName;
	}

	static record InternKey(ResourceLocation registry, ResourceLocation location) {
	}
}
