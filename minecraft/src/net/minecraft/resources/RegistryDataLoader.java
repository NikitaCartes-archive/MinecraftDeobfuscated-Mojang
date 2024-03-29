package net.minecraft.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.entity.BannerPattern;
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
	private static final RegistrationInfo NETWORK_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());
	private static final Function<Optional<KnownPack>, RegistrationInfo> REGISTRATION_INFO_CACHE = Util.memoize((Function)(optional -> {
		Lifecycle lifecycle = (Lifecycle)optional.map(KnownPack::isVanilla).map(boolean_ -> Lifecycle.stable()).orElse(Lifecycle.experimental());
		return new RegistrationInfo(optional, lifecycle);
	}));
	public static final List<RegistryDataLoader.RegistryData<?>> WORLDGEN_REGISTRIES = List.of(
		new RegistryDataLoader.RegistryData(Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registries.BIOME, Biome.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.CHAT_TYPE, ChatType.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.CONFIGURED_CARVER, ConfiguredWorldCarver.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.CONFIGURED_FEATURE, ConfiguredFeature.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.PLACED_FEATURE, PlacedFeature.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registries.STRUCTURE, Structure.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registries.PROCESSOR_LIST, StructureProcessorType.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registries.TEMPLATE_POOL, StructureTemplatePool.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.NOISE_SETTINGS, NoiseGeneratorSettings.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.NOISE, NormalNoise.NoiseParameters.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registries.DENSITY_FUNCTION, DensityFunction.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registries.WORLD_PRESET, WorldPreset.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPreset.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registries.WOLF_VARIANT, WolfVariant.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<>(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterList.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.BANNER_PATTERN, BannerPattern.DIRECT_CODEC)
	);
	public static final List<RegistryDataLoader.RegistryData<?>> DIMENSION_REGISTRIES = List.of(
		new RegistryDataLoader.RegistryData(Registries.LEVEL_STEM, LevelStem.CODEC)
	);
	public static final List<RegistryDataLoader.RegistryData<?>> SYNCHRONIZED_REGISTRIES = List.of(
		new RegistryDataLoader.RegistryData<Biome>(Registries.BIOME, Biome.NETWORK_CODEC),
		new RegistryDataLoader.RegistryData(Registries.CHAT_TYPE, ChatType.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData<WolfVariant>(Registries.WOLF_VARIANT, WolfVariant.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC),
		new RegistryDataLoader.RegistryData(Registries.BANNER_PATTERN, BannerPattern.DIRECT_CODEC)
	);

	public static RegistryAccess.Frozen load(ResourceManager resourceManager, RegistryAccess registryAccess, List<RegistryDataLoader.RegistryData<?>> list) {
		return load((loader, registryInfoLookup) -> loader.loadFromResources(resourceManager, registryInfoLookup), registryAccess, list);
	}

	public static RegistryAccess.Frozen load(
		Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> map,
		ResourceProvider resourceProvider,
		RegistryAccess registryAccess,
		List<RegistryDataLoader.RegistryData<?>> list
	) {
		return load((loader, registryInfoLookup) -> loader.loadFromNetwork(map, resourceProvider, registryInfoLookup), registryAccess, list);
	}

	public static RegistryAccess.Frozen load(
		RegistryDataLoader.LoadingFunction loadingFunction, RegistryAccess registryAccess, List<RegistryDataLoader.RegistryData<?>> list
	) {
		Map<ResourceKey<?>, Exception> map = new HashMap();
		List<RegistryDataLoader.Loader<?>> list2 = (List)list.stream()
			.map(registryData -> registryData.create(Lifecycle.stable(), map))
			.collect(Collectors.toUnmodifiableList());
		RegistryOps.RegistryInfoLookup registryInfoLookup = createContext(registryAccess, list2);
		list2.forEach(loader -> loadingFunction.apply(loader, registryInfoLookup));
		list2.forEach(loader -> {
			Registry<?> registry = loader.registry();

			try {
				registry.freeze();
			} catch (Exception var4xx) {
				map.put(registry.key(), var4xx);
			}
		});
		if (!map.isEmpty()) {
			logErrors(map);
			throw new IllegalStateException("Failed to load registries due to above errors");
		} else {
			return new RegistryAccess.ImmutableRegistryAccess(list2.stream().map(RegistryDataLoader.Loader::registry).toList()).freeze();
		}
	}

	private static RegistryOps.RegistryInfoLookup createContext(RegistryAccess registryAccess, List<RegistryDataLoader.Loader<?>> list) {
		final Map<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> map = new HashMap();
		registryAccess.registries().forEach(registryEntry -> map.put(registryEntry.key(), createInfoForContextRegistry(registryEntry.value())));
		list.forEach(loader -> map.put(loader.registry.key(), createInfoForNewRegistry(loader.registry)));
		return new RegistryOps.RegistryInfoLookup() {
			@Override
			public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
				return Optional.ofNullable((RegistryOps.RegistryInfo)map.get(resourceKey));
			}
		};
	}

	private static <T> RegistryOps.RegistryInfo<T> createInfoForNewRegistry(WritableRegistry<T> writableRegistry) {
		return new RegistryOps.RegistryInfo<>(writableRegistry.asLookup(), writableRegistry.createRegistrationLookup(), writableRegistry.registryLifecycle());
	}

	private static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(Registry<T> registry) {
		return new RegistryOps.RegistryInfo<>(registry.asLookup(), registry.asTagAddingLookup(), registry.registryLifecycle());
	}

	private static void logErrors(Map<ResourceKey<?>, Exception> map) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		Map<ResourceLocation, Map<ResourceLocation, Exception>> map2 = (Map)map.entrySet()
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

	private static <E> void loadElementFromResource(
		WritableRegistry<E> writableRegistry,
		Decoder<E> decoder,
		RegistryOps<JsonElement> registryOps,
		ResourceKey<E> resourceKey,
		Resource resource,
		RegistrationInfo registrationInfo
	) throws IOException {
		Reader reader = resource.openAsReader();

		try {
			JsonElement jsonElement = JsonParser.parseReader(reader);
			DataResult<E> dataResult = decoder.parse(registryOps, jsonElement);
			E object = dataResult.getOrThrow();
			writableRegistry.register(resourceKey, object, registrationInfo);
		} catch (Throwable var11) {
			if (reader != null) {
				try {
					reader.close();
				} catch (Throwable var10) {
					var11.addSuppressed(var10);
				}
			}

			throw var11;
		}

		if (reader != null) {
			reader.close();
		}
	}

	static <E> void loadContentsFromManager(
		ResourceManager resourceManager,
		RegistryOps.RegistryInfoLookup registryInfoLookup,
		WritableRegistry<E> writableRegistry,
		Decoder<E> decoder,
		Map<ResourceKey<?>, Exception> map
	) {
		String string = registryDirPath(writableRegistry.key().location());
		FileToIdConverter fileToIdConverter = FileToIdConverter.json(string);
		RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryInfoLookup);

		for(Entry<ResourceLocation, Resource> entry : fileToIdConverter.listMatchingResources(resourceManager).entrySet()) {
			ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
			ResourceKey<E> resourceKey = ResourceKey.create(writableRegistry.key(), fileToIdConverter.fileToId(resourceLocation));
			Resource resource = (Resource)entry.getValue();
			RegistrationInfo registrationInfo = (RegistrationInfo)REGISTRATION_INFO_CACHE.apply(resource.knownPackInfo());

			try {
				loadElementFromResource(writableRegistry, decoder, registryOps, resourceKey, resource, registrationInfo);
			} catch (Exception var15) {
				map.put(
					resourceKey, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", resourceLocation, resource.sourcePackId()), var15)
				);
			}
		}
	}

	static <E> void loadContentsFromNetwork(
		Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> map,
		ResourceProvider resourceProvider,
		RegistryOps.RegistryInfoLookup registryInfoLookup,
		WritableRegistry<E> writableRegistry,
		Decoder<E> decoder,
		Map<ResourceKey<?>, Exception> map2
	) {
		List<RegistrySynchronization.PackedRegistryEntry> list = (List)map.get(writableRegistry.key());
		if (list != null) {
			RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, registryInfoLookup);
			RegistryOps<JsonElement> registryOps2 = RegistryOps.create(JsonOps.INSTANCE, registryInfoLookup);
			String string = registryDirPath(writableRegistry.key().location());
			FileToIdConverter fileToIdConverter = FileToIdConverter.json(string);

			for(RegistrySynchronization.PackedRegistryEntry packedRegistryEntry : list) {
				ResourceKey<E> resourceKey = ResourceKey.create(writableRegistry.key(), packedRegistryEntry.id());
				Optional<Tag> optional = packedRegistryEntry.data();
				if (optional.isPresent()) {
					try {
						DataResult<E> dataResult = decoder.parse(registryOps, (Tag)optional.get());
						E object = dataResult.getOrThrow();
						writableRegistry.register(resourceKey, object, NETWORK_REGISTRATION_INFO);
					} catch (Exception var17) {
						map2.put(resourceKey, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse value %s from server", optional.get()), var17));
					}
				} else {
					ResourceLocation resourceLocation = fileToIdConverter.idToFile(packedRegistryEntry.id());

					try {
						Resource resource = resourceProvider.getResourceOrThrow(resourceLocation);
						loadElementFromResource(writableRegistry, decoder, registryOps2, resourceKey, resource, NETWORK_REGISTRATION_INFO);
					} catch (Exception var18) {
						map2.put(resourceKey, new IllegalStateException("Failed to parse local data", var18));
					}
				}
			}
		}
	}

	static record Loader<T>(RegistryDataLoader.RegistryData<T> data, WritableRegistry<T> registry, Map<ResourceKey<?>, Exception> loadingErrors) {
		final WritableRegistry<T> registry;

		public void loadFromResources(ResourceManager resourceManager, RegistryOps.RegistryInfoLookup registryInfoLookup) {
			RegistryDataLoader.loadContentsFromManager(resourceManager, registryInfoLookup, this.registry, this.data.elementCodec, this.loadingErrors);
		}

		public void loadFromNetwork(
			Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> map,
			ResourceProvider resourceProvider,
			RegistryOps.RegistryInfoLookup registryInfoLookup
		) {
			RegistryDataLoader.loadContentsFromNetwork(map, resourceProvider, registryInfoLookup, this.registry, this.data.elementCodec, this.loadingErrors);
		}
	}

	@FunctionalInterface
	interface LoadingFunction {
		void apply(RegistryDataLoader.Loader<?> loader, RegistryOps.RegistryInfoLookup registryInfoLookup);
	}

	public static record RegistryData<T>(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec) {
		final Codec<T> elementCodec;

		RegistryDataLoader.Loader<T> create(Lifecycle lifecycle, Map<ResourceKey<?>, Exception> map) {
			WritableRegistry<T> writableRegistry = new MappedRegistry<>(this.key, lifecycle);
			return new RegistryDataLoader.Loader<>(this, writableRegistry, map);
		}

		public void runWithArguments(BiConsumer<ResourceKey<? extends Registry<T>>, Codec<T>> biConsumer) {
			biConsumer.accept(this.key, this.elementCodec);
		}
	}
}
