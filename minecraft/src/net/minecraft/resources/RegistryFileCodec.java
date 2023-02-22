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

public final class RegistryFileCodec<E> implements Codec<Holder<E>> {
	private final ResourceKey<? extends Registry<E>> registryKey;
	private final Codec<E> elementCodec;
	private final boolean allowInline;

	public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
		return create(resourceKey, codec, true);
	}

	public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, boolean bl) {
		return new RegistryFileCodec<>(resourceKey, codec, bl);
	}

	private RegistryFileCodec(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, boolean bl) {
		this.registryKey = resourceKey;
		this.elementCodec = codec;
		this.allowInline = bl;
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
						object2 -> this.elementCodec.encode((E)object2, dynamicOps, object)
					);
			}
		}

		return this.elementCodec.encode(holder.value(), dynamicOps, object);
	}

	@Override
	public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
		if (dynamicOps instanceof RegistryOps<?> registryOps) {
			Optional<HolderGetter<E>> optional = registryOps.getter(this.registryKey);
			if (optional.isEmpty()) {
				return DataResult.error(() -> "Registry does not exist: " + this.registryKey);
			} else {
				HolderGetter<E> holderGetter = (HolderGetter<E>)optional.get();
				DataResult<Pair<ResourceLocation, T>> dataResult = ResourceLocation.CODEC.decode(dynamicOps, object);
				if (dataResult.result().isEmpty()) {
					return !this.allowInline
						? DataResult.error(() -> "Inline definitions not allowed here")
						: this.elementCodec.decode(dynamicOps, object).map(pairx -> pairx.mapFirst(Holder::direct));
				} else {
					Pair<ResourceLocation, T> pair = (Pair<ResourceLocation, T>)dataResult.result().get();
					ResourceKey<E> resourceKey = ResourceKey.create(this.registryKey, pair.getFirst());
					return ((DataResult)holderGetter.get(resourceKey).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Failed to get element " + resourceKey)))
						.<Pair<Holder<E>, T>>map(reference -> Pair.of(reference, pair.getSecond()))
						.setLifecycle(Lifecycle.stable());
				}
			}
		} else {
			return this.elementCodec.decode(dynamicOps, object).map(pairx -> pairx.mapFirst(Holder::direct));
		}
	}

	public String toString() {
		return "RegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
	}
}
