package net.minecraft.core;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
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
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
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

public interface RegistryAccess {
	Logger LOGGER = LogUtils.getLogger();
	Map<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> REGISTRIES = Util.make(() -> {
		Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> builder = ImmutableMap.builder();
		put(builder, Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC, DimensionType.DIRECT_CODEC);
		put(builder, Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC, Biome.NETWORK_CODEC);
		put(builder, Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC);
		put(builder, Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC);
		put(builder, Registry.PLACED_FEATURE_REGISTRY, PlacedFeature.DIRECT_CODEC);
		put(builder, Registry.STRUCTURE_REGISTRY, Structure.DIRECT_CODEC);
		put(builder, Registry.STRUCTURE_SET_REGISTRY, StructureSet.DIRECT_CODEC);
		put(builder, Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC);
		put(builder, Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC);
		put(builder, Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings.DIRECT_CODEC);
		put(builder, Registry.NOISE_REGISTRY, NormalNoise.NoiseParameters.DIRECT_CODEC);
		put(builder, Registry.DENSITY_FUNCTION_REGISTRY, DensityFunction.DIRECT_CODEC);
		put(builder, Registry.WORLD_PRESET_REGISTRY, WorldPreset.DIRECT_CODEC);
		put(builder, Registry.FLAT_LEVEL_GENERATOR_PRESET_REGISTRY, FlatLevelGeneratorPreset.DIRECT_CODEC);
		return builder.build();
	});
	Codec<RegistryAccess> NETWORK_CODEC = makeNetworkCodec();
	Supplier<RegistryAccess.Frozen> BUILTIN = Suppliers.memoize(() -> builtinCopy().freeze());

