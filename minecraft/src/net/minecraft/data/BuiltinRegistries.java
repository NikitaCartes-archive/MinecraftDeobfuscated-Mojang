package net.minecraft.data;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.slf4j.Logger;

public class BuiltinRegistries {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<ResourceLocation, Supplier<?>> LOADERS = Maps.<ResourceLocation, Supplier<?>>newLinkedHashMap();
	private static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY = new MappedRegistry<>(
		ResourceKey.createRegistryKey(new ResourceLocation("root")), Lifecycle.experimental()
	);
	public static final Registry<? extends Registry<?>> REGISTRY = WRITABLE_REGISTRY;
	public static final Registry<ConfiguredWorldCarver<?>> CONFIGURED_CARVER = registerSimple(Registry.CONFIGURED_CARVER_REGISTRY, () -> Carvers.CAVE);
	public static final Registry<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE = registerSimple(Registry.CONFIGURED_FEATURE_REGISTRY, FeatureUtils::bootstrap);
	public static final Registry<ConfiguredStructureFeature<?, ?>> CONFIGURED_STRUCTURE_FEATURE = registerSimple(
		Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, StructureFeatures::bootstrap
	);
	public static final Registry<PlacedFeature> PLACED_FEATURE = registerSimple(Registry.PLACED_FEATURE_REGISTRY, PlacementUtils::bootstrap);
	public static final Registry<StructureProcessorList> PROCESSOR_LIST = registerSimple(Registry.PROCESSOR_LIST_REGISTRY, () -> ProcessorLists.ZOMBIE_PLAINS);
	public static final Registry<StructureTemplatePool> TEMPLATE_POOL = registerSimple(Registry.TEMPLATE_POOL_REGISTRY, Pools::bootstrap);
	public static final Registry<Biome> BIOME = registerSimple(Registry.BIOME_REGISTRY, () -> Biomes.PLAINS);
	public static final Registry<NoiseGeneratorSettings> NOISE_GENERATOR_SETTINGS = registerSimple(
		Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings::bootstrap
	);
	public static final Registry<NormalNoise.NoiseParameters> NOISE = registerSimple(Registry.NOISE_REGISTRY, NoiseData::bootstrap);

	private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, Supplier<T> supplier) {
		return registerSimple(resourceKey, Lifecycle.stable(), supplier);
	}

	private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, Supplier<T> supplier) {
		return internalRegister(resourceKey, new MappedRegistry<>(resourceKey, lifecycle), supplier, lifecycle);
	}

	private static <T, R extends WritableRegistry<T>> R internalRegister(
		ResourceKey<? extends Registry<T>> resourceKey, R writableRegistry, Supplier<T> supplier, Lifecycle lifecycle
	) {
		ResourceLocation resourceLocation = resourceKey.location();
		LOADERS.put(resourceLocation, supplier);
		WritableRegistry<R> writableRegistry2 = WRITABLE_REGISTRY;
		return writableRegistry2.register((ResourceKey<R>)resourceKey, writableRegistry, lifecycle);
	}

	public static <T> T register(Registry<? super T> registry, String string, T object) {
		return register(registry, new ResourceLocation(string), object);
	}

	public static <V, T extends V> T register(Registry<V> registry, ResourceLocation resourceLocation, T object) {
		return register(registry, ResourceKey.create(registry.key(), resourceLocation), object);
	}

	public static <V, T extends V> T register(Registry<V> registry, ResourceKey<V> resourceKey, T object) {
		return ((WritableRegistry)registry).register(resourceKey, object, Lifecycle.stable());
	}

	public static <V, T extends V> T registerMapping(Registry<V> registry, ResourceKey<V> resourceKey, T object) {
		return ((WritableRegistry)registry).register(resourceKey, object, Lifecycle.stable());
	}

	public static void bootstrap() {
	}

	static {
		LOADERS.forEach((resourceLocation, supplier) -> {
			if (supplier.get() == null) {
				LOGGER.error("Unable to bootstrap registry '{}'", resourceLocation);
			}
		});
		Registry.checkRegistry(WRITABLE_REGISTRY);
	}
}
