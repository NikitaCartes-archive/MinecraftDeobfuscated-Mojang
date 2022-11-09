/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;

public class RegistrySynchronization {
    private static final Map<ResourceKey<? extends Registry<?>>, NetworkedRegistryData<?>> NETWORKABLE_REGISTRIES = Util.make(() -> {
        ImmutableMap.Builder<ResourceKey<Registry<?>>, NetworkedRegistryData<?>> builder = ImmutableMap.builder();
        RegistrySynchronization.put(builder, Registries.BIOME, Biome.NETWORK_CODEC);
        RegistrySynchronization.put(builder, Registries.CHAT_TYPE, ChatType.CODEC);
        RegistrySynchronization.put(builder, Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC);
        return builder.build();
    });
    public static final Codec<RegistryAccess> NETWORK_CODEC = RegistrySynchronization.makeNetworkCodec();

    private static <E> void put(ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, NetworkedRegistryData<?>> builder, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
        builder.put(resourceKey, new NetworkedRegistryData<E>(resourceKey, codec));
    }

    private static Stream<RegistryAccess.RegistryEntry<?>> ownedNetworkableRegistries(RegistryAccess registryAccess) {
        return registryAccess.registries().filter(registryEntry -> NETWORKABLE_REGISTRIES.containsKey(registryEntry.key()));
    }

    private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> resourceKey) {
        return Optional.ofNullable(NETWORKABLE_REGISTRIES.get(resourceKey)).map(networkedRegistryData -> networkedRegistryData.networkCodec()).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown or not serializable registry: " + resourceKey));
    }

    private static <E> Codec<RegistryAccess> makeNetworkCodec() {
        Codec<ResourceKey> codec = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
        Codec<Registry> codec2 = codec.partialDispatch("type", registry -> DataResult.success(registry.key()), resourceKey -> RegistrySynchronization.getNetworkCodec(resourceKey).map(codec -> RegistryCodecs.networkCodec(resourceKey, Lifecycle.experimental(), codec)));
        UnboundedMapCodec<ResourceKey, Registry> unboundedMapCodec = Codec.unboundedMap(codec, codec2);
        return RegistrySynchronization.captureMap(unboundedMapCodec);
    }

    private static <K extends ResourceKey<? extends Registry<?>>, V extends Registry<?>> Codec<RegistryAccess> captureMap(UnboundedMapCodec<K, V> unboundedMapCodec) {
        return unboundedMapCodec.xmap(RegistryAccess.ImmutableRegistryAccess::new, registryAccess -> RegistrySynchronization.ownedNetworkableRegistries(registryAccess).collect(ImmutableMap.toImmutableMap(registryEntry -> registryEntry.key(), registryEntry -> registryEntry.value())));
    }

    public static Stream<RegistryAccess.RegistryEntry<?>> networkedRegistries(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
        return RegistrySynchronization.ownedNetworkableRegistries(layeredRegistryAccess.getAccessFrom(RegistryLayer.WORLDGEN));
    }

    public static Stream<RegistryAccess.RegistryEntry<?>> networkSafeRegistries(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
        Stream<RegistryAccess.RegistryEntry<?>> stream = layeredRegistryAccess.getLayer(RegistryLayer.STATIC).registries();
        Stream<RegistryAccess.RegistryEntry<?>> stream2 = RegistrySynchronization.networkedRegistries(layeredRegistryAccess);
        return Stream.concat(stream2, stream);
    }

    record NetworkedRegistryData<E>(ResourceKey<? extends Registry<E>> key, Codec<E> networkCodec) {
    }
}

