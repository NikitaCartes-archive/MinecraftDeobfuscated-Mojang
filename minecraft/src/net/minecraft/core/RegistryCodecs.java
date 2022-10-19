package net.minecraft.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;

public class RegistryCodecs {
	private static <T> MapCodec<RegistryCodecs.RegistryEntry<T>> withNameAndId(ResourceKey<? extends Registry<T>> resourceKey, MapCodec<T> mapCodec) {
		return RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						ResourceKey.codec(resourceKey).fieldOf("name").forGetter(RegistryCodecs.RegistryEntry::key),
						Codec.INT.fieldOf("id").forGetter(RegistryCodecs.RegistryEntry::id),
						mapCodec.forGetter(RegistryCodecs.RegistryEntry::value)
					)
					.apply(instance, RegistryCodecs.RegistryEntry::new)
		);
	}

	public static <T> Codec<Registry<T>> networkCodec(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, Codec<T> codec) {
		return withNameAndId(resourceKey, codec.fieldOf("element")).codec().listOf().xmap(list -> {
			WritableRegistry<T> writableRegistry = new MappedRegistry<>(resourceKey, lifecycle);

			for (RegistryCodecs.RegistryEntry<T> registryEntry : list) {
				writableRegistry.registerMapping(registryEntry.id(), registryEntry.key(), registryEntry.value(), lifecycle);
			}

			return writableRegistry;
		}, registry -> {
			Builder<RegistryCodecs.RegistryEntry<T>> builder = ImmutableList.builder();

			for (T object : registry) {
				builder.add(new RegistryCodecs.RegistryEntry<>((ResourceKey<T>)registry.getResourceKey(object).get(), registry.getId(object), object));
			}

			return builder.build();
		});
	}

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

	static record RegistryEntry<T>(ResourceKey<T> key, int id, T value) {
	}
}
