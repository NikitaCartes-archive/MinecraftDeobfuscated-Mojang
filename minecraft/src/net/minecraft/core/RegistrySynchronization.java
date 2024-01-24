package net.minecraft.core;

import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;

public class RegistrySynchronization {
	private static final Set<ResourceKey<? extends Registry<?>>> NETWORKABLE_REGISTRIES = (Set<ResourceKey<? extends Registry<?>>>)RegistryDataLoader.SYNCHRONIZED_REGISTRIES
		.stream()
		.map(RegistryDataLoader.RegistryData::key)
		.collect(Collectors.toUnmodifiableSet());

	public static void packRegistries(
		DynamicOps<Tag> dynamicOps,
		RegistryAccess registryAccess,
		BiConsumer<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> biConsumer
	) {
		RegistryDataLoader.SYNCHRONIZED_REGISTRIES.forEach(registryData -> packRegistry(dynamicOps, registryData, registryAccess, biConsumer));
	}

	private static <T> void packRegistry(
		DynamicOps<Tag> dynamicOps,
		RegistryDataLoader.RegistryData<T> registryData,
		RegistryAccess registryAccess,
		BiConsumer<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> biConsumer
	) {
		registryAccess.registry(registryData.key())
			.ifPresent(
				registry -> {
					List<RegistrySynchronization.PackedRegistryEntry> list = new ArrayList(registry.size());
					registry.holders()
						.forEach(
							reference -> {
								Tag tag = Util.getOrThrow(
									registryData.elementCodec().encodeStart(dynamicOps, (T)reference.value()),
									string -> new IllegalArgumentException("Failed to serialize " + reference.key() + ": " + string)
								);
								list.add(new RegistrySynchronization.PackedRegistryEntry(reference.key().location(), tag));
							}
						);
					biConsumer.accept(registry.key(), list);
				}
			);
	}

	private static Stream<RegistryAccess.RegistryEntry<?>> ownedNetworkableRegistries(RegistryAccess registryAccess) {
		return registryAccess.registries().filter(registryEntry -> NETWORKABLE_REGISTRIES.contains(registryEntry.key()));
	}

	public static Stream<RegistryAccess.RegistryEntry<?>> networkedRegistries(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
		return ownedNetworkableRegistries(layeredRegistryAccess.getAccessFrom(RegistryLayer.WORLDGEN));
	}

	public static Stream<RegistryAccess.RegistryEntry<?>> networkSafeRegistries(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
		Stream<RegistryAccess.RegistryEntry<?>> stream = layeredRegistryAccess.getLayer(RegistryLayer.STATIC).registries();
		Stream<RegistryAccess.RegistryEntry<?>> stream2 = networkedRegistries(layeredRegistryAccess);
		return Stream.concat(stream2, stream);
	}

	public static record PackedRegistryEntry(ResourceLocation id, Tag data) {
		public static final StreamCodec<ByteBuf, RegistrySynchronization.PackedRegistryEntry> STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC,
			RegistrySynchronization.PackedRegistryEntry::id,
			ByteBufCodecs.TAG,
			RegistrySynchronization.PackedRegistryEntry::data,
			RegistrySynchronization.PackedRegistryEntry::new
		);
	}
}
