/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class RegistryAccess {
    static final Logger LOGGER = LogUtils.getLogger();
    static final Map<ResourceKey<? extends Registry<?>>, RegistryData<?>> REGISTRIES = Util.make(() -> {
        ImmutableMap.Builder<ResourceKey<Registry<?>>, RegistryData<?>> builder = ImmutableMap.builder();
        RegistryAccess.put(builder, Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC, DimensionType.DIRECT_CODEC);
        RegistryAccess.put(builder, Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC, Biome.NETWORK_CODEC);
        RegistryAccess.put(builder, Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC);
        RegistryAccess.put(builder, Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC, ConfiguredFeature.NETWORK_CODEC);
        RegistryAccess.put(builder, Registry.PLACED_FEATURE_REGISTRY, PlacedFeature.DIRECT_CODEC);
        RegistryAccess.put(builder, Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ConfiguredStructureFeature.DIRECT_CODEC);
        RegistryAccess.put(builder, Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC);
        RegistryAccess.put(builder, Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC);
        RegistryAccess.put(builder, Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings.DIRECT_CODEC);
        RegistryAccess.put(builder, Registry.NOISE_REGISTRY, NormalNoise.NoiseParameters.DIRECT_CODEC);
        return builder.build();
    });
    private static final RegistryHolder BUILTIN = Util.make(() -> {
        RegistryHolder registryHolder = new RegistryHolder();
        DimensionType.registerBuiltin(registryHolder);
        REGISTRIES.keySet().stream().filter(resourceKey -> !resourceKey.equals(Registry.DIMENSION_TYPE_REGISTRY)).forEach(resourceKey -> RegistryAccess.copyBuiltin(registryHolder, resourceKey));
        return registryHolder;
    });

    public abstract <E> Optional<WritableRegistry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> var1);

    public <E> WritableRegistry<E> ownedRegistryOrThrow(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return this.ownedRegistry(resourceKey).orElseThrow(() -> new IllegalStateException("Missing registry: " + resourceKey));
    }

    public <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        Optional<WritableRegistry<E>> optional = this.ownedRegistry(resourceKey);
        if (optional.isPresent()) {
            return optional;
        }
        return Registry.REGISTRY.getOptional(resourceKey.location());
    }

    public <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return this.registry(resourceKey).orElseThrow(() -> new IllegalStateException("Missing registry: " + resourceKey));
    }

    private static <E> void put(ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, RegistryData<?>> builder, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
        builder.put(resourceKey, new RegistryData<E>(resourceKey, codec, null));
    }

    private static <E> void put(ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, RegistryData<?>> builder, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec, Codec<E> codec2) {
        builder.put(resourceKey, new RegistryData<E>(resourceKey, codec, codec2));
    }

    public static Iterable<RegistryData<?>> knownRegistries() {
        return REGISTRIES.values();
    }

    public static RegistryHolder builtin() {
        RegistryHolder registryHolder = new RegistryHolder();
        RegistryResourceAccess.InMemoryStorage inMemoryStorage = new RegistryResourceAccess.InMemoryStorage();
        for (RegistryData<?> registryData : REGISTRIES.values()) {
            RegistryAccess.addBuiltinElements(registryHolder, inMemoryStorage, registryData);
        }
        RegistryReadOps.createAndLoad(JsonOps.INSTANCE, inMemoryStorage, (RegistryAccess)registryHolder);
        return registryHolder;
    }

    private static <E> void addBuiltinElements(RegistryHolder registryHolder, RegistryResourceAccess.InMemoryStorage inMemoryStorage, RegistryData<E> registryData) {
        ResourceKey<Registry<E>> resourceKey = registryData.key();
        boolean bl = !resourceKey.equals(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY) && !resourceKey.equals(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<E> registry = BUILTIN.registryOrThrow(resourceKey);
        WritableRegistry<E> writableRegistry = registryHolder.ownedRegistryOrThrow(resourceKey);
        for (Map.Entry<ResourceKey<E>, E> entry : registry.entrySet()) {
            ResourceKey<E> resourceKey2 = entry.getKey();
            E object = entry.getValue();
            if (bl) {
                inMemoryStorage.add(BUILTIN, resourceKey2, registryData.codec(), registry.getId(object), object, registry.lifecycle(object));
                continue;
            }
            writableRegistry.registerMapping(registry.getId(object), resourceKey2, object, registry.lifecycle(object));
        }
    }

    private static <R extends Registry<?>> void copyBuiltin(RegistryHolder registryHolder, ResourceKey<R> resourceKey) {
        Registry<Registry<?>> registry = BuiltinRegistries.REGISTRY;
        Registry<?> registry2 = registry.getOrThrow(resourceKey);
        RegistryAccess.copy(registryHolder, registry2);
    }

    private static <E> void copy(RegistryHolder registryHolder, Registry<E> registry) {
        WritableRegistry<E> writableRegistry = registryHolder.ownedRegistryOrThrow(registry.key());
        for (Map.Entry<ResourceKey<E>, E> entry : registry.entrySet()) {
            E object = entry.getValue();
            writableRegistry.registerMapping(registry.getId(object), entry.getKey(), object, registry.lifecycle(object));
        }
    }

    public static void load(RegistryAccess registryAccess, RegistryReadOps<?> registryReadOps) {
        for (RegistryData<?> registryData : REGISTRIES.values()) {
            RegistryAccess.readRegistry(registryReadOps, registryAccess, registryData);
        }
    }

    private static <E> void readRegistry(RegistryReadOps<?> registryReadOps, RegistryAccess registryAccess, RegistryData<E> registryData) {
        ResourceKey<Registry<E>> resourceKey = registryData.key();
        MappedRegistry mappedRegistry = (MappedRegistry)registryAccess.ownedRegistryOrThrow(resourceKey);
        DataResult<MappedRegistry<E>> dataResult = registryReadOps.decodeElements(mappedRegistry, registryData.key(), registryData.codec());
        dataResult.error().ifPresent(partialResult -> {
            throw new JsonParseException("Error loading registry data: " + partialResult.message());
        });
    }

    public record RegistryData<E>(ResourceKey<? extends Registry<E>> key, Codec<E> codec, @Nullable Codec<E> networkCodec) {
        public boolean sendToClient() {
            return this.networkCodec != null;
        }

        @Nullable
        public Codec<E> networkCodec() {
            return this.networkCodec;
        }
    }

    public static final class RegistryHolder
    extends RegistryAccess {
        public static final Codec<RegistryHolder> NETWORK_CODEC = RegistryHolder.makeNetworkCodec();
        private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> registries;

        private static <E> Codec<RegistryHolder> makeNetworkCodec() {
            Codec<ResourceKey> codec = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
            Codec<MappedRegistry> codec2 = codec.partialDispatch("type", mappedRegistry -> DataResult.success(mappedRegistry.key()), resourceKey -> RegistryHolder.getNetworkCodec(resourceKey).map(codec -> MappedRegistry.networkCodec(resourceKey, Lifecycle.experimental(), codec)));
            UnboundedMapCodec<ResourceKey, MappedRegistry> unboundedMapCodec = Codec.unboundedMap(codec, codec2);
            return RegistryHolder.captureMap(unboundedMapCodec);
        }

        private static <K extends ResourceKey<? extends Registry<?>>, V extends MappedRegistry<?>> Codec<RegistryHolder> captureMap(UnboundedMapCodec<K, V> unboundedMapCodec) {
            return unboundedMapCodec.xmap(RegistryHolder::new, registryHolder -> registryHolder.registries.entrySet().stream().filter(entry -> REGISTRIES.get(entry.getKey()).sendToClient()).collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
        }

        private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> resourceKey) {
            return Optional.ofNullable(REGISTRIES.get(resourceKey)).map(registryData -> registryData.networkCodec()).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown or not serializable registry: " + resourceKey));
        }

        public RegistryHolder() {
            this(REGISTRIES.keySet().stream().collect(Collectors.toMap(Function.identity(), RegistryHolder::createRegistry)));
        }

        public static RegistryAccess readFromDisk(Dynamic<?> dynamic) {
            return new RegistryHolder(REGISTRIES.keySet().stream().collect(Collectors.toMap(Function.identity(), resourceKey -> RegistryHolder.parseRegistry(resourceKey, dynamic))));
        }

        private static <E> MappedRegistry<?> parseRegistry(ResourceKey<? extends Registry<?>> resourceKey, Dynamic<?> dynamic) {
            return (MappedRegistry)RegistryLookupCodec.create(resourceKey).codec().parse(dynamic).resultOrPartial(Util.prefix(resourceKey + " registry: ", LOGGER::error)).orElseThrow(() -> new IllegalStateException("Failed to get " + resourceKey + " registry"));
        }

        private RegistryHolder(Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> map) {
            this.registries = map;
        }

        private static <E> MappedRegistry<?> createRegistry(ResourceKey<? extends Registry<?>> resourceKey) {
            return new MappedRegistry(resourceKey, Lifecycle.stable());
        }

        @Override
        public <E> Optional<WritableRegistry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
            return Optional.ofNullable(this.registries.get(resourceKey)).map(mappedRegistry -> mappedRegistry);
        }
    }
}

