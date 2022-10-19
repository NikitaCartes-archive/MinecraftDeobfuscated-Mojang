package net.minecraft.data;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
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
import net.minecraft.network.chat.ChatType;
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
	private static final Map<ResourceLocation, Supplier<? extends Holder<?>>> LOADERS = Maps.<ResourceLocation, Supplier<? extends Holder<?>>>newLinkedHashMap();
	private static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY = new MappedRegistry<>(
		ResourceKey.createRegistryKey(new ResourceLocation("root")), Lifecycle.experimental()
	);
	public static final Registry<? extends Registry<?>> REGISTRY = WRITABLE_REGISTRY;
	public static final Registry<DimensionType> DIMENSION_TYPE = registerSimple(Registry.DIMENSION_TYPE_REGISTRY, DimensionTypes::bootstrap);
	public static final Registry<ConfiguredWorldCarver<?>> CONFIGURED_CARVER = registerSimple(Registry.CONFIGURED_CARVER_REGISTRY, registryx -> Carvers.CAVE);
	public static final Registry<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE = registerSimple(Registry.CONFIGURED_FEATURE_REGISTRY, FeatureUtils::bootstrap);
	public static final Registry<PlacedFeature> PLACED_FEATURE = registerSimple(Registry.PLACED_FEATURE_REGISTRY, PlacementUtils::bootstrap);
	public static final Registry<Structure> STRUCTURES = registerSimple(Registry.STRUCTURE_REGISTRY, Structures::bootstrap);
	public static final Registry<StructureSet> STRUCTURE_SETS = registerSimple(Registry.STRUCTURE_SET_REGISTRY, StructureSets::bootstrap);
	public static final Registry<StructureProcessorList> PROCESSOR_LIST = registerSimple(
		Registry.PROCESSOR_LIST_REGISTRY, registryx -> ProcessorLists.ZOMBIE_PLAINS
	);
	public static final Registry<StructureTemplatePool> TEMPLATE_POOL = registerSimple(Registry.TEMPLATE_POOL_REGISTRY, Pools::bootstrap);
	public static final Registry<Biome> BIOME = registerSimple(Registry.BIOME_REGISTRY, Biomes::bootstrap);
	public static final Registry<NormalNoise.NoiseParameters> NOISE = registerSimple(Registry.NOISE_REGISTRY, NoiseData::bootstrap);
	public static final Registry<DensityFunction> DENSITY_FUNCTION = registerSimple(Registry.DENSITY_FUNCTION_REGISTRY, NoiseRouterData::bootstrap);
	public static final Registry<NoiseGeneratorSettings> NOISE_GENERATOR_SETTINGS = registerSimple(
		Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings::bootstrap
	);
	public static final Registry<WorldPreset> WORLD_PRESET = registerSimple(Registry.WORLD_PRESET_REGISTRY, WorldPresets::bootstrap);
	public static final Registry<FlatLevelGeneratorPreset> FLAT_LEVEL_GENERATOR_PRESET = registerSimple(
		Registry.FLAT_LEVEL_GENERATOR_PRESET_REGISTRY, FlatLevelGeneratorPresets::bootstrap
	);
	public static final Registry<ChatType> CHAT_TYPE = registerSimple(Registry.CHAT_TYPE_REGISTRY, ChatType::bootstrap);

	private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, BuiltinRegistries.RegistryBootstrap<T> registryBootstrap) {
		return registerSimple(resourceKey, Lifecycle.stable(), registryBootstrap);
	}

	private static <T> Registry<T> registerSimple(
		ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, BuiltinRegistries.RegistryBootstrap<T> registryBootstrap
	) {
		return internalRegister(resourceKey, new MappedRegistry<>(resourceKey, lifecycle), registryBootstrap, lifecycle);
	}

	private static <T, R extends WritableRegistry<T>> R internalRegister(
		ResourceKey<? extends Registry<T>> resourceKey, R writableRegistry, BuiltinRegistries.RegistryBootstrap<T> registryBootstrap, Lifecycle lifecycle
	) {
		ResourceLocation resourceLocation = resourceKey.location();
		LOADERS.put(resourceLocation, (Supplier)() -> registryBootstrap.run(writableRegistry));
		WRITABLE_REGISTRY.register((ResourceKey<WritableRegistry<?>>)resourceKey, writableRegistry, lifecycle);
		return writableRegistry;
	}

	public static RegistryAccess.Frozen createAccess() {
		RegistryAccess.Frozen frozen = RegistryAccess.fromRegistryOfRegistries(Registry.REGISTRY);
		RegistryAccess.Frozen frozen2 = RegistryAccess.fromRegistryOfRegistries(REGISTRY);
		return new RegistryAccess.ImmutableRegistryAccess(Stream.concat(frozen.registries(), frozen2.registries())).freeze();
	}

	public static <V extends T, T> Holder<V> registerExact(Registry<T> registry, String string, V object) {
		return register(registry, new ResourceLocation(string), object);
	}

	public static <T> Holder<T> register(Registry<T> registry, String string, T object) {
		return register(registry, new ResourceLocation(string), object);
	}

	public static <T> Holder<T> register(Registry<T> registry, ResourceLocation resourceLocation, T object) {
		return register(registry, ResourceKey.create(registry.key(), resourceLocation), object);
	}

	public static <T> Holder<T> register(Registry<T> registry, ResourceKey<T> resourceKey, T object) {
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
		REGISTRY.freeze();

		for (Registry<?> registry : REGISTRY) {
			registry.freeze();
		}

		Registry.checkRegistry(REGISTRY);
	}

	@FunctionalInterface
	interface RegistryBootstrap<T> {
		Holder<? extends T> run(Registry<T> registry);
	}
}
