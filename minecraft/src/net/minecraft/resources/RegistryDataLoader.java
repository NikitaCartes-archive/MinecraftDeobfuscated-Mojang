package net.minecraft.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.slf4j.Logger;

public class RegistryDataLoader {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final List<RegistryDataLoader.RegistryData<?>> WORLDGEN_REGISTRIES = List.of(
		new RegistryDataLoader.RegistryData<>(Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registry.CHAT_TYPE_REGISTRY, ChatType.CODEC),
		new RegistryDataLoader.RegistryData<>(Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registry.PLACED_FEATURE_REGISTRY, PlacedFeature.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registry.STRUCTURE_REGISTRY, Structure.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registry.STRUCTURE_SET_REGISTRY, StructureSet.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registry.NOISE_REGISTRY, NormalNoise.NoiseParameters.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registry.DENSITY_FUNCTION_REGISTRY, DensityFunction.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registry.WORLD_PRESET_REGISTRY, WorldPreset.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registry.FLAT_LEVEL_GENERATOR_PRESET_REGISTRY, FlatLevelGeneratorPreset.DIRECT_CODEC)
	);
	public static final List<RegistryDataLoader.RegistryData<?>> DIMENSION_REGISTRIES = List.of(
		new RegistryDataLoader.RegistryData<>(Registry.LEVEL_STEM_REGISTRY, LevelStem.CODEC)
	);

	public static RegistryAccess.Frozen load(ResourceManager resourceManager, RegistryAccess registryAccess, List<RegistryDataLoader.RegistryData<?>> list) {
		Map<ResourceKey<?>, Exception> map = new HashMap();
		List<Pair<Registry<?>, RegistryDataLoader.Loader>> list2 = list.stream().map(registryData -> registryData.create(Lifecycle.stable(), map)).toList();
		RegistryAccess registryAccess2 = new RegistryAccess.ImmutableRegistryAccess(list2.stream().map(Pair::getFirst).toList());
		RegistryAccess registryAccess3 = new RegistryAccess.ImmutableRegistryAccess(Stream.concat(registryAccess.registries(), registryAccess2.registries()));
		list2.forEach(pair -> ((RegistryDataLoader.Loader)pair.getSecond()).load(resourceManager, registryAccess3));
		list2.forEach(pair -> {
			Registry<?> registry = (Registry<?>)pair.getFirst();

			try {
				registry.freeze();
			} catch (Exception var4x) {
				map.put(registry.key(), var4x);
			}
		});
		if (!map.isEmpty()) {
			logErrors(map);
			throw new IllegalStateException("Failed to load registries due to above errors");
		} else {
			return registryAccess2.freeze();
		}
	}

	private static void logErrors(Map<ResourceKey<?>, Exception> map) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		Map<ResourceLocation, Map<ResourceLocation, Exception>> map2 = (Map<ResourceLocation, Map<ResourceLocation, Exception>>)map.entrySet()
			.stream()
			.collect(
				Collectors.groupingBy(
					entry -> ((ResourceKey)entry.getKey()).registry(), Collectors.toMap(entry -> ((ResourceKey)entry.getKey()).location(), Entry::getValue)
				)
			);
		map2.entrySet().stream().sorted(Entry.comparingByKey()).forEach(entry -> {
			printWriter.printf("> Errors in registry %s:%n", entry.getKey());
			((Map)entry.getValue()).entrySet().stream().sorted(Entry.comparingByKey()).forEach(entryx -> {
				printWriter.printf(">> Errors in element %s:%n", entryx.getKey());
				((Exception)entryx.getValue()).printStackTrace(printWriter);
			});
		});
		printWriter.flush();
		LOGGER.error("Registry loading errors:\n{}", stringWriter);
	}

	private static String registryDirPath(ResourceLocation resourceLocation) {
		return resourceLocation.getPath();
	}

	static <E> void loadRegistryContents(
		RegistryAccess registryAccess,
		ResourceManager resourceManager,
		ResourceKey<? extends Registry<E>> resourceKey,
		WritableRegistry<E> writableRegistry,
		Decoder<E> decoder,
		Map<ResourceKey<?>, Exception> map
	) {
		String string = registryDirPath(resourceKey.location());
		FileToIdConverter fileToIdConverter = FileToIdConverter.json(string);
		RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

		for (Entry<ResourceLocation, Resource> entry : fileToIdConverter.listMatchingResources(resourceManager).entrySet()) {
			ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
			ResourceKey<E> resourceKey2 = ResourceKey.create(resourceKey, fileToIdConverter.fileToId(resourceLocation));
			Resource resource = (Resource)entry.getValue();

			try {
				Reader reader = resource.openAsReader();

				try {
					JsonElement jsonElement = JsonParser.parseReader(reader);
					DataResult<E> dataResult = decoder.parse(registryOps, jsonElement);
					E object = dataResult.getOrThrow(false, stringx -> {
					});
					writableRegistry.register(resourceKey2, object, resource.isBuiltin() ? Lifecycle.stable() : dataResult.lifecycle());
				} catch (Throwable var19) {
					if (reader != null) {
						try {
							reader.close();
						} catch (Throwable var18) {
							var19.addSuppressed(var18);
						}
					}

					throw var19;
				}

				if (reader != null) {
					reader.close();
				}
			} catch (Exception var20) {
				map.put(resourceKey2, new IllegalStateException("Failed to parse %s from pack %s".formatted(resourceLocation, resource.sourcePackId()), var20));
			}
		}
	}

	interface Loader {
		void load(ResourceManager resourceManager, RegistryAccess registryAccess);
	}

	public static record RegistryData<T>(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec) {
		public Pair<Registry<?>, RegistryDataLoader.Loader> create(Lifecycle lifecycle, Map<ResourceKey<?>, Exception> map) {
			WritableRegistry<T> writableRegistry = new MappedRegistry<>(this.key, lifecycle);
			RegistryDataLoader.Loader loader = (resourceManager, registryAccess) -> RegistryDataLoader.loadRegistryContents(
					registryAccess, resourceManager, this.key, writableRegistry, this.elementCodec, map
				);
			return Pair.of(writableRegistry, loader);
		}
	}
}
