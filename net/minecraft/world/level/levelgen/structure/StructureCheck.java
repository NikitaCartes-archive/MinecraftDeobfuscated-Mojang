/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class StructureCheck {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int NO_STRUCTURE = -1;
    private final ChunkScanAccess storageAccess;
    private final RegistryAccess registryAccess;
    private final Registry<Biome> biomes;
    private final Registry<Structure> structureConfigs;
    private final StructureTemplateManager structureTemplateManager;
    private final ResourceKey<Level> dimension;
    private final ChunkGenerator chunkGenerator;
    private final RandomState randomState;
    private final LevelHeightAccessor heightAccessor;
    private final BiomeSource biomeSource;
    private final long seed;
    private final DataFixer fixerUpper;
    private final Long2ObjectMap<Object2IntMap<Structure>> loadedChunks = new Long2ObjectOpenHashMap<Object2IntMap<Structure>>();
    private final Map<Structure, Long2BooleanMap> featureChecks = new HashMap<Structure, Long2BooleanMap>();

    public StructureCheck(ChunkScanAccess chunkScanAccess, RegistryAccess registryAccess, StructureTemplateManager structureTemplateManager, ResourceKey<Level> resourceKey, ChunkGenerator chunkGenerator, RandomState randomState, LevelHeightAccessor levelHeightAccessor, BiomeSource biomeSource, long l, DataFixer dataFixer) {
        this.storageAccess = chunkScanAccess;
        this.registryAccess = registryAccess;
        this.structureTemplateManager = structureTemplateManager;
        this.dimension = resourceKey;
        this.chunkGenerator = chunkGenerator;
        this.randomState = randomState;
        this.heightAccessor = levelHeightAccessor;
        this.biomeSource = biomeSource;
        this.seed = l;
        this.fixerUpper = dataFixer;
        this.biomes = registryAccess.ownedRegistryOrThrow(Registry.BIOME_REGISTRY);
        this.structureConfigs = registryAccess.ownedRegistryOrThrow(Registry.STRUCTURE_REGISTRY);
    }

    public StructureCheckResult checkStart(ChunkPos chunkPos, Structure structure2, boolean bl) {
        long l2 = chunkPos.toLong();
        Object2IntMap object2IntMap = (Object2IntMap)this.loadedChunks.get(l2);
        if (object2IntMap != null) {
            return this.checkStructureInfo(object2IntMap, structure2, bl);
        }
        StructureCheckResult structureCheckResult = this.tryLoadFromStorage(chunkPos, structure2, bl, l2);
        if (structureCheckResult != null) {
            return structureCheckResult;
        }
        boolean bl2 = this.featureChecks.computeIfAbsent(structure2, structure -> new Long2BooleanOpenHashMap()).computeIfAbsent(l2, l -> this.canCreateStructure(chunkPos, structure2));
        if (!bl2) {
            return StructureCheckResult.START_NOT_PRESENT;
        }
        return StructureCheckResult.CHUNK_LOAD_NEEDED;
    }

    private boolean canCreateStructure(ChunkPos chunkPos, Structure structure) {
        return structure.findGenerationPoint(new Structure.GenerationContext(this.registryAccess, this.chunkGenerator, this.biomeSource, this.randomState, this.structureTemplateManager, this.seed, chunkPos, this.heightAccessor, structure.biomes()::contains)).isPresent();
    }

    @Nullable
    private StructureCheckResult tryLoadFromStorage(ChunkPos chunkPos, Structure structure, boolean bl, long l) {
        CompoundTag compoundTag2;
        CollectFields collectFields = new CollectFields(new FieldSelector(IntTag.TYPE, "DataVersion"), new FieldSelector("Level", "Structures", CompoundTag.TYPE, "Starts"), new FieldSelector("structures", CompoundTag.TYPE, "starts"));
        try {
            this.storageAccess.scanChunk(chunkPos, collectFields).join();
        } catch (Exception exception) {
            LOGGER.warn("Failed to read chunk {}", (Object)chunkPos, (Object)exception);
            return StructureCheckResult.CHUNK_LOAD_NEEDED;
        }
        Tag tag = collectFields.getResult();
        if (!(tag instanceof CompoundTag)) {
            return null;
        }
        CompoundTag compoundTag = (CompoundTag)tag;
        int i = ChunkStorage.getVersion(compoundTag);
        if (i <= 1493) {
            return StructureCheckResult.CHUNK_LOAD_NEEDED;
        }
        ChunkStorage.injectDatafixingContext(compoundTag, this.dimension, this.chunkGenerator.getTypeNameForDataFixer());
        try {
            compoundTag2 = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, compoundTag, i);
        } catch (Exception exception2) {
            LOGGER.warn("Failed to partially datafix chunk {}", (Object)chunkPos, (Object)exception2);
            return StructureCheckResult.CHUNK_LOAD_NEEDED;
        }
        Object2IntMap<Structure> object2IntMap = this.loadStructures(compoundTag2);
        if (object2IntMap == null) {
            return null;
        }
        this.storeFullResults(l, object2IntMap);
        return this.checkStructureInfo(object2IntMap, structure, bl);
    }

    @Nullable
    private Object2IntMap<Structure> loadStructures(CompoundTag compoundTag) {
        if (!compoundTag.contains("structures", 10)) {
            return null;
        }
        CompoundTag compoundTag2 = compoundTag.getCompound("structures");
        if (!compoundTag2.contains("starts", 10)) {
            return null;
        }
        CompoundTag compoundTag3 = compoundTag2.getCompound("starts");
        if (compoundTag3.isEmpty()) {
            return Object2IntMaps.emptyMap();
        }
        Object2IntOpenHashMap<Structure> object2IntMap = new Object2IntOpenHashMap<Structure>();
        Registry<Structure> registry = this.registryAccess.registryOrThrow(Registry.STRUCTURE_REGISTRY);
        for (String string : compoundTag3.getAllKeys()) {
            String string2;
            CompoundTag compoundTag4;
            Structure structure;
            ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
            if (resourceLocation == null || (structure = registry.get(resourceLocation)) == null || (compoundTag4 = compoundTag3.getCompound(string)).isEmpty() || "INVALID".equals(string2 = compoundTag4.getString("id"))) continue;
            int i = compoundTag4.getInt("references");
            object2IntMap.put(structure, i);
        }
        return object2IntMap;
    }

    private static Object2IntMap<Structure> deduplicateEmptyMap(Object2IntMap<Structure> object2IntMap) {
        return object2IntMap.isEmpty() ? Object2IntMaps.emptyMap() : object2IntMap;
    }

    private StructureCheckResult checkStructureInfo(Object2IntMap<Structure> object2IntMap, Structure structure, boolean bl) {
        int i = object2IntMap.getOrDefault((Object)structure, -1);
        return i != -1 && (!bl || i == 0) ? StructureCheckResult.START_PRESENT : StructureCheckResult.START_NOT_PRESENT;
    }

    public void onStructureLoad(ChunkPos chunkPos, Map<Structure, StructureStart> map) {
        long l = chunkPos.toLong();
        Object2IntOpenHashMap<Structure> object2IntMap = new Object2IntOpenHashMap<Structure>();
        map.forEach((structure, structureStart) -> {
            if (structureStart.isValid()) {
                object2IntMap.put((Structure)structure, structureStart.getReferences());
            }
        });
        this.storeFullResults(l, object2IntMap);
    }

    private void storeFullResults(long l, Object2IntMap<Structure> object2IntMap) {
        this.loadedChunks.put(l, StructureCheck.deduplicateEmptyMap(object2IntMap));
        this.featureChecks.values().forEach(long2BooleanMap -> long2BooleanMap.remove(l));
    }

    public void incrementReference(ChunkPos chunkPos, Structure structure) {
        this.loadedChunks.compute(chunkPos.toLong(), (long_, object2IntMap) -> {
            if (object2IntMap == null || object2IntMap.isEmpty()) {
                object2IntMap = new Object2IntOpenHashMap<Structure>();
            }
            object2IntMap.computeInt(structure, (structure, integer) -> integer == null ? 1 : integer + 1);
            return object2IntMap;
        });
    }
}

