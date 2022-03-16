package net.minecraft.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.RegistryLoader;
import net.minecraft.resources.RegistryOps;
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
			WritableRegistry<T> writableRegistry = new MappedRegistry<>(resourceKey, lifecycle, null);

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

	public static <E> Codec<Registry<E>> dataPackAwareCodec(ResourceKey<? extends Registry<E>> resourceKey, Lifecycle lifecycle, Codec<E> codec) {
		Codec<Map<ResourceKey<E>, E>> codec2 = directCodec(resourceKey, codec);
		Encoder<Registry<E>> encoder = codec2.comap(registry -> ImmutableMap.copyOf(registry.entrySet()));
		return Codec.of(encoder, dataPackAwareDecoder(resourceKey, codec, codec2, lifecycle), "DataPackRegistryCodec for " + resourceKey);
	}

	private static <E> Decoder<Registry<E>> dataPackAwareDecoder(
		ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, Decoder<Map<ResourceKey<E>, E>> decoder, Lifecycle lifecycle
	) {
		final Decoder<WritableRegistry<E>> decoder2 = decoder.map(map -> {
			WritableRegistry<E> writableRegistry = new MappedRegistry<>(resourceKey, lifecycle, null);
			map.forEach((resourceKeyxx, object) -> writableRegistry.register(resourceKeyxx, (E)object, lifecycle));
			return writableRegistry;
		});
		return new Decoder<Registry<E>>() {
			@Override
			public <T> DataResult<Pair<Registry<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
				DataResult<Pair<WritableRegistry<E>, T>> dataResult = decoder2.decode(dynamicOps, object);
				return dynamicOps instanceof RegistryOps<?> registryOps
					? (DataResult)registryOps.registryLoader()
						.map(bound -> this.overrideFromResources(dataResult, registryOps, bound.loader()))
						.orElseGet(() -> DataResult.error("Can't load registry with this ops"))
					: dataResult.map(pair -> pair.mapFirst(writableRegistry -> writableRegistry));
			}

			private <T> DataResult<Pair<Registry<E>, T>> overrideFromResources(
				DataResult<Pair<WritableRegistry<E>, T>> dataResult, RegistryOps<?> registryOps, RegistryLoader registryLoader
			) {
				return dataResult.flatMap(
					pair -> registryLoader.overrideRegistryFromResources((WritableRegistry<E>)pair.getFirst(), resourceKey, codec, registryOps.getAsJson())
							.map(registry -> Pair.of(registry, pair.getSecond()))
				);
			}
		};
	}

	private static <T> Codec<Map<ResourceKey<T>, T>> directCodec(ResourceKey<? extends Registry<T>> resourceKey, Codec<T> codec) {
		return Codec.unboundedMap(ResourceKey.codec(resourceKey), codec);
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
