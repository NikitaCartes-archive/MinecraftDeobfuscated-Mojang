package net.minecraft.core;

import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.repository.KnownPack;

public class RegistrySynchronization {
	private static final Set<ResourceKey<? extends Registry<?>>> NETWORKABLE_REGISTRIES = (Set<ResourceKey<? extends Registry<?>>>)RegistryDataLoader.SYNCHRONIZED_REGISTRIES
		.stream()
		.map(RegistryDataLoader.RegistryData::key)
		.collect(Collectors.toUnmodifiableSet());

	public static void packRegistries(
		DynamicOps<Tag> dynamicOps,
		RegistryAccess registryAccess,
		Set<KnownPack> set,
		BiConsumer<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> biConsumer
	) {
		RegistryDataLoader.SYNCHRONIZED_REGISTRIES.forEach(registryData -> packRegistry(dynamicOps, registryData, registryAccess, set, biConsumer));
	}

	private static <T> void packRegistry(
		DynamicOps<Tag> dynamicOps,
		RegistryDataLoader.RegistryData<T> registryData,
		RegistryAccess registryAccess,
		Set<KnownPack> set,
		BiConsumer<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> biConsumer
	) {
		registryAccess.lookup(registryData.key())
			.ifPresent(
				registry -> {
					List<RegistrySynchronization.PackedRegistryEntry> list = new ArrayList(registry.size());
					registry.listElements()
						.forEach(
							reference -> {
								boolean bl = registry.registrationInfo(reference.key()).flatMap(RegistrationInfo::knownPackInfo).filter(set::contains).isPresent();
								Optional<Tag> optional;
								if (bl) {
									optional = Optional.empty();
								} else {
									Tag tag = registryData.elementCodec()
										.encodeStart(dynamicOps, (T)reference.value())
										.getOrThrow(string -> new IllegalArgumentException("Failed to serialize " + reference.key() + ": " + string));
									optional = Optional.of(tag);
								}

								list.add(new RegistrySynchronization.PackedRegistryEntry(reference.key().location(), optional));
							}
						);
					biConsumer.accept(registry.key(), list);
				}
			);
	}

	private static Stream<RegistryAccess.RegistryEntry<?>> ownedNetworkableRegistries(RegistryAccess registryAccess) {
		return registryAccess.registries().filter(registryEntry -> isNetworkable(registryEntry.key()));
	}

	public static Stream<RegistryAccess.RegistryEntry<?>> networkedRegistries(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
		return ownedNetworkableRegistries(layeredRegistryAccess.getAccessFrom(RegistryLayer.WORLDGEN));
	}

	public static Stream<RegistryAccess.RegistryEntry<?>> networkSafeRegistries(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
		Stream<RegistryAccess.RegistryEntry<?>> stream = layeredRegistryAccess.getLayer(RegistryLayer.STATIC).registries();
		Stream<RegistryAccess.RegistryEntry<?>> stream2 = networkedRegistries(layeredRegistryAccess);
		return Stream.concat(stream2, stream);
	}

	public static boolean isNetworkable(ResourceKey<? extends Registry<?>> resourceKey) {
		return NETWORKABLE_REGISTRIES.contains(resourceKey);
	}

	public static record PackedRegistryEntry(ResourceLocation id, Optional<Tag> data) {
		public static final StreamCodec<ByteBuf, RegistrySynchronization.PackedRegistryEntry> STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC,
			RegistrySynchronization.PackedRegistryEntry::id,
			ByteBufCodecs.TAG.apply(ByteBufCodecs::optional),
			RegistrySynchronization.PackedRegistryEntry::data,
			RegistrySynchronization.PackedRegistryEntry::new
		);
	}
}
