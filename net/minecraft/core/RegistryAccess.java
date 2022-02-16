/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public interface RegistryAccess {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<ResourceKey<? extends Registry<?>>, RegistryData<?>> REGISTRIES = Util.make(() -> {
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
    public static final Codec<RegistryAccess> NETWORK_CODEC = RegistryAccess.makeNetworkCodec();
    public static final Supplier<Frozen> BUILTIN = Suppliers.memoize(() -> RegistryAccess.builtinCopy().freeze());

    public <E> Optional<Registry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> var1);

    default public <E> Registry<E> ownedRegistryOrThrow(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        return this.ownedRegistry(resourceKey).orElseThrow(() -> new IllegalStateException("Missing registry: " + resourceKey));
    }

    default public <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
        Optional<Registry<E>> optional = this.ownedRegistry(resourceKey);
        if (optional.isPresent()) {
            return optional;
        }
        return Registry.REGISTRY.getOptional(resourceKey.location());
    }

    default public <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> resourceKey) {
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

    public Stream<RegistryEntry<?>> ownedRegistries();

    private static Stream<RegistryEntry<Object>> globalRegistries() {
        return Registry.REGISTRY.holders().map(RegistryEntry::fromHolder);
    }

    default public Stream<RegistryEntry<?>> registries() {
        return Stream.concat(this.ownedRegistries(), RegistryAccess.globalRegistries());
    }

    default public Stream<RegistryEntry<?>> networkSafeRegistries() {
        return Stream.concat(this.ownedNetworkableRegistries(), RegistryAccess.globalRegistries());
    }

    private static <E> Codec<RegistryAccess> makeNetworkCodec() {
        Codec<ResourceKey> codec = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
        Codec<Registry> codec2 = codec.partialDispatch("type", registry -> DataResult.success(registry.key()), resourceKey -> RegistryAccess.getNetworkCodec(resourceKey).map(codec -> RegistryCodecs.networkCodec(resourceKey, Lifecycle.experimental(), codec)));
        UnboundedMapCodec<ResourceKey, Registry> unboundedMapCodec = Codec.unboundedMap(codec, codec2);
        return RegistryAccess.captureMap(unboundedMapCodec);
    }

    private static <K extends ResourceKey<? extends Registry<?>>, V extends Registry<?>> Codec<RegistryAccess> captureMap(UnboundedMapCodec<K, V> unboundedMapCodec) {
        return unboundedMapCodec.xmap(ImmutableRegistryAccess::new, registryAccess -> registryAccess.ownedNetworkableRegistries().collect(ImmutableMap.toImmutableMap(registryEntry -> registryEntry.key(), registryEntry -> registryEntry.value())));
    }

    private Stream<RegistryEntry<?>> ownedNetworkableRegistries() {
        return this.ownedRegistries().filter(registryEntry -> REGISTRIES.get(registryEntry.key).sendToClient());
    }

    private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> resourceKey) {
        return Optional.ofNullable(REGISTRIES.get(resourceKey)).map(registryData -> registryData.networkCodec()).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown or not serializable registry: " + resourceKey));
    }

    private static Map<ResourceKey<? extends Registry<?>>, ? extends WritableRegistry<?>> createFreshRegistries() {
        return REGISTRIES.keySet().stream().collect(Collectors.toMap(Function.identity(), RegistryAccess::createRegistry));
    }

    private static Writable blankWriteable() {
        return new WritableRegistryAccess(RegistryAccess.createFreshRegistries());
    }

    public static Frozen fromRegistryOfRegistries(final Registry<? extends Registry<?>> registry) {
        return new Frozen(){

            public <T> Optional<Registry<T>> ownedRegistry(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                Registry registry2 = registry;
                return registry2.getOptional(resourceKey);
            }

            @Override
            public Stream<RegistryEntry<?>> ownedRegistries() {
                return registry.entrySet().stream().map(RegistryEntry::fromMapEntry);
            }
        };
    }

    public static Writable builtinCopy() {
        Writable writable = RegistryAccess.blankWriteable();
        RegistryResourceAccess.InMemoryStorage inMemoryStorage = new RegistryResourceAccess.InMemoryStorage();
        for (Map.Entry<ResourceKey<Registry<?>>, RegistryData<?>> entry : REGISTRIES.entrySet()) {
            if (entry.getKey().equals(Registry.DIMENSION_TYPE_REGISTRY)) continue;
            RegistryAccess.addBuiltinElements(writable, inMemoryStorage, entry.getValue());
        }
        RegistryOps.createAndLoad(JsonOps.INSTANCE, writable, inMemoryStorage);
        return DimensionType.registerBuiltin(writable);
    }

    private static <E> void addBuiltinElements(Writable writable, RegistryResourceAccess.InMemoryStorage inMemoryStorage, RegistryData<E> registryData) {
        ResourceKey<Registry<E>> resourceKey = registryData.key();
        Registry<E> registry = BuiltinRegistries.ACCESS.registryOrThrow(resourceKey);
        WritableRegistry<E> writableRegistry = writable.ownedWritableRegistryOrThrow(resourceKey);
        for (Map.Entry<ResourceKey<E>, E> entry : registry.entrySet()) {
            ResourceKey<E> resourceKey2 = entry.getKey();
            E object = entry.getValue();
            if (!RegistryAccess.isIdentityCopy(resourceKey)) {
                inMemoryStorage.add(BuiltinRegistries.ACCESS, resourceKey2, registryData.codec(), registry.getId(object), object, registry.lifecycle(object));
                continue;
            }
            writableRegistry.registerMapping(registry.getId(object), resourceKey2, object, registry.lifecycle(object));
        }
    }

    public static <E> boolean isIdentityCopy(ResourceKey<? extends Registry<E>> resourceKey) {
        return resourceKey.equals(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
    }

    public static void load(Writable writable, DynamicOps<JsonElement> dynamicOps, RegistryLoader registryLoader) {
        RegistryLoader.Bound bound = registryLoader.bind(writable);
        for (RegistryData<?> registryData : REGISTRIES.values()) {
            RegistryAccess.readRegistry(dynamicOps, bound, registryData);
        }
    }

    private static <E> void readRegistry(DynamicOps<JsonElement> dynamicOps, RegistryLoader.Bound bound, RegistryData<E> registryData) {
        DataResult<Registry<E>> dataResult = bound.overrideRegistryFromResources(registryData.key(), registryData.codec(), dynamicOps);
        dataResult.error().ifPresent(partialResult -> {
            throw new JsonParseException("Error loading registry data: " + partialResult.message());
        });
    }

    public static RegistryAccess readFromDisk(Dynamic<?> dynamic) {
        return new ImmutableRegistryAccess(REGISTRIES.keySet().stream().collect(Collectors.toMap(Function.identity(), resourceKey -> RegistryAccess.retrieveRegistry(resourceKey, dynamic))));
    }

    public static <E> Registry<E> retrieveRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey, Dynamic<?> dynamic) {
        return (Registry)RegistryOps.retrieveRegistry(resourceKey).codec().parse(dynamic).resultOrPartial(Util.prefix(resourceKey + " registry: ", LOGGER::error)).orElseThrow(() -> new IllegalStateException("Failed to get " + resourceKey + " registry"));
    }

    public static <E> WritableRegistry<?> createRegistry(ResourceKey<? extends Registry<?>> resourceKey) {
        return new MappedRegistry(resourceKey, Lifecycle.stable(), null);
    }

    default public Frozen freeze() {
        return new ImmutableRegistryAccess(this.ownedRegistries().map(RegistryEntry::freeze));
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

    public static final class WritableRegistryAccess
    implements Writable {
        private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends WritableRegistry<?>> registries;

        WritableRegistryAccess(Map<? extends ResourceKey<? extends Registry<?>>, ? extends WritableRegistry<?>> map) {
            this.registries = map;
        }

        @Override
        public <E> Optional<Registry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
            return Optional.ofNullable(this.registries.get(resourceKey)).map(writableRegistry -> writableRegistry);
        }

        @Override
        public <E> Optional<WritableRegistry<E>> ownedWritableRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
            return Optional.ofNullable(this.registries.get(resourceKey)).map(writableRegistry -> writableRegistry);
        }

        @Override
        public Stream<RegistryEntry<?>> ownedRegistries() {
            return this.registries.entrySet().stream().map(RegistryEntry::fromMapEntry);
        }
    }

    public static interface Writable
    extends RegistryAccess {
        public <E> Optional<WritableRegistry<E>> ownedWritableRegistry(ResourceKey<? extends Registry<? extends E>> var1);

        default public <E> WritableRegistry<E> ownedWritableRegistryOrThrow(ResourceKey<? extends Registry<? extends E>> resourceKey) {
            return this.ownedWritableRegistry(resourceKey).orElseThrow(() -> new IllegalStateException("Missing registry: " + resourceKey));
        }
    }

    public static final class ImmutableRegistryAccess
    implements Frozen {
        private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> registries;

        public ImmutableRegistryAccess(Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> map) {
            this.registries = Map.copyOf(map);
        }

        ImmutableRegistryAccess(Stream<RegistryEntry<?>> stream) {
            this.registries = stream.collect(ImmutableMap.toImmutableMap(RegistryEntry::key, RegistryEntry::value));
        }

        @Override
        public <E> Optional<Registry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
            return Optional.ofNullable(this.registries.get(resourceKey)).map(registry -> registry);
        }

        @Override
        public Stream<RegistryEntry<?>> ownedRegistries() {
            return this.registries.entrySet().stream().map(RegistryEntry::fromMapEntry);
        }
    }

    public static interface Frozen
    extends RegistryAccess {
        @Override
        default public Frozen freeze() {
            return this;
        }
    }

    public record RegistryEntry<T>(ResourceKey<? extends Registry<T>> key, Registry<T> value) {
        private static <T, R extends Registry<? extends T>> RegistryEntry<T> fromMapEntry(Map.Entry<? extends ResourceKey<? extends Registry<?>>, R> entry) {
            return RegistryEntry.fromUntyped(entry.getKey(), (Registry)entry.getValue());
        }

        private static <T> RegistryEntry<T> fromHolder(Holder.Reference<? extends Registry<? extends T>> reference) {
            return RegistryEntry.fromUntyped(reference.key(), reference.value());
        }

        private static <T> RegistryEntry<T> fromUntyped(ResourceKey<? extends Registry<?>> resourceKey, Registry<?> registry) {
            return new RegistryEntry(resourceKey, registry);
        }

        private RegistryEntry<T> freeze() {
            return new RegistryEntry<T>(this.key, this.value.freeze());
        }
    }
}

