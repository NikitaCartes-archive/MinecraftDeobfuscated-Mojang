package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;

public class RegistryWriteOps<T> extends DelegatingOps<T> {
	private final RegistryAccess registryHolder;

	public static <T> RegistryWriteOps<T> create(DynamicOps<T> dynamicOps, RegistryAccess registryAccess) {
		return new RegistryWriteOps<>(dynamicOps, registryAccess);
	}

	private RegistryWriteOps(DynamicOps<T> dynamicOps, RegistryAccess registryAccess) {
		super(dynamicOps);
		this.registryHolder = registryAccess;
	}

	protected <E> DataResult<T> encode(E object, T object2, ResourceKey<? extends Registry<E>> resourceKey, MapCodec<E> mapCodec) {
		Optional<WritableRegistry<E>> optional = this.registryHolder.registry(resourceKey);
		if (optional.isPresent()) {
			WritableRegistry<E> writableRegistry = (WritableRegistry<E>)optional.get();
			Optional<ResourceKey<E>> optional2 = writableRegistry.getResourceKey(object);
			if (optional2.isPresent()) {
				ResourceKey<E> resourceKey2 = (ResourceKey<E>)optional2.get();
				if (writableRegistry.persistent(resourceKey2)) {
					return MappedRegistry.withName(resourceKey, mapCodec).codec().encode(Pair.of(resourceKey2, object), this.delegate, object2);
				}

				return ResourceLocation.CODEC.encode(resourceKey2.location(), this.delegate, object2);
			}
		}

		return mapCodec.codec().encode(object, this, object2);
	}
}
