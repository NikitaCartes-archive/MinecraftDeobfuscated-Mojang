package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class RegistryAccess {
	static final Logger LOGGER = LogManager.getLogger();
	static final Map<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> REGISTRIES = Util.make(() -> {
		Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> builder = ImmutableMap.builder();
		put(builder, Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC, DimensionType.DIRECT_CODEC);
		put(builder, Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC, Biome.NETWORK_CODEC);
		put(builder, Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC);
		put(builder, Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC);
		put(builder, Registry.PLACED_FEATURE_REGISTRY, PlacedFeature.DIRECT_CODEC);
		put(builder, Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ConfiguredStructureFeature.DIRECT_CODEC);
		put(builder, Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC);
		put(builder, Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC);
		put(builder, Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings.DIRECT_CODEC);
		put(builder, Registry.NOISE_REGISTRY, NormalNoise.NoiseParameters.DIRECT_CODEC);
		return builder.build();
	});
	private static final RegistryAccess.RegistryHolder BUILTIN = Util.make(
		() -> {
			RegistryAccess.RegistryHolder registryHolder = new RegistryAccess.RegistryHolder();
			DimensionType.registerBuiltin(registryHolder);
			REGISTRIES.keySet()
				.stream()
				.filter(resourceKey -> !resourceKey.equals(Registry.DIMENSION_TYPE_REGISTRY))
				.forEach(resourceKey -> copyBuiltin(registryHolder, resourceKey));
			return registryHolder;
		}
	);

	public abstract <E> Optional<WritableRegistry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey);

	public <E> WritableRegistry<E> ownedRegistryOrThrow(ResourceKey<? extends Registry<? extends E>> resourceKey) {
		return (WritableRegistry<E>)this.ownedRegistry(resourceKey).orElseThrow(() -> new IllegalStateException("Missing registry: " + resourceKey));
	}

	public <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
		Optional<? extends Registry<E>> optional = this.ownedRegistry(resourceKey);
		return optional.isPresent() ? optional : Registry.REGISTRY.getOptional(resourceKey.location());
	}

	public <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> resourceKey) {
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

	public static Iterable<RegistryAccess.RegistryData<?>> knownRegistries() {
		return REGISTRIES.values();
	}

	public static RegistryAccess.RegistryHolder builtin() {
		RegistryAccess.RegistryHolder registryHolder = new RegistryAccess.RegistryHolder();
		RegistryResourceAccess.InMemoryStorage inMemoryStorage = new RegistryResourceAccess.InMemoryStorage();

		for (RegistryAccess.RegistryData<?> registryData : REGISTRIES.values()) {
			addBuiltinElements(registryHolder, inMemoryStorage, registryData);
		}

		RegistryReadOps.createAndLoad(JsonOps.INSTANCE, inMemoryStorage, registryHolder);
		return registryHolder;
	}

	private static <E> void addBuiltinElements(
		RegistryAccess.RegistryHolder registryHolder, RegistryResourceAccess.InMemoryStorage inMemoryStorage, RegistryAccess.RegistryData<E> registryData
	) {
		ResourceKey<? extends Registry<E>> resourceKey = registryData.key();
		boolean bl = !resourceKey.equals(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY) && !resourceKey.equals(Registry.DIMENSION_TYPE_REGISTRY);
		Registry<E> registry = BUILTIN.registryOrThrow(resourceKey);
		WritableRegistry<E> writableRegistry = registryHolder.ownedRegistryOrThrow(resourceKey);

		for (Entry<ResourceKey<E>, E> entry : registry.entrySet()) {
			ResourceKey<E> resourceKey2 = (ResourceKey<E>)entry.getKey();
			E object = (E)entry.getValue();
			if (bl) {
				inMemoryStorage.add(BUILTIN, resourceKey2, registryData.codec(), registry.getId(object), object, registry.lifecycle(object));
			} else {
				writableRegistry.registerMapping(registry.getId(object), resourceKey2, object, registry.lifecycle(object));
			}
		}
	}

	private static <R extends Registry<?>> void copyBuiltin(RegistryAccess.RegistryHolder registryHolder, ResourceKey<R> resourceKey) {
		Registry<R> registry = (Registry<R>)BuiltinRegistries.REGISTRY;
		Registry<?> registry2 = registry.getOrThrow(resourceKey);
		copy(registryHolder, registry2);
	}

	private static <E> void copy(RegistryAccess.RegistryHolder registryHolder, Registry<E> registry) {
		WritableRegistry<E> writableRegistry = registryHolder.ownedRegistryOrThrow(registry.key());

		for (Entry<ResourceKey<E>, E> entry : registry.entrySet()) {
			E object = (E)entry.getValue();
			writableRegistry.registerMapping(registry.getId(object), (ResourceKey<E>)entry.getKey(), object, registry.lifecycle(object));
		}
	}

	public static void load(RegistryAccess registryAccess, RegistryReadOps<?> registryReadOps) {
		for (RegistryAccess.RegistryData<?> registryData : REGISTRIES.values()) {
			readRegistry(registryReadOps, registryAccess, registryData);
		}
	}

	private static <E> void readRegistry(RegistryReadOps<?> registryReadOps, RegistryAccess registryAccess, RegistryAccess.RegistryData<E> registryData) {
		ResourceKey<? extends Registry<E>> resourceKey = registryData.key();
		MappedRegistry<E> mappedRegistry = (MappedRegistry<E>)registryAccess.<E>ownedRegistryOrThrow(resourceKey);
		DataResult<MappedRegistry<E>> dataResult = registryReadOps.decodeElements(mappedRegistry, registryData.key(), registryData.codec());
		dataResult.error().ifPresent(partialResult -> {
			throw new JsonParseException("Error loading registry data: " + partialResult.message());
		});
	}

	public static record RegistryData<E>(ResourceKey<? extends Registry<E>> key, Codec<E> codec, @Nullable Codec<E> networkCodec) {
		public boolean sendToClient() {
			return this.networkCodec != null;
		}
	}

	public static final class RegistryHolder extends RegistryAccess {
		public static final Codec<RegistryAccess.RegistryHolder> NETWORK_CODEC = makeNetworkCodec();
		private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> registries;

		private static <E> Codec<RegistryAccess.RegistryHolder> makeNetworkCodec() {
			Codec<ResourceKey<? extends Registry<E>>> codec = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
			Codec<MappedRegistry<E>> codec2 = codec.partialDispatch(
				"type",
				mappedRegistry -> DataResult.success(mappedRegistry.key()),
				resourceKey -> getNetworkCodec(resourceKey).map(codecx -> MappedRegistry.networkCodec(resourceKey, Lifecycle.experimental(), codecx))
			);
			UnboundedMapCodec<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> unboundedMapCodec = Codec.unboundedMap(codec, codec2);
			return captureMap(unboundedMapCodec);
		}

		private static <K extends ResourceKey<? extends Registry<?>>, V extends MappedRegistry<?>> Codec<RegistryAccess.RegistryHolder> captureMap(
			UnboundedMapCodec<K, V> unboundedMapCodec
		) {
			return unboundedMapCodec.xmap(
				RegistryAccess.RegistryHolder::new,
				registryHolder -> (Map)registryHolder.registries
						.entrySet()
						.stream()
						.filter(entry -> ((RegistryAccess.RegistryData)RegistryAccess.REGISTRIES.get(entry.getKey())).sendToClient())
						.collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue))
			);
		}

		private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> resourceKey) {
			return (DataResult<? extends Codec<E>>)Optional.ofNullable((RegistryAccess.RegistryData)RegistryAccess.REGISTRIES.get(resourceKey))
				.map(registryData -> registryData.networkCodec())
				.map(DataResult::success)
				.orElseGet(() -> DataResult.error("Unknown or not serializable registry: " + resourceKey));
		}

		public RegistryHolder() {
			this(
				(Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>>)RegistryAccess.REGISTRIES
					.keySet()
					.stream()
					.collect(Collectors.toMap(Function.identity(), RegistryAccess.RegistryHolder::createRegistry))
			);
		}

		public static RegistryAccess readFromDisk(Dynamic<?> dynamic) {
			return new RegistryAccess.RegistryHolder(
				(Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>>)RegistryAccess.REGISTRIES
					.keySet()
					.stream()
					.collect(Collectors.toMap(Function.identity(), resourceKey -> parseRegistry(resourceKey, dynamic)))
			);
		}

		private static <E> MappedRegistry<?> parseRegistry(ResourceKey<? extends Registry<?>> resourceKey, Dynamic<?> dynamic) {
			return (MappedRegistry<?>)RegistryLookupCodec.create(resourceKey)
				.codec()
				.parse(dynamic)
				.resultOrPartial(Util.prefix(resourceKey + " registry: ", RegistryAccess.LOGGER::error))
				.orElseThrow(() -> new IllegalStateException("Failed to get " + resourceKey + " registry"));
		}

		private RegistryHolder(Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> map) {
			this.registries = map;
		}

		private static <E> MappedRegistry<?> createRegistry(ResourceKey<? extends Registry<?>> resourceKey) {
			return new MappedRegistry<>(resourceKey, Lifecycle.stable());
		}

		@Override
		public <E> Optional<WritableRegistry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> resourceKey) {
			return Optional.ofNullable((MappedRegistry)this.registries.get(resourceKey)).map(mappedRegistry -> mappedRegistry);
		}
	}
}
