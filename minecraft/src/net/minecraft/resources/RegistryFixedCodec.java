package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

public final class RegistryFixedCodec<E> implements Codec<Holder<E>> {
	private final ResourceKey<? extends Registry<E>> registryKey;

	public static <E> RegistryFixedCodec<E> create(ResourceKey<? extends Registry<E>> resourceKey) {
		return new RegistryFixedCodec<>(resourceKey);
	}

	private RegistryFixedCodec(ResourceKey<? extends Registry<E>> resourceKey) {
		this.registryKey = resourceKey;
	}

	public <T> DataResult<T> encode(Holder<E> holder, DynamicOps<T> dynamicOps, T object) {
		if (dynamicOps instanceof RegistryOps<?> registryOps) {
			Optional<? extends Registry<E>> optional = registryOps.registry(this.registryKey);
			if (optional.isPresent()) {
				if (!holder.isValidInRegistry((Registry<E>)optional.get())) {
					return DataResult.error("Element " + holder + " is not valid in current registry set");
				}

				return holder.unwrap()
					.map(
						resourceKey -> ResourceLocation.CODEC.encode(resourceKey.location(), dynamicOps, object),
						objectx -> DataResult.error("Elements from registry " + this.registryKey + " can't be serialized to a value")
					);
			}
		}

		return DataResult.error("Can't access registry " + this.registryKey);
	}

	@Override
	public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
		if (dynamicOps instanceof RegistryOps<?> registryOps) {
			Optional<? extends Registry<E>> optional = registryOps.registry(this.registryKey);
			if (optional.isPresent()) {
				return ResourceLocation.CODEC.decode(dynamicOps, object).flatMap(pair -> {
					ResourceLocation resourceLocation = (ResourceLocation)pair.getFirst();
					DataResult<Holder<E>> dataResult = ((Registry)optional.get()).getOrCreateHolder(ResourceKey.create(this.registryKey, resourceLocation));
					return dataResult.map(holder -> Pair.of(holder, pair.getSecond())).setLifecycle(Lifecycle.stable());
				});
			}
		}

		return DataResult.error("Can't access registry " + this.registryKey);
	}

	public String toString() {
		return "RegistryFixedCodec[" + this.registryKey + "]";
	}
}
