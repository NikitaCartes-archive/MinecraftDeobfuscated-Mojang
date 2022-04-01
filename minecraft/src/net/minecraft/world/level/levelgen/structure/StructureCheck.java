package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2BooleanFunction;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.slf4j.Logger;

public class StructureCheck {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int NO_STRUCTURE = -1;
	private final ChunkScanAccess storageAccess;
	private final RegistryAccess registryAccess;
	private final Registry<Biome> biomes;
	private final Registry<ConfiguredStructureFeature<?, ?>> structureConfigs;
	private final StructureManager structureManager;
	private final ResourceKey<Level> dimension;
	private final ChunkGenerator chunkGenerator;
	private final LevelHeightAccessor heightAccessor;
	private final BiomeSource biomeSource;
	private final long seed;
	private final DataFixer fixerUpper;
	private final Long2ObjectMap<Object2IntMap<ConfiguredStructureFeature<?, ?>>> loadedChunks = new Long2ObjectOpenHashMap<>();
	private final Map<ConfiguredStructureFeature<?, ?>, Long2BooleanMap> featureChecks = new HashMap();

	public StructureCheck(
		ChunkScanAccess chunkScanAccess,
		RegistryAccess registryAccess,
		StructureManager structureManager,
		ResourceKey<Level> resourceKey,
		ChunkGenerator chunkGenerator,
		LevelHeightAccessor levelHeightAccessor,
		BiomeSource biomeSource,
		long l,
		DataFixer dataFixer
	) {
		this.storageAccess = chunkScanAccess;
		this.registryAccess = registryAccess;
		this.structureManager = structureManager;
		this.dimension = resourceKey;
		this.chunkGenerator = chunkGenerator;
		this.heightAccessor = levelHeightAccessor;
		this.biomeSource = biomeSource;
		this.seed = l;
		this.fixerUpper = dataFixer;
		this.biomes = registryAccess.ownedRegistryOrThrow(Registry.BIOME_REGISTRY);
		this.structureConfigs = registryAccess.ownedRegistryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
	}

	public StructureCheckResult checkStart(ChunkPos chunkPos, ConfiguredStructureFeature<?, ?> configuredStructureFeature, boolean bl) {
		long l = chunkPos.toLong();
		Object2IntMap<ConfiguredStructureFeature<?, ?>> object2IntMap = this.loadedChunks.get(l);
		if (object2IntMap != null) {
			return this.checkStructureInfo(object2IntMap, configuredStructureFeature, bl);
		} else {
			StructureCheckResult structureCheckResult = this.tryLoadFromStorage(chunkPos, configuredStructureFeature, bl, l);
			if (structureCheckResult != null) {
				return structureCheckResult;
			} else {
				boolean bl2 = ((Long2BooleanMap)this.featureChecks
						.computeIfAbsent(configuredStructureFeature, configuredStructureFeaturex -> new Long2BooleanOpenHashMap()))
					.computeIfAbsent(l, (Long2BooleanFunction)(lx -> this.canCreateStructure(chunkPos, configuredStructureFeature)));
				return !bl2 ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.CHUNK_LOAD_NEEDED;
			}
		}
	}

	private <FC extends FeatureConfiguration, F extends StructureFeature<FC>> boolean canCreateStructure(
		ChunkPos chunkPos, ConfiguredStructureFeature<FC, F> configuredStructureFeature
	) {
		return configuredStructureFeature.feature
			.canGenerate(
				this.registryAccess,
				this.chunkGenerator,
				this.biomeSource,
				this.structureManager,
				this.seed,
				chunkPos,
				configuredStructureFeature.config,
				this.heightAccessor,
				configuredStructureFeature.biomes()::contains
			);
	}

