package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
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
			Optional<HolderOwner<E>> optional = registryOps.owner(this.registryKey);
			if (optional.isPresent()) {
				if (!holder.canSerializeIn((HolderOwner<E>)optional.get())) {
					return DataResult.error(() -> "Element " + holder + " is not valid in current registry set");
				}

				return holder.unwrap()
					.map(
						resourceKey -> ResourceLocation.CODEC.encode(resourceKey.location(), dynamicOps, object),
						objectx -> DataResult.error(() -> "Elements from registry " + this.registryKey + " can't be serialized to a value")
					);
			}
		}

		return DataResult.error(() -> "Can't access registry " + this.registryKey);
	}

	@Override
	public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
		if (dynamicOps instanceof RegistryOps<?> registryOps) {
			Optional<HolderGetter<E>> optional = registryOps.getter(this.registryKey);
			if (optional.isPresent()) {
				return ResourceLocation.CODEC
					.decode(dynamicOps, object)
					.flatMap(
						pair -> {
							ResourceLocation resourceLocation = (ResourceLocation)pair.getFirst();
							return ((DataResult)((HolderGetter)optional.get())
									.get(ResourceKey.create(this.registryKey, resourceLocation))
									.map(DataResult::success)
									.orElseGet(() -> DataResult.error(() -> "Failed to get element " + resourceLocation)))
								.map(reference -> Pair.of(reference, pair.getSecond()))
								.setLifecycle(Lifecycle.stable());
						}
					);
			}
		}

		return DataResult.error(() -> "Can't access registry " + this.registryKey);
	}

	public String toString() {
		return "RegistryFixedCodec[" + this.registryKey + "]";
	}
}
