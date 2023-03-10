/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ChunkSerializer {
    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codecRW(Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState());
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_UPGRADE_DATA = "UpgradeData";
    private static final String BLOCK_TICKS_TAG = "block_ticks";
    private static final String FLUID_TICKS_TAG = "fluid_ticks";
    public static final String X_POS_TAG = "xPos";
    public static final String Z_POS_TAG = "zPos";
    public static final String HEIGHTMAPS_TAG = "Heightmaps";
    public static final String IS_LIGHT_ON_TAG = "isLightOn";
    public static final String SECTIONS_TAG = "sections";
    public static final String BLOCK_LIGHT_TAG = "BlockLight";
    public static final String SKY_LIGHT_TAG = "SkyLight";

    public static ProtoChunk read(ServerLevel serverLevel, PoiManager poiManager, ChunkPos chunkPos, CompoundTag compoundTag) {
        ChunkAccess chunkAccess;
        ChunkPos chunkPos2 = new ChunkPos(compoundTag.getInt(X_POS_TAG), compoundTag.getInt(Z_POS_TAG));
        if (!Objects.equals(chunkPos, chunkPos2)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", chunkPos, chunkPos, chunkPos2);
        }
        UpgradeData upgradeData = compoundTag.contains(TAG_UPGRADE_DATA, 10) ? new UpgradeData(compoundTag.getCompound(TAG_UPGRADE_DATA), serverLevel) : UpgradeData.EMPTY;
        boolean bl = compoundTag.getBoolean(IS_LIGHT_ON_TAG);
        ListTag listTag = compoundTag.getList(SECTIONS_TAG, 10);
        int i = serverLevel.getSectionsCount();
        LevelChunkSection[] levelChunkSections = new LevelChunkSection[i];
        boolean bl2 = serverLevel.dimensionType().hasSkyLight();
        ServerChunkCache chunkSource = serverLevel.getChunkSource();
        LevelLightEngine levelLightEngine = ((ChunkSource)chunkSource).getLightEngine();
        Registry<Biome> registry = serverLevel.registryAccess().registryOrThrow(Registries.BIOME);
        Codec<PalettedContainerRO<Holder<Biome>>> codec = ChunkSerializer.makeBiomeCodec(registry);
        boolean bl3 = false;
        for (int j = 0; j < listTag.size(); ++j) {
            boolean bl5;
            CompoundTag compoundTag2 = listTag.getCompound(j);
            byte k = compoundTag2.getByte("Y");
            int l = serverLevel.getSectionIndexFromSectionY(k);
            if (l >= 0 && l < levelChunkSections.length) {
                LevelChunkSection levelChunkSection;
                PalettedContainer palettedContainer = compoundTag2.contains("block_states", 10) ? (PalettedContainer)BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, compoundTag2.getCompound("block_states")).promotePartial(string -> ChunkSerializer.logErrors(chunkPos, k, string)).getOrThrow(false, LOGGER::error) : new PalettedContainer(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
                PalettedContainerRO<Holder<Biome>> palettedContainerRO = compoundTag2.contains("biomes", 10) ? (PalettedContainerRO)codec.parse(NbtOps.INSTANCE, compoundTag2.getCompound("biomes")).promotePartial(string -> ChunkSerializer.logErrors(chunkPos, k, string)).getOrThrow(false, LOGGER::error) : new PalettedContainer<Holder<Biome>>(registry.asHolderIdMap(), registry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
                levelChunkSections[l] = levelChunkSection = new LevelChunkSection(k, palettedContainer, palettedContainerRO);
                poiManager.checkConsistencyWithBlocks(chunkPos, levelChunkSection);
            }
            boolean bl4 = compoundTag2.contains(BLOCK_LIGHT_TAG, 7);
            boolean bl6 = bl5 = bl2 && compoundTag2.contains(SKY_LIGHT_TAG, 7);
            if (!bl4 && !bl5) continue;
            if (!bl3) {
                levelLightEngine.retainData(chunkPos, true);
                bl3 = true;
            }
            if (bl4) {
                levelLightEngine.queueSectionData(LightLayer.BLOCK, SectionPos.of(chunkPos, k), new DataLayer(compoundTag2.getByteArray(BLOCK_LIGHT_TAG)), true);
            }
            if (!bl5) continue;
            levelLightEngine.queueSectionData(LightLayer.SKY, SectionPos.of(chunkPos, k), new DataLayer(compoundTag2.getByteArray(SKY_LIGHT_TAG)), true);
        }
        long m = compoundTag.getLong("InhabitedTime");
        ChunkStatus.ChunkType chunkType = ChunkSerializer.getChunkTypeFromTag(compoundTag);
        BlendingData blendingData = compoundTag.contains("blending_data", 10) ? (BlendingData)BlendingData.CODEC.parse(new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag.getCompound("blending_data"))).resultOrPartial(LOGGER::error).orElse(null) : null;
        if (chunkType == ChunkStatus.ChunkType.LEVELCHUNK) {
            LevelChunkTicks<Block> levelChunkTicks = LevelChunkTicks.load(compoundTag.getList(BLOCK_TICKS_TAG, 10), string -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(string)), chunkPos);
            LevelChunkTicks<Fluid> levelChunkTicks2 = LevelChunkTicks.load(compoundTag.getList(FLUID_TICKS_TAG, 10), string -> BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(string)), chunkPos);
            chunkAccess = new LevelChunk(serverLevel.getLevel(), chunkPos, upgradeData, levelChunkTicks, levelChunkTicks2, m, levelChunkSections, ChunkSerializer.postLoadChunk(serverLevel, compoundTag), blendingData);
        } else {
            boolean bl6;
            ProtoChunkTicks<Block> protoChunkTicks = ProtoChunkTicks.load(compoundTag.getList(BLOCK_TICKS_TAG, 10), string -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(string)), chunkPos);
            ProtoChunkTicks<Fluid> protoChunkTicks2 = ProtoChunkTicks.load(compoundTag.getList(FLUID_TICKS_TAG, 10), string -> BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(string)), chunkPos);
            ProtoChunk protoChunk = new ProtoChunk(chunkPos, upgradeData, levelChunkSections, protoChunkTicks, protoChunkTicks2, serverLevel, registry, blendingData);
            chunkAccess = protoChunk;
            chunkAccess.setInhabitedTime(m);
            if (compoundTag.contains("below_zero_retrogen", 10)) {
                BelowZeroRetrogen.CODEC.parse(new Dynamic<CompoundTag>(NbtOps.INSTANCE, compoundTag.getCompound("below_zero_retrogen"))).resultOrPartial(LOGGER::error).ifPresent(protoChunk::setBelowZeroRetrogen);
            }
            ChunkStatus chunkStatus = ChunkStatus.byName(compoundTag.getString("Status"));
            protoChunk.setStatus(chunkStatus);
            if (chunkStatus.isOrAfter(ChunkStatus.FEATURES)) {
                protoChunk.setLightEngine(levelLightEngine);
            }
            BelowZeroRetrogen belowZeroRetrogen = protoChunk.getBelowZeroRetrogen();
            boolean bl7 = bl6 = chunkStatus.isOrAfter(ChunkStatus.LIGHT) || belowZeroRetrogen != null && belowZeroRetrogen.targetStatus().isOrAfter(ChunkStatus.LIGHT);
            if (!bl && bl6) {
                for (BlockPos blockPos : BlockPos.betweenClosed(chunkPos.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), serverLevel.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ())) {
                    if (chunkAccess.getBlockState(blockPos).getLightEmission() == 0) continue;
                    protoChunk.addLight(blockPos);
                }
            }
        }
        chunkAccess.setLightCorrect(bl);
        CompoundTag compoundTag3 = compoundTag.getCompound(HEIGHTMAPS_TAG);
        EnumSet<Heightmap.Types> enumSet = EnumSet.noneOf(Heightmap.Types.class);
        for (Heightmap.Types types : chunkAccess.getStatus().heightmapsAfter()) {
            String string2 = types.getSerializationKey();
            if (compoundTag3.contains(string2, 12)) {
                chunkAccess.setHeightmap(types, compoundTag3.getLongArray(string2));
                continue;
            }
            enumSet.add(types);
        }
        Heightmap.primeHeightmaps(chunkAccess, enumSet);
        CompoundTag compoundTag4 = compoundTag.getCompound("structures");
        chunkAccess.setAllStarts(ChunkSerializer.unpackStructureStart(StructurePieceSerializationContext.fromLevel(serverLevel), compoundTag4, serverLevel.getSeed()));
        chunkAccess.setAllReferences(ChunkSerializer.unpackStructureReferences(serverLevel.registryAccess(), chunkPos, compoundTag4));
        if (compoundTag.getBoolean("shouldSave")) {
            chunkAccess.setUnsaved(true);
        }
        ListTag listTag2 = compoundTag.getList("PostProcessing", 9);
        for (int n = 0; n < listTag2.size(); ++n) {
            ListTag listTag3 = listTag2.getList(n);
            for (int o = 0; o < listTag3.size(); ++o) {
                chunkAccess.addPackedPostProcess(listTag3.getShort(o), n);
            }
        }
        if (chunkType == ChunkStatus.ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk)chunkAccess, false);
        }
        ProtoChunk protoChunk2 = (ProtoChunk)chunkAccess;
        ListTag listTag3 = compoundTag.getList("entities", 10);
        for (int o = 0; o < listTag3.size(); ++o) {
            protoChunk2.addEntity(listTag3.getCompound(o));
        }
        ListTag listTag4 = compoundTag.getList("block_entities", 10);
        for (int p = 0; p < listTag4.size(); ++p) {
            CompoundTag compoundTag5 = listTag4.getCompound(p);
            chunkAccess.setBlockEntityNbt(compoundTag5);
        }
        ListTag listTag5 = compoundTag.getList("Lights", 9);
        for (int q = 0; q < listTag5.size(); ++q) {
            LevelChunkSection levelChunkSection2 = levelChunkSections[q];
            if (levelChunkSection2 == null || levelChunkSection2.hasOnlyAir()) continue;
            ListTag listTag6 = listTag5.getList(q);
            for (int r = 0; r < listTag6.size(); ++r) {
                protoChunk2.addLight(listTag6.getShort(r), q);
            }
        }
        CompoundTag compoundTag5 = compoundTag.getCompound("CarvingMasks");
        for (String string2 : compoundTag5.getAllKeys()) {
            GenerationStep.Carving carving = GenerationStep.Carving.valueOf(string2);
            protoChunk2.setCarvingMask(carving, new CarvingMask(compoundTag5.getLongArray(string2), chunkAccess.getMinBuildHeight()));
        }
        return protoChunk2;
    }

    private static void logErrors(ChunkPos chunkPos, int i, String string) {
        LOGGER.error("Recoverable errors when loading section [" + chunkPos.x + ", " + i + ", " + chunkPos.z + "]: " + string);
    }

    private static Codec<PalettedContainerRO<Holder<Biome>>> makeBiomeCodec(Registry<Biome> registry) {
        return PalettedContainer.codecRO(registry.asHolderIdMap(), registry.holderByNameCodec(), PalettedContainer.Strategy.SECTION_BIOMES, registry.getHolderOrThrow(Biomes.PLAINS));
    }

    public static CompoundTag write(ServerLevel serverLevel, ChunkAccess chunkAccess) {
        CompoundTag compoundTag3;
        UpgradeData upgradeData;
        BelowZeroRetrogen belowZeroRetrogen;
        ChunkPos chunkPos = chunkAccess.getPos();
        CompoundTag compoundTag = NbtUtils.addCurrentDataVersion(new CompoundTag());
        compoundTag.putInt(X_POS_TAG, chunkPos.x);
        compoundTag.putInt("yPos", chunkAccess.getMinSection());
        compoundTag.putInt(Z_POS_TAG, chunkPos.z);
        compoundTag.putLong("LastUpdate", serverLevel.getGameTime());
        compoundTag.putLong("InhabitedTime", chunkAccess.getInhabitedTime());
        compoundTag.putString("Status", chunkAccess.getStatus().getName());
        BlendingData blendingData = chunkAccess.getBlendingData();
        if (blendingData != null) {
            BlendingData.CODEC.encodeStart(NbtOps.INSTANCE, blendingData).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("blending_data", (Tag)tag));
        }
        if ((belowZeroRetrogen = chunkAccess.getBelowZeroRetrogen()) != null) {
            BelowZeroRetrogen.CODEC.encodeStart(NbtOps.INSTANCE, belowZeroRetrogen).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("below_zero_retrogen", (Tag)tag));
        }
        if (!(upgradeData = chunkAccess.getUpgradeData()).isEmpty()) {
            compoundTag.put(TAG_UPGRADE_DATA, upgradeData.write());
        }
        LevelChunkSection[] levelChunkSections = chunkAccess.getSections();
        ListTag listTag = new ListTag();
        ThreadedLevelLightEngine levelLightEngine = serverLevel.getChunkSource().getLightEngine();
        Registry<Biome> registry = serverLevel.registryAccess().registryOrThrow(Registries.BIOME);
        Codec<PalettedContainerRO<Holder<Biome>>> codec = ChunkSerializer.makeBiomeCodec(registry);
        boolean bl = chunkAccess.isLightCorrect();
        for (int i = levelLightEngine.getMinLightSection(); i < levelLightEngine.getMaxLightSection(); ++i) {
            int j = chunkAccess.getSectionIndexFromSectionY(i);
            boolean bl2 = j >= 0 && j < levelChunkSections.length;
            DataLayer dataLayer = levelLightEngine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkPos, i));
            DataLayer dataLayer2 = levelLightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkPos, i));
            if (!bl2 && dataLayer == null && dataLayer2 == null) continue;
            CompoundTag compoundTag2 = new CompoundTag();
            if (bl2) {
                LevelChunkSection levelChunkSection = levelChunkSections[j];
                compoundTag2.put("block_states", BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, levelChunkSection.getStates()).getOrThrow(false, LOGGER::error));
                compoundTag2.put("biomes", codec.encodeStart(NbtOps.INSTANCE, levelChunkSection.getBiomes()).getOrThrow(false, LOGGER::error));
            }
            if (dataLayer != null && !dataLayer.isEmpty()) {
                compoundTag2.putByteArray(BLOCK_LIGHT_TAG, dataLayer.getData());
            }
            if (dataLayer2 != null && !dataLayer2.isEmpty()) {
                compoundTag2.putByteArray(SKY_LIGHT_TAG, dataLayer2.getData());
            }
            if (compoundTag2.isEmpty()) continue;
            compoundTag2.putByte("Y", (byte)i);
            listTag.add(compoundTag2);
        }
        compoundTag.put(SECTIONS_TAG, listTag);
        if (bl) {
            compoundTag.putBoolean(IS_LIGHT_ON_TAG, true);
        }
        ListTag listTag2 = new ListTag();
        for (BlockPos blockPos : chunkAccess.getBlockEntitiesPos()) {
            compoundTag3 = chunkAccess.getBlockEntityNbtForSaving(blockPos);
            if (compoundTag3 == null) continue;
            listTag2.add(compoundTag3);
        }
        compoundTag.put("block_entities", listTag2);
        if (chunkAccess.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
            ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
            ListTag listTag3 = new ListTag();
            listTag3.addAll(protoChunk.getEntities());
            compoundTag.put("entities", listTag3);
            compoundTag.put("Lights", ChunkSerializer.packOffsets(protoChunk.getPackedLights()));
            compoundTag3 = new CompoundTag();
            for (GenerationStep.Carving carving : GenerationStep.Carving.values()) {
                CarvingMask carvingMask = protoChunk.getCarvingMask(carving);
                if (carvingMask == null) continue;
                compoundTag3.putLongArray(carving.toString(), carvingMask.toArray());
            }
            compoundTag.put("CarvingMasks", compoundTag3);
        }
        ChunkSerializer.saveTicks(serverLevel, compoundTag, chunkAccess.getTicksForSerialization());
        compoundTag.put("PostProcessing", ChunkSerializer.packOffsets(chunkAccess.getPostProcessing()));
        CompoundTag compoundTag4 = new CompoundTag();
        for (Map.Entry<Heightmap.Types, Heightmap> entry : chunkAccess.getHeightmaps()) {
            if (!chunkAccess.getStatus().heightmapsAfter().contains(entry.getKey())) continue;
            compoundTag4.put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
        }
        compoundTag.put(HEIGHTMAPS_TAG, compoundTag4);
        compoundTag.put("structures", ChunkSerializer.packStructureData(StructurePieceSerializationContext.fromLevel(serverLevel), chunkPos, chunkAccess.getAllStarts(), chunkAccess.getAllReferences()));
        return compoundTag;
    }

    private static void saveTicks(ServerLevel serverLevel, CompoundTag compoundTag, ChunkAccess.TicksToSave ticksToSave) {
        long l = serverLevel.getLevelData().getGameTime();
        compoundTag.put(BLOCK_TICKS_TAG, ticksToSave.blocks().save(l, block -> BuiltInRegistries.BLOCK.getKey((Block)block).toString()));
        compoundTag.put(FLUID_TICKS_TAG, ticksToSave.fluids().save(l, fluid -> BuiltInRegistries.FLUID.getKey((Fluid)fluid).toString()));
    }

    public static ChunkStatus.ChunkType getChunkTypeFromTag(@Nullable CompoundTag compoundTag) {
        if (compoundTag != null) {
            return ChunkStatus.byName(compoundTag.getString("Status")).getChunkType();
        }
        return ChunkStatus.ChunkType.PROTOCHUNK;
    }

    @Nullable
    private static LevelChunk.PostLoadProcessor postLoadChunk(ServerLevel serverLevel, CompoundTag compoundTag) {
        ListTag listTag = ChunkSerializer.getListOfCompoundsOrNull(compoundTag, "entities");
        ListTag listTag2 = ChunkSerializer.getListOfCompoundsOrNull(compoundTag, "block_entities");
        if (listTag == null && listTag2 == null) {
            return null;
        }
        return levelChunk -> {
            if (listTag != null) {
                serverLevel.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(listTag, serverLevel));
            }
            if (listTag2 != null) {
                for (int i = 0; i < listTag2.size(); ++i) {
                    CompoundTag compoundTag = listTag2.getCompound(i);
                    boolean bl = compoundTag.getBoolean("keepPacked");
                    if (bl) {
                        levelChunk.setBlockEntityNbt(compoundTag);
                        continue;
                    }
                    BlockPos blockPos = BlockEntity.getPosFromTag(compoundTag);
                    BlockEntity blockEntity = BlockEntity.loadStatic(blockPos, levelChunk.getBlockState(blockPos), compoundTag);
                    if (blockEntity == null) continue;
                    levelChunk.setBlockEntity(blockEntity);
                }
            }
        };
    }

    @Nullable
    private static ListTag getListOfCompoundsOrNull(CompoundTag compoundTag, String string) {
        ListTag listTag = compoundTag.getList(string, 10);
        return listTag.isEmpty() ? null : listTag;
    }

    private static CompoundTag packStructureData(StructurePieceSerializationContext structurePieceSerializationContext, ChunkPos chunkPos, Map<Structure, StructureStart> map, Map<Structure, LongSet> map2) {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag compoundTag2 = new CompoundTag();
        Registry<Structure> registry = structurePieceSerializationContext.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (Map.Entry<Structure, StructureStart> entry : map.entrySet()) {
            ResourceLocation resourceLocation = registry.getKey(entry.getKey());
            compoundTag2.put(resourceLocation.toString(), entry.getValue().createTag(structurePieceSerializationContext, chunkPos));
        }
        compoundTag.put("starts", compoundTag2);
        CompoundTag compoundTag3 = new CompoundTag();
        for (Map.Entry<Structure, LongSet> entry2 : map2.entrySet()) {
            if (entry2.getValue().isEmpty()) continue;
            ResourceLocation resourceLocation2 = registry.getKey(entry2.getKey());
            compoundTag3.put(resourceLocation2.toString(), new LongArrayTag(entry2.getValue()));
        }
        compoundTag.put("References", compoundTag3);
        return compoundTag;
    }

    private static Map<Structure, StructureStart> unpackStructureStart(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag, long l) {
        HashMap<Structure, StructureStart> map = Maps.newHashMap();
        Registry<Structure> registry = structurePieceSerializationContext.registryAccess().registryOrThrow(Registries.STRUCTURE);
        CompoundTag compoundTag2 = compoundTag.getCompound("starts");
        for (String string : compoundTag2.getAllKeys()) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
            Structure structure = registry.get(resourceLocation);
            if (structure == null) {
                LOGGER.error("Unknown structure start: {}", (Object)resourceLocation);
                continue;
            }
            StructureStart structureStart = StructureStart.loadStaticStart(structurePieceSerializationContext, compoundTag2.getCompound(string), l);
            if (structureStart == null) continue;
            map.put(structure, structureStart);
        }
        return map;
    }

    private static Map<Structure, LongSet> unpackStructureReferences(RegistryAccess registryAccess, ChunkPos chunkPos, CompoundTag compoundTag) {
        HashMap<Structure, LongSet> map = Maps.newHashMap();
        Registry<Structure> registry = registryAccess.registryOrThrow(Registries.STRUCTURE);
        CompoundTag compoundTag2 = compoundTag.getCompound("References");
        for (String string : compoundTag2.getAllKeys()) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
            Structure structure = registry.get(resourceLocation);
            if (structure == null) {
                LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", (Object)resourceLocation, (Object)chunkPos);
                continue;
            }
            long[] ls = compoundTag2.getLongArray(string);
            if (ls.length == 0) continue;
            map.put(structure, new LongOpenHashSet(Arrays.stream(ls).filter(l -> {
                ChunkPos chunkPos2 = new ChunkPos(l);
                if (chunkPos2.getChessboardDistance(chunkPos) > 8) {
                    LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", resourceLocation, chunkPos2, chunkPos);
                    return false;
                }
                return true;
            }).toArray()));
        }
        return map;
    }

    public static ListTag packOffsets(ShortList[] shortLists) {
        ListTag listTag = new ListTag();
        for (ShortList shortList : shortLists) {
            ListTag listTag2 = new ListTag();
            if (shortList != null) {
                for (Short short_ : shortList) {
                    listTag2.add(ShortTag.valueOf(short_));
                }
            }
            listTag.add(listTag2);
        }
        return listTag;
    }
}

