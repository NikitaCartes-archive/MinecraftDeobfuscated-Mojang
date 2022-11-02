/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.slf4j.Logger;

public class RegistryDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final List<RegistryData<?>> WORLDGEN_REGISTRIES = List.of(new RegistryData<DimensionType>(Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC), new RegistryData<Biome>(Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC), new RegistryData<ChatType>(Registry.CHAT_TYPE_REGISTRY, ChatType.CODEC), new RegistryData(Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC), new RegistryData(Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC), new RegistryData<PlacedFeature>(Registry.PLACED_FEATURE_REGISTRY, PlacedFeature.DIRECT_CODEC), new RegistryData<Structure>(Registry.STRUCTURE_REGISTRY, Structure.DIRECT_CODEC), new RegistryData<StructureSet>(Registry.STRUCTURE_SET_REGISTRY, StructureSet.DIRECT_CODEC), new RegistryData<StructureProcessorList>(Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC), new RegistryData<StructureTemplatePool>(Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC), new RegistryData<NoiseGeneratorSettings>(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings.DIRECT_CODEC), new RegistryData<NormalNoise.NoiseParameters>(Registry.NOISE_REGISTRY, NormalNoise.NoiseParameters.DIRECT_CODEC), new RegistryData<DensityFunction>(Registry.DENSITY_FUNCTION_REGISTRY, DensityFunction.DIRECT_CODEC), new RegistryData<WorldPreset>(Registry.WORLD_PRESET_REGISTRY, WorldPreset.DIRECT_CODEC), new RegistryData<FlatLevelGeneratorPreset>(Registry.FLAT_LEVEL_GENERATOR_PRESET_REGISTRY, FlatLevelGeneratorPreset.DIRECT_CODEC));
    public static final List<RegistryData<?>> DIMENSION_REGISTRIES = List.of(new RegistryData<LevelStem>(Registry.LEVEL_STEM_REGISTRY, LevelStem.CODEC));

    public static RegistryAccess.Frozen load(ResourceManager resourceManager, RegistryAccess registryAccess, List<RegistryData<?>> list) {
        HashMap map = new HashMap();
        List<Pair<WritableRegistry<?>, Loader>> list2 = list.stream().map(registryData -> registryData.create(Lifecycle.stable(), map)).toList();
        RegistryOps.RegistryInfoLookup registryInfoLookup = RegistryDataLoader.createContext(registryAccess, list2);
        list2.forEach(pair -> ((Loader)pair.getSecond()).load(resourceManager, registryInfoLookup));
        list2.forEach(pair -> {
            Registry registry = (Registry)pair.getFirst();
            try {
                registry.freeze();
            } catch (Exception exception) {
                map.put(registry.key(), exception);
            }
        });
        if (!map.isEmpty()) {
            RegistryDataLoader.logErrors(map);
            throw new IllegalStateException("Failed to load registries due to above errors");
        }
        return new RegistryAccess.ImmutableRegistryAccess(list2.stream().map(Pair::getFirst).toList()).freeze();
    }

    private static RegistryOps.RegistryInfoLookup createContext(RegistryAccess registryAccess, List<Pair<WritableRegistry<?>, Loader>> list) {
        final HashMap map = new HashMap();
        registryAccess.registries().forEach(registryEntry -> map.put(registryEntry.key(), RegistryDataLoader.createInfoForContextRegistry(registryEntry.value())));
        list.forEach(pair -> map.put(((WritableRegistry)pair.getFirst()).key(), RegistryDataLoader.createInfoForNewRegistry((WritableRegistry)pair.getFirst())));
        return new RegistryOps.RegistryInfoLookup(){

            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                return Optional.ofNullable((RegistryOps.RegistryInfo)map.get(resourceKey));
            }
        };
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForNewRegistry(WritableRegistry<T> writableRegistry) {
        return new RegistryOps.RegistryInfo(writableRegistry.asLookup(), writableRegistry.createRegistrationLookup(), writableRegistry.elementsLifecycle());
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(Registry<T> registry) {
        return new RegistryOps.RegistryInfo<T>(registry.asLookup(), registry.asTagAddingLookup(), registry.elementsLifecycle());
    }

    private static void logErrors(Map<ResourceKey<?>, Exception> map) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Map<ResourceLocation, Map<ResourceLocation, Exception>> map2 = map.entrySet().stream().collect(Collectors.groupingBy(entry -> ((ResourceKey)entry.getKey()).registry(), Collectors.toMap(entry -> ((ResourceKey)entry.getKey()).location(), Map.Entry::getValue)));
        map2.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry2 -> {
            printWriter.printf("> Errors in registry %s:%n", entry2.getKey());
            ((Map)entry2.getValue()).entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                printWriter.printf(">> Errors in element %s:%n", entry.getKey());
                ((Exception)entry.getValue()).printStackTrace(printWriter);
            });
        });
        printWriter.flush();
        LOGGER.error("Registry loading errors:\n{}", (Object)stringWriter);
    }

    private static String registryDirPath(ResourceLocation resourceLocation) {
        return resourceLocation.getPath();
    }

    static <E> void loadRegistryContents(RegistryOps.RegistryInfoLookup registryInfoLookup, ResourceManager resourceManager, ResourceKey<? extends Registry<E>> resourceKey, WritableRegistry<E> writableRegistry, Decoder<E> decoder, Map<ResourceKey<?>, Exception> map) {
        String string2 = RegistryDataLoader.registryDirPath(resourceKey.location());
        FileToIdConverter fileToIdConverter = FileToIdConverter.json(string2);
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryInfoLookup);
        for (Map.Entry<ResourceLocation, Resource> entry : fileToIdConverter.listMatchingResources(resourceManager).entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            ResourceKey resourceKey2 = ResourceKey.create(resourceKey, fileToIdConverter.fileToId(resourceLocation));
            Resource resource = entry.getValue();
            try {
                BufferedReader reader = resource.openAsReader();
                try {
                    JsonElement jsonElement = JsonParser.parseReader(reader);
                    DataResult<E> dataResult = decoder.parse(registryOps, jsonElement);
                    E object = dataResult.getOrThrow(false, string -> {});
                    writableRegistry.register(resourceKey2, object, resource.isBuiltin() ? Lifecycle.stable() : dataResult.lifecycle());
                } finally {
                    if (reader == null) continue;
                    ((Reader)reader).close();
                }
            } catch (Exception exception) {
                map.put(resourceKey2, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", resourceLocation, resource.sourcePackId()), exception));
            }
        }
    }

    static interface Loader {
        public void load(ResourceManager var1, RegistryOps.RegistryInfoLookup var2);
    }

    public record RegistryData<T>(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec) {
        Pair<WritableRegistry<?>, Loader> create(Lifecycle lifecycle, Map<ResourceKey<?>, Exception> map) {
            MappedRegistry writableRegistry = new MappedRegistry(this.key, lifecycle);
            Loader loader = (resourceManager, registryInfoLookup) -> RegistryDataLoader.loadRegistryContents(registryInfoLookup, resourceManager, this.key, writableRegistry, this.elementCodec, map);
            return Pair.of(writableRegistry, loader);
        }
    }
}

