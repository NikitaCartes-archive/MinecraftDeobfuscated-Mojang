/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.StructureSets;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPresets;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.slf4j.Logger;

public class BuiltinRegistries {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, Supplier<? extends Holder<?>>> LOADERS = Maps.newLinkedHashMap();
    private static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY = new MappedRegistry(ResourceKey.createRegistryKey(new ResourceLocation("root")), Lifecycle.experimental(), null);
    public static final Registry<? extends Registry<?>> REGISTRY = WRITABLE_REGISTRY;
    public static final Registry<DimensionType> DIMENSION_TYPE = BuiltinRegistries.registerSimple(Registry.DIMENSION_TYPE_REGISTRY, DimensionTypes::bootstrap);
    public static final Registry<ConfiguredWorldCarver<?>> CONFIGURED_CARVER = BuiltinRegistries.registerSimple(Registry.CONFIGURED_CARVER_REGISTRY, () -> Carvers.CAVE);
    public static final Registry<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE = BuiltinRegistries.registerSimple(Registry.CONFIGURED_FEATURE_REGISTRY, FeatureUtils::bootstrap);
    public static final Registry<PlacedFeature> PLACED_FEATURE = BuiltinRegistries.registerSimple(Registry.PLACED_FEATURE_REGISTRY, PlacementUtils::bootstrap);
    public static final Registry<Structure> STRUCTURES = BuiltinRegistries.registerSimple(Registry.STRUCTURE_REGISTRY, Structures::bootstrap);
    public static final Registry<StructureSet> STRUCTURE_SETS = BuiltinRegistries.registerSimple(Registry.STRUCTURE_SET_REGISTRY, StructureSets::bootstrap);
    public static final Registry<StructureProcessorList> PROCESSOR_LIST = BuiltinRegistries.registerSimple(Registry.PROCESSOR_LIST_REGISTRY, () -> ProcessorLists.ZOMBIE_PLAINS);
    public static final Registry<StructureTemplatePool> TEMPLATE_POOL = BuiltinRegistries.registerSimple(Registry.TEMPLATE_POOL_REGISTRY, Pools::bootstrap);
    public static final Registry<Biome> BIOME = BuiltinRegistries.registerSimple(Registry.BIOME_REGISTRY, Biomes::bootstrap);
    public static final Registry<NormalNoise.NoiseParameters> NOISE = BuiltinRegistries.registerSimple(Registry.NOISE_REGISTRY, NoiseData::bootstrap);
    public static final Registry<DensityFunction> DENSITY_FUNCTION = BuiltinRegistries.registerSimple(Registry.DENSITY_FUNCTION_REGISTRY, NoiseRouterData::bootstrap);
    public static final Registry<NoiseGeneratorSettings> NOISE_GENERATOR_SETTINGS = BuiltinRegistries.registerSimple(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings::bootstrap);
    public static final Registry<WorldPreset> WORLD_PRESET = BuiltinRegistries.registerSimple(Registry.WORLD_PRESET_REGISTRY, WorldPresets::bootstrap);
    public static final Registry<FlatLevelGeneratorPreset> FLAT_LEVEL_GENERATOR_PRESET = BuiltinRegistries.registerSimple(Registry.FLAT_LEVEL_GENERATOR_PRESET_REGISTRY, FlatLevelGeneratorPresets::bootstrap);
    public static final RegistryAccess ACCESS;

    private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, Supplier<? extends Holder<? extends T>> supplier) {
        return BuiltinRegistries.registerSimple(resourceKey, Lifecycle.stable(), supplier);
    }

    private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, Supplier<? extends Holder<? extends T>> supplier) {
        return BuiltinRegistries.internalRegister(resourceKey, new MappedRegistry(resourceKey, lifecycle, null), supplier, lifecycle);
    }

    private static <T, R extends WritableRegistry<T>> R internalRegister(ResourceKey<? extends Registry<T>> resourceKey, R writableRegistry, Supplier<? extends Holder<? extends T>> supplier, Lifecycle lifecycle) {
        ResourceLocation resourceLocation = resourceKey.location();
        LOADERS.put(resourceLocation, supplier);
        WRITABLE_REGISTRY.register(resourceKey, writableRegistry, lifecycle);
        return writableRegistry;
    }

    public static <V extends T, T> Holder<V> registerExact(Registry<T> registry, String string, V object) {
        Holder<T> holder = BuiltinRegistries.register(registry, new ResourceLocation(string), object);
        return holder;
    }

    public static <T> Holder<T> register(Registry<T> registry, String string, T object) {
        return BuiltinRegistries.register(registry, new ResourceLocation(string), object);
    }

    public static <T> Holder<T> register(Registry<T> registry, ResourceLocation resourceLocation, T object) {
        return BuiltinRegistries.register(registry, ResourceKey.create(registry.key(), resourceLocation), object);
    }

    public static <T> Holder<T> register(Registry<T> registry, ResourceKey<T> resourceKey, T object) {
        return ((WritableRegistry)registry).register(resourceKey, object, Lifecycle.stable());
    }

    public static void bootstrap() {
    }

    static {
        LOADERS.forEach((resourceLocation, supplier) -> {
            if (!((Holder)supplier.get()).isBound()) {
                LOGGER.error("Unable to bootstrap registry '{}'", resourceLocation);
            }
        });
        Registry.checkRegistry(WRITABLE_REGISTRY);
        ACCESS = RegistryAccess.fromRegistryOfRegistries(REGISTRY);
    }
}

