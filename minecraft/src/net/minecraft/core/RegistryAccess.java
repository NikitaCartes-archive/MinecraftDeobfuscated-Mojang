package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface RegistryAccess {
	Logger LOGGER = LogManager.getLogger();
	Map<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> REGISTRIES = Util.make(() -> {
		Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> builder = ImmutableMap.builder();
		put(builder, Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC, true);
		put(builder, Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC, true);
		put(builder, Registry.CONFIGURED_SURFACE_BUILDER_REGISTRY, ConfiguredSurfaceBuilder.DIRECT_CODEC, false);
		put(builder, Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC, false);
		put(builder, Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC, false);
		put(builder, Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ConfiguredStructureFeature.DIRECT_CODEC, false);
		put(builder, Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC, false);
		put(builder, Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC, false);
		return builder.build();
	});

	<E> Optional<WritableRegistry<E>> registry(ResourceKey<? extends Registry<E>> resourceKey);

	default <E> WritableRegistry<E> registryOrThrow(ResourceKey<? extends Registry<E>> resourceKey) {
		return (WritableRegistry<E>)this.registry(resourceKey).orElseThrow(() -> new IllegalStateException("Missing registry: " + resourceKey));
	}

	default Registry<DimensionType> dimensionTypes() {
		return this.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
	}

	static <E> Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> put(
		Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> builder,
		ResourceKey<? extends Registry<E>> resourceKey,
		MapCodec<E> mapCodec,
		boolean bl
	) {
		return builder.put(resourceKey, new RegistryAccess.RegistryData<>(resourceKey, mapCodec, bl));
	}

	static RegistryAccess.RegistryHolder builtin() {
		RegistryAccess.RegistryHolder registryHolder = new RegistryAccess.RegistryHolder();
		DimensionType.registerBuiltin(registryHolder);
		REGISTRIES.keySet()
			.stream()
			.filter(resourceKey -> !resourceKey.equals(Registry.DIMENSION_TYPE_REGISTRY))
			.forEach(resourceKey -> copyBuiltin(registryHolder, resourceKey));
		return registryHolder;
	}

	static <R extends Registry<?>> void copyBuiltin(RegistryAccess.RegistryHolder registryHolder, ResourceKey<R> resourceKey) {
		Registry<R> registry = (Registry<R>)BuiltinRegistries.REGISTRY;
		Registry<?> registry2 = registry.get(resourceKey);
		if (registry2 == null) {
			throw new IllegalStateException("Missing builtin registry: " + resourceKey);
		} else {
			copy(registryHolder, registry2);
		}
	}

	static <E> void copy(RegistryAccess.RegistryHolder registryHolder, Registry<E> registry) {
		WritableRegistry<E> writableRegistry = (WritableRegistry<E>)registryHolder.registry(registry.key())
			.orElseThrow(() -> new IllegalStateException("Missing registry: " + registry.key()));

		for (Entry<ResourceKey<E>, E> entry : registry.entrySet()) {
			writableRegistry.register((ResourceKey<E>)entry.getKey(), entry.getValue());
		}
	}

	@Environment(EnvType.CLIENT)
	static RegistryAccess.RegistryHolder load(ResourceManager resourceManager) {
		RegistryAccess.RegistryHolder registryHolder = builtin();
		RegistryReadOps<JsonElement> registryReadOps = RegistryReadOps.create(JsonOps.INSTANCE, resourceManager, registryHolder);

		for (RegistryAccess.RegistryData<?> registryData : REGISTRIES.values()) {
			readRegistry(registryReadOps, registryHolder, registryData);
		}

		return registryHolder;
	}

	@Environment(EnvType.CLIENT)
	static <E> void readRegistry(
		RegistryReadOps<JsonElement> registryReadOps, RegistryAccess.RegistryHolder registryHolder, RegistryAccess.RegistryData<E> registryData
	) {
		ResourceKey<? extends Registry<E>> resourceKey = registryData.key();
		MappedRegistry<E> mappedRegistry = (MappedRegistry<E>)Optional.ofNullable(registryHolder.registries.get(resourceKey))
			.map(mappedRegistryx -> mappedRegistryx)
			.orElseThrow(() -> new IllegalStateException("Missing registry: " + resourceKey));
		DataResult<MappedRegistry<E>> dataResult = registryReadOps.decodeElements(mappedRegistry, registryData.key(), registryData.codec());
		dataResult.error().ifPresent(partialResult -> LOGGER.error("Error loading registry data: {}", partialResult.message()));
	}

	public static final class RegistryData<E> {
		private final ResourceKey<? extends Registry<E>> key;
		private final MapCodec<E> codec;
		private final boolean sendToClient;

		public RegistryData(ResourceKey<? extends Registry<E>> resourceKey, MapCodec<E> mapCodec, boolean bl) {
			this.key = resourceKey;
			this.codec = mapCodec;
			this.sendToClient = bl;
		}

		@Environment(EnvType.CLIENT)
		public ResourceKey<? extends Registry<E>> key() {
			return this.key;
		}

		public MapCodec<E> codec() {
			return this.codec;
		}

		public boolean sendToClient() {
			return this.sendToClient;
		}
	}

	public static final class RegistryHolder implements RegistryAccess {
		public static final Codec<RegistryAccess.RegistryHolder> NETWORK_CODEC = makeDirectCodec();
		private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> registries;

		private static <E> Codec<RegistryAccess.RegistryHolder> makeDirectCodec() {
			Codec<ResourceKey<? extends Registry<E>>> codec = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
			Codec<MappedRegistry<E>> codec2 = codec.partialDispatch(
				"type",
				mappedRegistry -> DataResult.success(mappedRegistry.key()),
				resourceKey -> getCodec(resourceKey).map(mapCodec -> MappedRegistry.networkCodec(resourceKey, Lifecycle.experimental(), mapCodec))
			);
			UnboundedMapCodec<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> unboundedMapCodec = Codec.unboundedMap(codec, codec2);
			return captureMap(unboundedMapCodec);
		}

		private static <K extends ResourceKey<? extends Registry<?>>, V extends MappedRegistry<?>> Codec<RegistryAccess.RegistryHolder> captureMap(
			UnboundedMapCodec<K, V> unboundedMapCodec
		) {
			return unboundedMapCodec.xmap(
				RegistryAccess.RegistryHolder::new,
				registryHolder -> (ImmutableMap)registryHolder.registries
						.entrySet()
						.stream()
						.filter(entry -> ((RegistryAccess.RegistryData)REGISTRIES.get(entry.getKey())).sendToClient())
						.collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue))
			);
		}

		private static <E> DataResult<? extends MapCodec<E>> getCodec(ResourceKey<? extends Registry<E>> resourceKey) {
			return (DataResult<? extends MapCodec<E>>)Optional.ofNullable(REGISTRIES.get(resourceKey))
				.map(registryData -> DataResult.success(registryData.codec()))
				.orElseGet(() -> DataResult.error("Unknown registry: " + resourceKey));
		}

		public RegistryHolder() {
			this(
				(Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>>)REGISTRIES.keySet()
					.stream()
					.collect(Collectors.toMap(Function.identity(), RegistryAccess.RegistryHolder::createRegistry))
			);
		}

		private RegistryHolder(Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> map) {
			this.registries = map;
		}

		private static <E> MappedRegistry<?> createRegistry(ResourceKey<? extends Registry<?>> resourceKey) {
			return new MappedRegistry<>(resourceKey, Lifecycle.experimental());
		}

		@Override
		public <E> Optional<WritableRegistry<E>> registry(ResourceKey<? extends Registry<E>> resourceKey) {
			return Optional.ofNullable(this.registries.get(resourceKey)).map(mappedRegistry -> mappedRegistry);
		}
	}
}
