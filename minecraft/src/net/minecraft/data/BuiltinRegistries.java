package net.minecraft.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BuiltinRegistries {
	protected static final Logger LOGGER = LogManager.getLogger();
	private static final Map<ResourceLocation, Supplier<?>> LOADERS = Maps.<ResourceLocation, Supplier<?>>newLinkedHashMap();
	private static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY = new MappedRegistry<>(
		ResourceKey.createRegistryKey(new ResourceLocation("root")), Lifecycle.experimental()
	);
	public static final Registry<? extends Registry<?>> REGISTRY = WRITABLE_REGISTRY;
	public static final Registry<ConfiguredSurfaceBuilder<?>> CONFIGURED_SURFACE_BUILDER = registerSimple(
		Registry.CONFIGURED_SURFACE_BUILDER_REGISTRY, () -> SurfaceBuilders.NOPE
	);
	public static final Registry<ConfiguredWorldCarver<?>> CONFIGURED_CARVER = registerSimple(Registry.CONFIGURED_CARVER_REGISTRY, () -> Carvers.CAVE);
	public static final Registry<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE = registerSimple(Registry.CONFIGURED_FEATURE_REGISTRY, () -> Features.NOPE);
	public static final Registry<ConfiguredStructureFeature<?, ?>> CONFIGURED_STRUCTURE_FEATURE = registerSimple(
		Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, () -> StructureFeatures.MINESHAFT
	);
	public static final Registry<ImmutableList<StructureProcessor>> PROCESSOR_LIST = registerSimple(
		Registry.PROCESSOR_LIST_REGISTRY, () -> ProcessorLists.ZOMBIE_PLAINS
	);
	public static final Registry<StructureTemplatePool> TEMPLATE_POOL = registerSimple(Registry.TEMPLATE_POOL_REGISTRY, () -> Pools.EMPTY);
	public static final Registry<Biome> BIOME = registerSimple(Registry.BIOME_REGISTRY, () -> Biomes.DEFAULT);

	private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, Supplier<T> supplier) {
		return registerSimple(resourceKey, Lifecycle.experimental(), supplier);
	}

	private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, Supplier<T> supplier) {
		return internalRegister(resourceKey, new MappedRegistry<>(resourceKey, lifecycle), supplier);
	}

	private static <T, R extends WritableRegistry<T>> R internalRegister(ResourceKey<? extends Registry<T>> resourceKey, R writableRegistry, Supplier<T> supplier) {
		ResourceLocation resourceLocation = resourceKey.location();
		LOADERS.put(resourceLocation, supplier);
		WritableRegistry<R> writableRegistry2 = WRITABLE_REGISTRY;
		return writableRegistry2.register((ResourceKey<R>)resourceKey, writableRegistry);
	}

	public static <T> T register(Registry<? super T> registry, String string, T object) {
		return register(registry, new ResourceLocation(string), object);
	}

	public static <V, T extends V> T register(Registry<V> registry, ResourceLocation resourceLocation, T object) {
		return ((WritableRegistry)registry).register(ResourceKey.create(registry.key(), resourceLocation), object);
	}

	public static <V, T extends V> T registerMapping(Registry<V> registry, int i, String string, T object) {
		return ((WritableRegistry)registry).registerMapping(i, ResourceKey.create(registry.key(), new ResourceLocation(string)), object);
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
