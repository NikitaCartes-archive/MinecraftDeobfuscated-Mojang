package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;

public class RegistryCodecs {
	public static <E> Codec<Registry<E>> fullCodec(ResourceKey<? extends Registry<E>> resourceKey, Lifecycle lifecycle, Codec<E> codec) {
		Codec<Map<ResourceKey<E>, E>> codec2 = Codec.unboundedMap(ResourceKey.codec(resourceKey), codec);
		return codec2.xmap(map -> {
			WritableRegistry<E> writableRegistry = new MappedRegistry<>(resourceKey, lifecycle);
			map.forEach((resourceKeyxx, object) -> writableRegistry.register(resourceKeyxx, (E)object, lifecycle));
			return writableRegistry.freeze();
		}, registry -> ImmutableMap.copyOf(registry.entrySet()));
	}

	public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
		return homogeneousList(resourceKey, codec, false);
	}

	public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, boolean bl) {
		return HolderSetCodec.create(resourceKey, RegistryFileCodec.create(resourceKey, codec), bl);
	}

	public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourceKey) {
		return homogeneousList(resourceKey, false);
	}

	public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourceKey, boolean bl) {
		return HolderSetCodec.create(resourceKey, RegistryFixedCodec.create(resourceKey), bl);
	}
}
