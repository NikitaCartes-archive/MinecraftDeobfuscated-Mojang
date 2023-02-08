package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;

public class RegistrySynchronization {
	private static final Map<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>> NETWORKABLE_REGISTRIES = Util.make(() -> {
		Builder<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>> builder = ImmutableMap.builder();
		put(builder, Registries.BIOME, Biome.NETWORK_CODEC);
		put(builder, Registries.CHAT_TYPE, ChatType.CODEC);
		put(builder, Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC);
		put(builder, Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC);
		put(builder, Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC);
		put(builder, Registries.DAMAGE_TYPE, DamageType.CODEC);
		return builder.build();
	});
	public static final Codec<RegistryAccess> NETWORK_CODEC = makeNetworkCodec();

	private static <E> void put(
		Builder<ResourceKey<? extends Registry<?>>, RegistrySynchronization.NetworkedRegistryData<?>> builder,
		ResourceKey<? extends Registry<E>> resourceKey,
		Codec<E> codec
	) {
		builder.put(resourceKey, new RegistrySynchronization.NetworkedRegistryData<>(resourceKey, codec));
	}

	private static Stream<RegistryAccess.RegistryEntry<?>> ownedNetworkableRegistries(RegistryAccess registryAccess) {
		return registryAccess.registries().filter(registryEntry -> NETWORKABLE_REGISTRIES.containsKey(registryEntry.key()));
	}

	private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> resourceKey) {
		return (DataResult<? extends Codec<E>>)Optional.ofNullable((RegistrySynchronization.NetworkedRegistryData)NETWORKABLE_REGISTRIES.get(resourceKey))
			.map(networkedRegistryData -> networkedRegistryData.networkCodec())
			.map(DataResult::success)
			.orElseGet(() -> DataResult.error("Unknown or not serializable registry: " + resourceKey));
	}

	private static <E> Codec<RegistryAccess> makeNetworkCodec() {
		Codec<ResourceKey<? extends Registry<E>>> codec = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
		Codec<Registry<E>> codec2 = codec.partialDispatch(
			"type",
			registry -> DataResult.success(registry.key()),
			resourceKey -> getNetworkCodec(resourceKey).map(codecx -> RegistryCodecs.networkCodec(resourceKey, Lifecycle.experimental(), codecx))
		);
		UnboundedMapCodec<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> unboundedMapCodec = Codec.unboundedMap(codec, codec2);
		return captureMap(unboundedMapCodec);
	}

	private static <K extends ResourceKey<? extends Registry<?>>, V extends Registry<?>> Codec<RegistryAccess> captureMap(
		UnboundedMapCodec<K, V> unboundedMapCodec
	) {
		return unboundedMapCodec.xmap(
			RegistryAccess.ImmutableRegistryAccess::new,
			registryAccess -> (Map)ownedNetworkableRegistries(registryAccess)
					.collect(ImmutableMap.toImmutableMap(registryEntry -> registryEntry.key(), registryEntry -> registryEntry.value()))
		);
	}

	public static Stream<RegistryAccess.RegistryEntry<?>> networkedRegistries(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
		return ownedNetworkableRegistries(layeredRegistryAccess.getAccessFrom(RegistryLayer.WORLDGEN));
	}

	public static Stream<RegistryAccess.RegistryEntry<?>> networkSafeRegistries(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
		Stream<RegistryAccess.RegistryEntry<?>> stream = layeredRegistryAccess.getLayer(RegistryLayer.STATIC).registries();
		Stream<RegistryAccess.RegistryEntry<?>> stream2 = networkedRegistries(layeredRegistryAccess);
		return Stream.concat(stream2, stream);
	}

	static record NetworkedRegistryData<E>(ResourceKey<? extends Registry<E>> key, Codec<E> networkCodec) {
	}
}
