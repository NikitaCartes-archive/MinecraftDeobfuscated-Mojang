/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;

public interface RegistryAccess {
    @Environment(value=EnvType.CLIENT)
    public Registry<DimensionType> dimensionTypes();

    @Environment(value=EnvType.CLIENT)
    public static RegistryHolder builtin() {
        return DimensionType.registerBuiltin(new RegistryHolder());
    }

    public static final class RegistryHolder
    implements RegistryAccess {
        public static final Codec<RegistryHolder> CODEC = ((MapCodec)MappedRegistry.codec(Registry.DIMENSION_TYPE_REGISTRY, Lifecycle.experimental(), DimensionType.CODEC).xmap(RegistryHolder::new, registryHolder -> registryHolder.dimensionTypes).fieldOf("dimension")).codec();
        private final MappedRegistry<DimensionType> dimensionTypes;

        public RegistryHolder() {
            this(new MappedRegistry<DimensionType>(Registry.DIMENSION_TYPE_REGISTRY, Lifecycle.experimental()));
        }

        private RegistryHolder(MappedRegistry<DimensionType> mappedRegistry) {
            this.dimensionTypes = mappedRegistry;
        }

        public void registerDimension(ResourceKey<DimensionType> resourceKey, DimensionType dimensionType) {
            this.dimensionTypes.register(resourceKey, dimensionType);
        }

        @Override
        @Environment(value=EnvType.CLIENT)
        public Registry<DimensionType> dimensionTypes() {
            return this.dimensionTypes;
        }
    }
}