	<E> Optional<Registry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey);

	default <E> Registry<E> ownedRegistryOrThrow(ResourceKey<? extends Registry<? extends E>> resourceKey) {
		return (Registry<E>)this.ownedRegistry(resourceKey).orElseThrow(() -> new IllegalStateException("Missing registry: " + resourceKey));
	}

	default <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
		Optional<? extends Registry<E>> optional = this.ownedRegistry(resourceKey);
		return optional.isPresent() ? optional : Registry.REGISTRY.getOptional(resourceKey.location());
	}

	default <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> resourceKey) {
		return (Registry<E>)this.registry(resourceKey).orElseThrow(() -> new IllegalStateException("Missing registry: " + resourceKey));
	}

	private static <E> void put(
		Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> builder, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec
	) {
		builder.put(resourceKey, new RegistryAccess.RegistryData<>(resourceKey, codec, null));
	}

	private static <E> void put(
		Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> builder,
		ResourceKey<? extends Registry<E>> resourceKey,
		Codec<E> codec,
		Codec<E> codec2
	) {
		builder.put(resourceKey, new RegistryAccess.RegistryData<>(resourceKey, codec, codec2));
	}

	static Iterable<RegistryAccess.RegistryData<?>> knownRegistries() {
		return REGISTRIES.values();
	}

	Stream<RegistryAccess.RegistryEntry<?>> ownedRegistries();

	private static Stream<RegistryAccess.RegistryEntry<Object>> globalRegistries() {
		return Registry.REGISTRY.holders().map(RegistryAccess.RegistryEntry::fromHolder);
	}

	default Stream<RegistryAccess.RegistryEntry<?>> registries() {
		return Stream.concat(this.ownedRegistries(), globalRegistries());
	}

	default Stream<RegistryAccess.RegistryEntry<?>> networkSafeRegistries() {
		return Stream.concat(this.ownedNetworkableRegistries(), globalRegistries());
	}

	private static <E> Codec<RegistryAccess> makeNetworkCodec() {
		Codec<ResourceKey<? extends Registry<E>>> codec = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
		Codec<Registry<E>> codec2 = codec.partialDispatch(
			"type",
			registry -> DataResult.success(registry.key()),
			resourceKey -> getNetworkCodec(resourceKey).map(codecx -> RegistryCodecs.networkCodec(resourceKey, Lifecycle.experimental(), codecx))
		);
		UnboundedMapCodec<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> unboundedMapCodec = Codec.unboundedMap(codec, codec2);
		return captureMap(unboundedMapCodec);
	}

	private static <K extends ResourceKey<? extends Registry<?>>, V extends Registry<?>> Codec<RegistryAccess> captureMap(
		UnboundedMapCodec<K, V> unboundedMapCodec
	) {
		return unboundedMapCodec.xmap(
			RegistryAccess.ImmutableRegistryAccess::new,
			registryAccess -> (Map)registryAccess.ownedNetworkableRegistries()
					.collect(ImmutableMap.toImmutableMap(registryEntry -> registryEntry.key(), registryEntry -> registryEntry.value()))
		);
	}

	private Stream<RegistryAccess.RegistryEntry<?>> ownedNetworkableRegistries() {
		return this.ownedRegistries().filter(registryEntry -> ((RegistryAccess.RegistryData)REGISTRIES.get(registryEntry.key)).sendToClient());
	}

	private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> resourceKey) {
		return (DataResult<? extends Codec<E>>)Optional.ofNullable((RegistryAccess.RegistryData)REGISTRIES.get(resourceKey))
			.map(registryData -> registryData.networkCodec())
			.map(DataResult::success)
			.orElseGet(() -> DataResult.error("Unknown or not serializable registry: " + resourceKey));
	}

	private static Map<ResourceKey<? extends Registry<?>>, ? extends WritableRegistry<?>> createFreshRegistries() {
		return (Map<ResourceKey<? extends Registry<?>>, ? extends WritableRegistry<?>>)REGISTRIES.keySet()
			.stream()
			.collect(Collectors.toMap(Function.identity(), RegistryAccess::createRegistry));
	}

	private static RegistryAccess.Writable blankWriteable() {
		return new RegistryAccess.WritableRegistryAccess(createFreshRegistries());
	}

	static RegistryAccess.Frozen fromRegistryOfRegistries(Registry<? extends Registry<?>> registry) {
		return new RegistryAccess.Frozen() {
			@Override
			public <T> Optional<Registry<T>> ownedRegistry(ResourceKey<? extends Registry<? extends T>> resourceKey) {
				Registry<Registry<T>> registry = (Registry<Registry<T>>)registry;
				return registry.getOptional((ResourceKey<Registry<T>>)resourceKey);
			}

			@Override
			public Stream<RegistryAccess.RegistryEntry<?>> ownedRegistries() {
				return registry.entrySet().stream().map(RegistryAccess.RegistryEntry::fromMapEntry);
			}
		};
	}

	static RegistryAccess.Writable builtinCopy() {
		RegistryAccess.Writable writable = blankWriteable();
		RegistryResourceAccess.InMemoryStorage inMemoryStorage = new RegistryResourceAccess.InMemoryStorage();

		for (Entry<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> entry : REGISTRIES.entrySet()) {
			addBuiltinElements(inMemoryStorage, (RegistryAccess.RegistryData)entry.getValue());
		}

		RegistryOps.createAndLoad(JsonOps.INSTANCE, writable, inMemoryStorage);
		return writable;
	}

	private static <E> void addBuiltinElements(RegistryResourceAccess.InMemoryStorage inMemoryStorage, RegistryAccess.RegistryData<E> registryData) {
		ResourceKey<? extends Registry<E>> resourceKey = registryData.key();
		Registry<E> registry = BuiltinRegistries.ACCESS.registryOrThrow(resourceKey);

		for (Entry<ResourceKey<E>, E> entry : registry.entrySet()) {
			ResourceKey<E> resourceKey2 = (ResourceKey<E>)entry.getKey();
			E object = (E)entry.getValue();
			inMemoryStorage.add(BuiltinRegistries.ACCESS, resourceKey2, registryData.codec(), registry.getId(object), object, registry.lifecycle(object));
		}
	}

	static void load(RegistryAccess.Writable writable, DynamicOps<JsonElement> dynamicOps, RegistryLoader registryLoader) {
		RegistryLoader.Bound bound = registryLoader.bind(writable);

		for (RegistryAccess.RegistryData<?> registryData : REGISTRIES.values()) {
			readRegistry(dynamicOps, bound, registryData);
		}
	}

	private static <E> void readRegistry(DynamicOps<JsonElement> dynamicOps, RegistryLoader.Bound bound, RegistryAccess.RegistryData<E> registryData) {
		DataResult<? extends Registry<E>> dataResult = bound.overrideRegistryFromResources(registryData.key(), registryData.codec(), dynamicOps);
		dataResult.error().ifPresent(partialResult -> {
			throw new JsonParseException("Error loading registry data: " + partialResult.message());
		});
	}

	static RegistryAccess readFromDisk(Dynamic<?> dynamic) {
		return new RegistryAccess.ImmutableRegistryAccess(
			(Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>>)REGISTRIES.keySet()
				.stream()
				.collect(Collectors.toMap(Function.identity(), resourceKey -> retrieveRegistry(resourceKey, dynamic)))
		);
	}

	static <E> Registry<E> retrieveRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey, Dynamic<?> dynamic) {
		return (Registry<E>)RegistryOps.retrieveRegistry(resourceKey)
			.codec()
			.parse(dynamic)
			.resultOrPartial(Util.prefix(resourceKey + " registry: ", LOGGER::error))
			.orElseThrow(() -> new IllegalStateException("Failed to get " + resourceKey + " registry"));
	}

	static <E> WritableRegistry<?> createRegistry(ResourceKey<? extends Registry<?>> resourceKey) {
		return new MappedRegistry<>(resourceKey, Lifecycle.stable(), null);
	}

	default RegistryAccess.Frozen freeze() {
		return new RegistryAccess.ImmutableRegistryAccess(this.ownedRegistries().map(RegistryAccess.RegistryEntry::freeze));
	}

	default Lifecycle allElementsLifecycle() {
		return (Lifecycle)this.ownedRegistries().map(registryEntry -> registryEntry.value.elementsLifecycle()).reduce(Lifecycle.stable(), Lifecycle::add);
	}

	public interface Frozen extends RegistryAccess {
		@Override
		default RegistryAccess.Frozen freeze() {
			return this;
		}
	}

	public static final class ImmutableRegistryAccess implements RegistryAccess.Frozen {
		private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> registries;

		public ImmutableRegistryAccess(Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> map) {
			this.registries = Map.copyOf(map);
		}

		ImmutableRegistryAccess(Stream<RegistryAccess.RegistryEntry<?>> stream) {
			this.registries = (Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>>)stream.collect(
				ImmutableMap.toImmutableMap(RegistryAccess.RegistryEntry::key, RegistryAccess.RegistryEntry::value)
			);
		}

		@Override
		public <E> Optional<Registry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
			return Optional.ofNullable((Registry)this.registries.get(resourceKey)).map(registry -> registry);
		}

		@Override
		public Stream<RegistryAccess.RegistryEntry<?>> ownedRegistries() {
			return this.registries.entrySet().stream().map(RegistryAccess.RegistryEntry::fromMapEntry);
		}
	}

	public static record RegistryData<E>(ResourceKey<? extends Registry<E>> key, Codec<E> codec, @Nullable Codec<E> networkCodec) {
		public boolean sendToClient() {
			return this.networkCodec != null;
		}
	}

	public static record RegistryEntry<T>(ResourceKey<? extends Registry<T>> key, Registry<T> value) {

		private static <T, R extends Registry<? extends T>> RegistryAccess.RegistryEntry<T> fromMapEntry(Entry<? extends ResourceKey<? extends Registry<?>>, R> entry) {
			return fromUntyped((ResourceKey<? extends Registry<?>>)entry.getKey(), (Registry<?>)entry.getValue());
		}

		private static <T> RegistryAccess.RegistryEntry<T> fromHolder(Holder.Reference<? extends Registry<? extends T>> reference) {
			return fromUntyped(reference.key(), (Registry<?>)reference.value());
		}

		private static <T> RegistryAccess.RegistryEntry<T> fromUntyped(ResourceKey<? extends Registry<?>> resourceKey, Registry<?> registry) {
			return new RegistryAccess.RegistryEntry<>((ResourceKey<? extends Registry<T>>)resourceKey, (Registry<T>)registry);
		}

		private RegistryAccess.RegistryEntry<T> freeze() {
			return new RegistryAccess.RegistryEntry<>(this.key, this.value.freeze());
		}
	}

	public interface Writable extends RegistryAccess {
		<E> Optional<WritableRegistry<E>> ownedWritableRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey);

		default <E> WritableRegistry<E> ownedWritableRegistryOrThrow(ResourceKey<? extends Registry<? extends E>> resourceKey) {
			return (WritableRegistry<E>)this.ownedWritableRegistry(resourceKey).orElseThrow(() -> new IllegalStateException("Missing registry: " + resourceKey));
		}
	}

	public static final class WritableRegistryAccess implements RegistryAccess.Writable {
		private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends WritableRegistry<?>> registries;

		WritableRegistryAccess(Map<? extends ResourceKey<? extends Registry<?>>, ? extends WritableRegistry<?>> map) {
			this.registries = map;
		}

		@Override
		public <E> Optional<Registry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
			return Optional.ofNullable((WritableRegistry)this.registries.get(resourceKey)).map(writableRegistry -> writableRegistry);
		}

		@Override
		public <E> Optional<WritableRegistry<E>> ownedWritableRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
			return Optional.ofNullable((WritableRegistry)this.registries.get(resourceKey)).map(writableRegistry -> writableRegistry);
		}

		@Override
		public Stream<RegistryAccess.RegistryEntry<?>> ownedRegistries() {
			return this.registries.entrySet().stream().map(RegistryAccess.RegistryEntry::fromMapEntry);
		}
	}
}
