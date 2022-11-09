/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;

public class RegistryCodecs {
    private static <T> MapCodec<RegistryEntry<T>> withNameAndId(ResourceKey<? extends Registry<T>> resourceKey, MapCodec<T> mapCodec) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)ResourceKey.codec(resourceKey).fieldOf("name")).forGetter(RegistryEntry::key), ((MapCodec)Codec.INT.fieldOf("id")).forGetter(RegistryEntry::id), mapCodec.forGetter(RegistryEntry::value)).apply((Applicative<RegistryEntry, ?>)instance, RegistryEntry::new));
    }

    public static <T> Codec<Registry<T>> networkCodec(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, Codec<T> codec) {
        return RegistryCodecs.withNameAndId(resourceKey, codec.fieldOf("element")).codec().listOf().xmap(list -> {
            MappedRegistry writableRegistry = new MappedRegistry(resourceKey, lifecycle);
            for (RegistryEntry registryEntry : list) {
                writableRegistry.registerMapping(registryEntry.id(), registryEntry.key(), registryEntry.value(), lifecycle);
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

    public static <E> Codec<Registry<E>> fullCodec(ResourceKey<? extends Registry<E>> resourceKey, Lifecycle lifecycle, Codec<E> codec) {
        UnboundedMapCodec codec2 = Codec.unboundedMap(ResourceKey.codec(resourceKey), codec);
        return codec2.xmap(map -> {
            MappedRegistry writableRegistry = new MappedRegistry(resourceKey, lifecycle);
            map.forEach((resourceKey, object) -> writableRegistry.register(resourceKey, object, lifecycle));
            return writableRegistry.freeze();
        }, registry -> ImmutableMap.copyOf(registry.entrySet()));
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