	@Nullable
	private StructureCheckResult tryLoadFromStorage(ChunkPos chunkPos, ConfiguredStructureFeature<?, ?> configuredStructureFeature, boolean bl, long l) {
		CollectFields collectFields = new CollectFields(
			new FieldSelector(IntTag.TYPE, "DataVersion"),
			new FieldSelector("Level", "Structures", CompoundTag.TYPE, "Starts"),
			new FieldSelector("structures", CompoundTag.TYPE, "starts")
		);

		try {
			this.storageAccess.scanChunk(chunkPos, collectFields).join();
		} catch (Exception var13) {
			LOGGER.warn("Failed to read chunk {}", chunkPos, var13);
			return StructureCheckResult.CHUNK_LOAD_NEEDED;
		}

		if (!(collectFields.getResult() instanceof CompoundTag compoundTag)) {
			return null;
		} else {
			int i = ChunkStorage.getVersion(compoundTag);
			if (i <= 1493) {
				return StructureCheckResult.CHUNK_LOAD_NEEDED;
			} else {
				ChunkStorage.injectDatafixingContext(compoundTag, this.dimension, this.chunkGenerator.getTypeNameForDataFixer());

				CompoundTag compoundTag2;
				try {
					compoundTag2 = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, compoundTag, i);
				} catch (Exception var12) {
					LOGGER.warn("Failed to partially datafix chunk {}", chunkPos, var12);
					return StructureCheckResult.CHUNK_LOAD_NEEDED;
				}

				Object2IntMap<ConfiguredStructureFeature<?, ?>> object2IntMap = this.loadStructures(compoundTag2);
				if (object2IntMap == null) {
					return null;
				} else {
					this.storeFullResults(l, object2IntMap);
					return this.checkStructureInfo(object2IntMap, configuredStructureFeature, bl);
				}
			}
		}
	}

	@Nullable
	private Object2IntMap<ConfiguredStructureFeature<?, ?>> loadStructures(CompoundTag compoundTag) {
		if (!compoundTag.contains("structures", 10)) {
			return null;
		} else {
			CompoundTag compoundTag2 = compoundTag.getCompound("structures");
			if (!compoundTag2.contains("starts", 10)) {
				return null;
			} else {
				CompoundTag compoundTag3 = compoundTag2.getCompound("starts");
				if (compoundTag3.isEmpty()) {
					return Object2IntMaps.emptyMap();
				} else {
					Object2IntMap<ConfiguredStructureFeature<?, ?>> object2IntMap = new Object2IntOpenHashMap<>();
					Registry<ConfiguredStructureFeature<?, ?>> registry = this.registryAccess.registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);

					for (String string : compoundTag3.getAllKeys()) {
						ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
						if (resourceLocation != null) {
							ConfiguredStructureFeature<?, ?> configuredStructureFeature = registry.get(resourceLocation);
							if (configuredStructureFeature != null) {
								CompoundTag compoundTag4 = compoundTag3.getCompound(string);
								if (!compoundTag4.isEmpty()) {
									String string2 = compoundTag4.getString("id");
									if (!"INVALID".equals(string2)) {
										int i = compoundTag4.getInt("references");
										object2IntMap.put(configuredStructureFeature, i);
									}
								}
							}
						}
					}

					return object2IntMap;
				}
			}
		}
	}

	private static Object2IntMap<ConfiguredStructureFeature<?, ?>> deduplicateEmptyMap(Object2IntMap<ConfiguredStructureFeature<?, ?>> object2IntMap) {
		return object2IntMap.isEmpty() ? Object2IntMaps.emptyMap() : object2IntMap;
	}

	private StructureCheckResult checkStructureInfo(
		Object2IntMap<ConfiguredStructureFeature<?, ?>> object2IntMap, ConfiguredStructureFeature<?, ?> configuredStructureFeature, boolean bl
	) {
		int i = object2IntMap.getOrDefault(configuredStructureFeature, -1);
		return i == -1 || bl && i != 0 ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.START_PRESENT;
	}

	public void onStructureLoad(ChunkPos chunkPos, Map<ConfiguredStructureFeature<?, ?>, StructureStart> map) {
		long l = chunkPos.toLong();
		Object2IntMap<ConfiguredStructureFeature<?, ?>> object2IntMap = new Object2IntOpenHashMap<>();
		map.forEach((configuredStructureFeature, structureStart) -> {
			if (structureStart.isValid()) {
				object2IntMap.put(configuredStructureFeature, structureStart.getReferences());
			}
		});
		this.storeFullResults(l, object2IntMap);
	}

	private void storeFullResults(long l, Object2IntMap<ConfiguredStructureFeature<?, ?>> object2IntMap) {
		this.loadedChunks.put(l, deduplicateEmptyMap(object2IntMap));
		this.featureChecks.values().forEach(long2BooleanMap -> long2BooleanMap.remove(l));
	}

	public void incrementReference(ChunkPos chunkPos, ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
		this.loadedChunks.compute(chunkPos.toLong(), (long_, object2IntMap) -> {
			if (object2IntMap == null || object2IntMap.isEmpty()) {
				object2IntMap = new Object2IntOpenHashMap();
			}

			object2IntMap.computeInt(configuredStructureFeature, (configuredStructureFeaturexx, integer) -> integer == null ? 1 : integer + 1);
			return object2IntMap;
		});
	}
}
