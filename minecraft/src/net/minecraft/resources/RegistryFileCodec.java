package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.function.Supplier;
import net.minecraft.core.Registry;

public final class RegistryFileCodec<E> implements Codec<Supplier<E>> {
	private final ResourceKey<? extends Registry<E>> registryKey;
	private final Codec<E> elementCodec;

	public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
		return new RegistryFileCodec<>(resourceKey, codec);
	}

	private RegistryFileCodec(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
		this.registryKey = resourceKey;
		this.elementCodec = codec;
	}

	public <T> DataResult<T> encode(Supplier<E> supplier, DynamicOps<T> dynamicOps, T object) {
		return dynamicOps instanceof RegistryWriteOps
			? ((RegistryWriteOps)dynamicOps).encode(supplier.get(), object, this.registryKey, this.elementCodec)
			: this.elementCodec.encode((E)supplier.get(), dynamicOps, object);
	}

	@Override
	public <T> DataResult<Pair<Supplier<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
		return dynamicOps instanceof RegistryReadOps
			? ((RegistryReadOps)dynamicOps).decodeElement(object, this.registryKey, this.elementCodec)
			: this.elementCodec.decode(dynamicOps, object).map(pair -> pair.mapFirst(objectx -> () -> objectx));
	}

	public String toString() {
		return "RegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
	}
}
