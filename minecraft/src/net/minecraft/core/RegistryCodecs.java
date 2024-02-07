package net.minecraft.core;

import com.mojang.serialization.Codec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;

public class RegistryCodecs {
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
