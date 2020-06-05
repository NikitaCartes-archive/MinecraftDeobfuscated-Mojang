package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;

public final class RegistryDataPackCodec<E> implements Codec<MappedRegistry<E>> {
	private final Codec<MappedRegistry<E>> directCodec;
	private final ResourceKey<Registry<E>> registryKey;
	private final Codec<E> elementCodec;

	public static <E> RegistryDataPackCodec<E> create(ResourceKey<Registry<E>> resourceKey, Lifecycle lifecycle, Codec<E> codec) {
		return new RegistryDataPackCodec<>(resourceKey, lifecycle, codec);
	}

	private RegistryDataPackCodec(ResourceKey<Registry<E>> resourceKey, Lifecycle lifecycle, Codec<E> codec) {
		this.directCodec = MappedRegistry.directCodec(resourceKey, lifecycle, codec);
		this.registryKey = resourceKey;
		this.elementCodec = codec;
	}

	public <T> DataResult<T> encode(MappedRegistry<E> mappedRegistry, DynamicOps<T> dynamicOps, T object) {
		return this.directCodec.encode(mappedRegistry, dynamicOps, object);
	}

	@Override
	public <T> DataResult<Pair<MappedRegistry<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
		DataResult<Pair<MappedRegistry<E>, T>> dataResult = this.directCodec.decode(dynamicOps, object);
		return dynamicOps instanceof RegistryReadOps
			? dataResult.flatMap(
				pair -> ((RegistryReadOps)dynamicOps)
						.decodeElements((MappedRegistry<E>)pair.getFirst(), this.registryKey, this.elementCodec)
						.map(mappedRegistry -> Pair.of(mappedRegistry, pair.getSecond()))
			)
			: dataResult;
	}

	public String toString() {
		return "RegistryDapaPackCodec[" + this.directCodec + " " + this.registryKey + " " + this.elementCodec + "]";
	}
}
