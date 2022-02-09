/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.Applicative;
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
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.RegistryLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class RegistryCodecs {
    private static <T> MapCodec<RegistryEntry<T>> withNameAndId(ResourceKey<? extends Registry<T>> resourceKey, MapCodec<T> mapCodec) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)ResourceLocation.CODEC.xmap(ResourceKey.elementKey(resourceKey), ResourceKey::location).fieldOf("name")).forGetter(RegistryEntry::key), ((MapCodec)Codec.INT.fieldOf("id")).forGetter(RegistryEntry::id), mapCodec.forGetter(RegistryEntry::value)).apply((Applicative<RegistryEntry, ?>)instance, RegistryEntry::new));
    }

    public static <T> Codec<Registry<T>> networkCodec(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, Codec<T> codec) {
        return RegistryCodecs.withNameAndId(resourceKey, codec.fieldOf("element")).codec().listOf().xmap(list -> {
            MappedRegistry writableRegistry = new MappedRegistry(resourceKey, lifecycle, null);
            for (RegistryEntry registryEntry : list) {
                ((WritableRegistry)writableRegistry).registerMapping(registryEntry.id(), registryEntry.key(), registryEntry.value(), lifecycle);
            }
            return writableRegistry;
        }, registry -> {
            ImmutableList.Builder builder = ImmutableList.builder();
            for (Object object : registry) {
                builder.add(new RegistryEntry(registry.getResourceKey(object).get(), registry.getId(object), object));
            }
            return builder.build();
        });
    }

    public static <E> Codec<Registry<E>> dataPackAwareCodec(ResourceKey<? extends Registry<E>> resourceKey, Lifecycle lifecycle, Codec<E> codec) {
        Codec<Map<ResourceKey<E>, E>> codec2 = RegistryCodecs.directCodec(resourceKey, codec);
        Encoder<Registry> encoder = codec2.comap(registry -> ImmutableMap.copyOf(registry.entrySet()));
        return Codec.of(encoder, RegistryCodecs.dataPackAwareDecoder(resourceKey, codec, codec2, lifecycle), "DataPackRegistryCodec for " + resourceKey);
    }

    private static <E> Decoder<Registry<E>> dataPackAwareDecoder(final ResourceKey<? extends Registry<E>> resourceKey, final Codec<E> codec, Decoder<Map<ResourceKey<E>, E>> decoder, Lifecycle lifecycle) {
        final Decoder<WritableRegistry> decoder2 = decoder.map(map -> {
            MappedRegistry writableRegistry = new MappedRegistry(resourceKey, lifecycle, null);
            map.forEach((resourceKey, object) -> writableRegistry.register(resourceKey, object, lifecycle));
            return writableRegistry;
        });
        return new Decoder<Registry<E>>(){

            @Override
            public <T> DataResult<Pair<Registry<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
                DataResult dataResult = decoder2.decode(dynamicOps, object);
                if (dynamicOps instanceof RegistryOps) {
                    RegistryOps registryOps = (RegistryOps)dynamicOps;
                    return registryOps.registryLoader().map((? super T bound) -> this.overrideFromResources(dataResult, registryOps, bound.loader())).orElseGet(() -> DataResult.error("Can't load registry with this ops"));
                }
                return dataResult.map((? super R pair) -> pair.mapFirst(writableRegistry -> writableRegistry));
            }

            private <T> DataResult<Pair<Registry<E>, T>> overrideFromResources(DataResult<Pair<WritableRegistry<E>, T>> dataResult, RegistryOps<?> registryOps, RegistryLoader registryLoader) {
                return dataResult.flatMap((? super R pair) -> registryLoader.overrideRegistryFromResources((WritableRegistry)pair.getFirst(), resourceKey, codec, registryOps.getAsJson()).map((? super R registry) -> Pair.of(registry, pair.getSecond())));
            }
        };
    }

    private static <T> Codec<Map<ResourceKey<T>, T>> directCodec(ResourceKey<? extends Registry<T>> resourceKey, Codec<T> codec) {
        return Codec.unboundedMap(ResourceLocation.CODEC.xmap(ResourceKey.elementKey(resourceKey), ResourceKey::location), codec);
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
        return RegistryCodecs.homogeneousList(resourceKey, codec, false);
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, boolean bl) {
        return HolderSetCodec.create(resourceKey, RegistryFileCodec.create(resourceKey, codec), bl);
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourceKey) {
        return RegistryCodecs.homogeneousList(resourceKey, false);
    }

    public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> resourceKey, boolean bl) {
        return HolderSetCodec.create(resourceKey, RegistryFixedCodec.create(resourceKey), bl);
    }

    record RegistryEntry<T>(ResourceKey<T> key, int id, T value) {
    }
}

