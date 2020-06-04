package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;

public interface RegistryAccess {
	<E> Optional<WritableRegistry<E>> registry(ResourceKey<Registry<E>> resourceKey);

	@Environment(EnvType.CLIENT)
	Registry<DimensionType> dimensionTypes();

	static RegistryAccess.RegistryHolder builtin() {
		return DimensionType.registerBuiltin(new RegistryAccess.RegistryHolder());
	}

	public static final class RegistryHolder implements RegistryAccess {
		public static final Codec<RegistryAccess.RegistryHolder> CODEC = MappedRegistry.networkCodec(
				Registry.DIMENSION_TYPE_REGISTRY, Lifecycle.experimental(), DimensionType.DIRECT_CODEC
			)
			.<RegistryAccess.RegistryHolder>xmap(RegistryAccess.RegistryHolder::new, registryHolder -> registryHolder.dimensionTypes)
			.fieldOf("dimension")
			.codec();
		private final MappedRegistry<DimensionType> dimensionTypes;

		public RegistryHolder() {
			this(new MappedRegistry<>(Registry.DIMENSION_TYPE_REGISTRY, Lifecycle.experimental()));
		}

		private RegistryHolder(MappedRegistry<DimensionType> mappedRegistry) {
			this.dimensionTypes = mappedRegistry;
		}

		public void registerDimension(ResourceKey<DimensionType> resourceKey, DimensionType dimensionType) {
			this.dimensionTypes.register(resourceKey, dimensionType);
		}

		@Override
		public <E> Optional<WritableRegistry<E>> registry(ResourceKey<Registry<E>> resourceKey) {
			return Objects.equals(resourceKey, Registry.DIMENSION_TYPE_REGISTRY) ? Optional.of(this.dimensionTypes) : Optional.empty();
		}

		@Override
		public Registry<DimensionType> dimensionTypes() {
			return this.dimensionTypes;
		}
	}
}
