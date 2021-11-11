package net.minecraft.resources;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class ResourceKey<T> {
	private static final Map<String, ResourceKey<?>> VALUES = Collections.synchronizedMap(Maps.newIdentityHashMap());
	private final ResourceLocation registryName;
	private final ResourceLocation location;

	public static <T> Codec<ResourceKey<T>> codec(ResourceKey<? extends Registry<T>> resourceKey) {
		return ResourceLocation.CODEC.xmap(resourceLocation -> create(resourceKey, resourceLocation), ResourceKey::location);
	}

	public static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> resourceKey, ResourceLocation resourceLocation) {
		return create(resourceKey.location, resourceLocation);
	}

	public static <T> ResourceKey<Registry<T>> createRegistryKey(ResourceLocation resourceLocation) {
		return create(Registry.ROOT_REGISTRY_NAME, resourceLocation);
	}

	private static <T> ResourceKey<T> create(ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		String string = (resourceLocation + ":" + resourceLocation2).intern();
		return (ResourceKey<T>)VALUES.computeIfAbsent(string, stringx -> new ResourceKey(resourceLocation, resourceLocation2));
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

	public static <T> Function<ResourceLocation, ResourceKey<T>> elementKey(ResourceKey<? extends Registry<T>> resourceKey) {
		return resourceLocation -> create(resourceKey, resourceLocation);
	}
}
