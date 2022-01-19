package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Multimap;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.resources.ResourceKey;
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
	private final StructureManager structureManager;
	private final ResourceKey<Level> dimension;
	private final ChunkGenerator chunkGenerator;
	private final LevelHeightAccessor heightAccessor;
	private final BiomeSource biomeSource;
	private final long seed;
	private final DataFixer fixerUpper;
	private final Long2ObjectMap<Object2IntMap<StructureFeature<?>>> loadedChunks = new Long2ObjectOpenHashMap<>();
	private final Map<StructureFeature<?>, Long2BooleanMap> featureChecks = new HashMap();

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
	}

	public <F extends StructureFeature<?>> StructureCheckResult checkStart(ChunkPos chunkPos, F structureFeature, boolean bl) {
		long l = chunkPos.toLong();
		Object2IntMap<StructureFeature<?>> object2IntMap = this.loadedChunks.get(l);
		if (object2IntMap != null) {
			return this.checkStructureInfo(object2IntMap, structureFeature, bl);
		} else {
			StructureCheckResult structureCheckResult = this.tryLoadFromStorage(chunkPos, structureFeature, bl, l);
			if (structureCheckResult != null) {
				return structureCheckResult;
			} else {
				boolean bl2 = ((Long2BooleanMap)this.featureChecks.computeIfAbsent(structureFeature, structureFeaturex -> new Long2BooleanOpenHashMap()))
					.computeIfAbsent(l, (Long2BooleanFunction)(lx -> {
						Multimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> multimap = this.chunkGenerator.getSettings().structures(structureFeature);

						for (Entry<ConfiguredStructureFeature<?, ?>, Collection<ResourceKey<Biome>>> entry : multimap.asMap().entrySet()) {
							if (this.canCreateStructure(chunkPos, (ConfiguredStructureFeature)entry.getKey(), (Collection<ResourceKey<Biome>>)entry.getValue())) {
								return true;
							}
						}

						return false;
					}));
				return !bl2 ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.CHUNK_LOAD_NEEDED;
			}
		}
	}

	private <FC extends FeatureConfiguration, F extends StructureFeature<FC>> boolean canCreateStructure(
		ChunkPos chunkPos, ConfiguredStructureFeature<FC, F> configuredStructureFeature, Collection<ResourceKey<Biome>> collection
	) {
		Predicate<Biome> predicate = biome -> this.biomes.getResourceKey(biome).filter(collection::contains).isPresent();
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
				predicate
			);
	}

	@Nullable
	private StructureCheckResult tryLoadFromStorage(ChunkPos chunkPos, StructureFeature<?> structureFeature, boolean bl, long l) {
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

				Object2IntMap<StructureFeature<?>> object2IntMap = this.loadStructures(compoundTag2);
				if (object2IntMap == null) {
					return null;
				} else {
					this.storeFullResults(l, object2IntMap);
					return this.checkStructureInfo(object2IntMap, structureFeature, bl);
				}
			}
		}
	}

	@Nullable
	private Object2IntMap<StructureFeature<?>> loadStructures(CompoundTag compoundTag) {
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
					Object2IntMap<StructureFeature<?>> object2IntMap = new Object2IntOpenHashMap<>();

					for (String string : compoundTag3.getAllKeys()) {
						String string2 = string.toLowerCase(Locale.ROOT);
						StructureFeature<?> structureFeature = (StructureFeature<?>)StructureFeature.STRUCTURES_REGISTRY.get(string2);
						if (structureFeature != null) {
							CompoundTag compoundTag4 = compoundTag3.getCompound(string);
							if (!compoundTag4.isEmpty()) {
								String string3 = compoundTag4.getString("id");
								if (!"INVALID".equals(string3)) {
									int i = compoundTag4.getInt("references");
									object2IntMap.put(structureFeature, i);
								}
							}
						}
					}

					return object2IntMap;
				}
			}
		}
	}

	private static Object2IntMap<StructureFeature<?>> deduplicateEmptyMap(Object2IntMap<StructureFeature<?>> object2IntMap) {
		return object2IntMap.isEmpty() ? Object2IntMaps.emptyMap() : object2IntMap;
	}

	private StructureCheckResult checkStructureInfo(Object2IntMap<StructureFeature<?>> object2IntMap, StructureFeature<?> structureFeature, boolean bl) {
		int i = object2IntMap.getOrDefault(structureFeature, -1);
		return i == -1 || bl && i != 0 ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.START_PRESENT;
	}

	public void onStructureLoad(ChunkPos chunkPos, Map<StructureFeature<?>, StructureStart<?>> map) {
		long l = chunkPos.toLong();
		Object2IntMap<StructureFeature<?>> object2IntMap = new Object2IntOpenHashMap<>();
		map.forEach((structureFeature, structureStart) -> {
			if (structureStart.isValid()) {
				object2IntMap.put(structureFeature, structureStart.getReferences());
			}
		});
		this.storeFullResults(l, object2IntMap);
	}

	private void storeFullResults(long l, Object2IntMap<StructureFeature<?>> object2IntMap) {
		this.loadedChunks.put(l, deduplicateEmptyMap(object2IntMap));
		this.featureChecks.values().forEach(long2BooleanMap -> long2BooleanMap.remove(l));
	}

	public void incrementReference(ChunkPos chunkPos, StructureFeature<?> structureFeature) {
		this.loadedChunks.compute(chunkPos.toLong(), (long_, object2IntMap) -> {
			if (object2IntMap == null || object2IntMap.isEmpty()) {
				object2IntMap = new Object2IntOpenHashMap();
			}

			object2IntMap.computeInt(structureFeature, (structureFeaturexx, integer) -> integer == null ? 1 : integer + 1);
			return object2IntMap;
		});
	}
}
