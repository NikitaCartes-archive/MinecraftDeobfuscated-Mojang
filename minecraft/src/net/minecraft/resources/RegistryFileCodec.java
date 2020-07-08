package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.Registry;

public final class RegistryFileCodec<E> implements Codec<Supplier<E>> {
	private final ResourceKey<? extends Registry<E>> registryKey;
	private final MapCodec<E> elementCodec;

	public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> resourceKey, MapCodec<E> mapCodec) {
		return new RegistryFileCodec<>(resourceKey, mapCodec);
	}

	private RegistryFileCodec(ResourceKey<? extends Registry<E>> resourceKey, MapCodec<E> mapCodec) {
		this.registryKey = resourceKey;
		this.elementCodec = mapCodec;
	}

	public <T> DataResult<T> encode(Supplier<E> supplier, DynamicOps<T> dynamicOps, T object) {
		return dynamicOps instanceof RegistryWriteOps
			? ((RegistryWriteOps)dynamicOps).encode(supplier.get(), object, this.registryKey, this.elementCodec)
			: this.elementCodec.codec().encode((E)supplier.get(), dynamicOps, object);
	}

	@Override
	public <T> DataResult<Pair<Supplier<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
		return dynamicOps instanceof RegistryReadOps
			? ((RegistryReadOps)dynamicOps).decodeElement(object, this.registryKey, this.elementCodec)
			: this.elementCodec.codec().decode(dynamicOps, object).map(pair -> pair.mapFirst(objectx -> () -> objectx));
	}

	public String toString() {
		return "RegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
	}
}
