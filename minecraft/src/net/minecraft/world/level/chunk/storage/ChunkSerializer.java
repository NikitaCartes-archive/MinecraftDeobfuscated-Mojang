package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.ShortTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ProtoTickList;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkSerializer {
	private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codec(
		Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES
	);
	private static final Logger LOGGER = LogManager.getLogger();
	public static final String TAG_UPGRADE_DATA = "UpgradeData";

	public static ProtoChunk read(ServerLevel serverLevel, PoiManager poiManager, ChunkPos chunkPos, CompoundTag compoundTag) {
		CompoundTag compoundTag2 = compoundTag.getCompound("Level");
		ChunkPos chunkPos2 = new ChunkPos(compoundTag2.getInt("xPos"), compoundTag2.getInt("zPos"));
		if (!Objects.equals(chunkPos, chunkPos2)) {
			LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", chunkPos, chunkPos, chunkPos2);
		}

		UpgradeData upgradeData = compoundTag2.contains("UpgradeData", 10)
			? new UpgradeData(compoundTag2.getCompound("UpgradeData"), serverLevel)
			: UpgradeData.EMPTY;
		ProtoTickList<Block> protoTickList = new ProtoTickList<>(
			block -> block == null || block.defaultBlockState().isAir(), chunkPos, compoundTag2.getList("ToBeTicked", 9), serverLevel
		);
		ProtoTickList<Fluid> protoTickList2 = new ProtoTickList<>(
			fluid -> fluid == null || fluid == Fluids.EMPTY, chunkPos, compoundTag2.getList("LiquidsToBeTicked", 9), serverLevel
		);
		boolean bl = compoundTag2.getBoolean("isLightOn");
		ListTag listTag = compoundTag2.getList("Sections", 10);
		int i = serverLevel.getSectionsCount();
		LevelChunkSection[] levelChunkSections = new LevelChunkSection[i];
		boolean bl2 = serverLevel.dimensionType().hasSkyLight();
		ChunkSource chunkSource = serverLevel.getChunkSource();
		LevelLightEngine levelLightEngine = chunkSource.getLightEngine();
		if (bl) {
			levelLightEngine.retainData(chunkPos, true);
		}

		Registry<Biome> registry = serverLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
		Codec<PalettedContainer<Biome>> codec = PalettedContainer.codec(registry, registry, PalettedContainer.Strategy.SECTION_BIOMES);

		for (int j = 0; j < listTag.size(); j++) {
			CompoundTag compoundTag3 = listTag.getCompound(j);
			int k = compoundTag3.getByte("Y");
			int l = serverLevel.getSectionIndexFromSectionY(k);
			if (l >= 0 && l < levelChunkSections.length) {
				PalettedContainer<BlockState> palettedContainer;
				if (compoundTag3.contains("block_states", 10)) {
					palettedContainer = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, compoundTag3.getCompound("block_states")).getOrThrow(false, LOGGER::error);
				} else {
					palettedContainer = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
				}

				PalettedContainer<Biome> palettedContainer2;
				if (compoundTag3.contains("biomes", 10)) {
					palettedContainer2 = codec.parse(NbtOps.INSTANCE, compoundTag3.getCompound("biomes")).getOrThrow(false, LOGGER::error);
				} else {
					palettedContainer2 = new PalettedContainer<>(registry, registry.getOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
				}

				LevelChunkSection levelChunkSection = new LevelChunkSection(k, palettedContainer, palettedContainer2);
				levelChunkSections[l] = levelChunkSection;
				poiManager.checkConsistencyWithBlocks(chunkPos, levelChunkSection);
			}

			if (bl) {
				if (compoundTag3.contains("BlockLight", 7)) {
					levelLightEngine.queueSectionData(LightLayer.BLOCK, SectionPos.of(chunkPos, k), new DataLayer(compoundTag3.getByteArray("BlockLight")), true);
				}

				if (bl2 && compoundTag3.contains("SkyLight", 7)) {
					levelLightEngine.queueSectionData(LightLayer.SKY, SectionPos.of(chunkPos, k), new DataLayer(compoundTag3.getByteArray("SkyLight")), true);
				}
			}
		}

		long m = compoundTag2.getLong("InhabitedTime");
		ChunkStatus.ChunkType chunkType = getChunkTypeFromTag(compoundTag);
		ChunkAccess chunkAccess;
		if (chunkType == ChunkStatus.ChunkType.LEVELCHUNK) {
			TickList<Block> tickList;
			if (compoundTag2.contains("TileTicks", 9)) {
				tickList = ChunkTickList.create(compoundTag2.getList("TileTicks", 10), Registry.BLOCK::getKey, Registry.BLOCK::get);
			} else {
				tickList = protoTickList;
			}

			TickList<Fluid> tickList2;
			if (compoundTag2.contains("LiquidTicks", 9)) {
				tickList2 = ChunkTickList.create(compoundTag2.getList("LiquidTicks", 10), Registry.FLUID::getKey, Registry.FLUID::get);
			} else {
				tickList2 = protoTickList2;
			}

			chunkAccess = new LevelChunk(
				serverLevel.getLevel(),
				chunkPos,
				upgradeData,
				tickList,
				tickList2,
				m,
				levelChunkSections,
				levelChunk -> postLoadChunk(serverLevel, compoundTag2, levelChunk)
			);
		} else {
			ProtoChunk protoChunk = new ProtoChunk(chunkPos, upgradeData, levelChunkSections, protoTickList, protoTickList2, serverLevel, registry);
			chunkAccess = protoChunk;
			protoChunk.setInhabitedTime(m);
			protoChunk.setStatus(ChunkStatus.byName(compoundTag2.getString("Status")));
			if (protoChunk.getStatus().isOrAfter(ChunkStatus.FEATURES)) {
				protoChunk.setLightEngine(levelLightEngine);
			}

			if (!bl && protoChunk.getStatus().isOrAfter(ChunkStatus.LIGHT)) {
				for (BlockPos blockPos : BlockPos.betweenClosed(
					chunkPos.getMinBlockX(),
					serverLevel.getMinBuildHeight(),
					chunkPos.getMinBlockZ(),
					chunkPos.getMaxBlockX(),
					serverLevel.getMaxBuildHeight() - 1,
					chunkPos.getMaxBlockZ()
				)) {
					if (chunkAccess.getBlockState(blockPos).getLightEmission() != 0) {
						protoChunk.addLight(blockPos);
					}
				}
			}
		}

		chunkAccess.setLightCorrect(bl);
		CompoundTag compoundTag4 = compoundTag2.getCompound("Heightmaps");
		EnumSet<Heightmap.Types> enumSet = EnumSet.noneOf(Heightmap.Types.class);

		for (Heightmap.Types types : chunkAccess.getStatus().heightmapsAfter()) {
			String string = types.getSerializationKey();
			if (compoundTag4.contains(string, 12)) {
				chunkAccess.setHeightmap(types, compoundTag4.getLongArray(string));
			} else {
				enumSet.add(types);
			}
		}

		Heightmap.primeHeightmaps(chunkAccess, enumSet);
		CompoundTag compoundTag5 = compoundTag2.getCompound("Structures");
		chunkAccess.setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel(serverLevel), compoundTag5, serverLevel.getSeed()));
		chunkAccess.setAllReferences(unpackStructureReferences(chunkPos, compoundTag5));
		if (compoundTag2.getBoolean("shouldSave")) {
			chunkAccess.setUnsaved(true);
		}

		ListTag listTag2 = compoundTag2.getList("PostProcessing", 9);

		for (int n = 0; n < listTag2.size(); n++) {
			ListTag listTag3 = listTag2.getList(n);

			for (int o = 0; o < listTag3.size(); o++) {
				chunkAccess.addPackedPostProcess(listTag3.getShort(o), n);
			}
		}

		if (chunkType == ChunkStatus.ChunkType.LEVELCHUNK) {
			return new ImposterProtoChunk((LevelChunk)chunkAccess, false);
		} else {
			ProtoChunk protoChunk2 = (ProtoChunk)chunkAccess;
			ListTag listTag3 = compoundTag2.getList("Entities", 10);

			for (int o = 0; o < listTag3.size(); o++) {
				protoChunk2.addEntity(listTag3.getCompound(o));
			}

			ListTag listTag4 = compoundTag2.getList("TileEntities", 10);

			for (int p = 0; p < listTag4.size(); p++) {
				CompoundTag compoundTag6 = listTag4.getCompound(p);
				chunkAccess.setBlockEntityNbt(compoundTag6);
			}

			ListTag listTag5 = compoundTag2.getList("Lights", 9);

			for (int q = 0; q < listTag5.size(); q++) {
				ListTag listTag6 = listTag5.getList(q);

				for (int r = 0; r < listTag6.size(); r++) {
					protoChunk2.addLight(listTag6.getShort(r), q);
				}
			}

			CompoundTag compoundTag6 = compoundTag2.getCompound("CarvingMasks");

			for (String string2 : compoundTag6.getAllKeys()) {
				GenerationStep.Carving carving = GenerationStep.Carving.valueOf(string2);
				protoChunk2.setCarvingMask(carving, BitSet.valueOf(compoundTag6.getByteArray(string2)));
			}

			return protoChunk2;
		}
	}

	public static CompoundTag write(ServerLevel serverLevel, ChunkAccess chunkAccess) {
		ChunkPos chunkPos = chunkAccess.getPos();
		CompoundTag compoundTag = new CompoundTag();
		CompoundTag compoundTag2 = new CompoundTag();
		compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
		compoundTag.put("Level", compoundTag2);
		compoundTag2.putInt("xPos", chunkPos.x);
		compoundTag2.putInt("zPos", chunkPos.z);
		compoundTag2.putLong("LastUpdate", serverLevel.getGameTime());
		compoundTag2.putLong("InhabitedTime", chunkAccess.getInhabitedTime());
		compoundTag2.putString("Status", chunkAccess.getStatus().getName());
		UpgradeData upgradeData = chunkAccess.getUpgradeData();
		if (!upgradeData.isEmpty()) {
			compoundTag2.put("UpgradeData", upgradeData.write());
		}

		LevelChunkSection[] levelChunkSections = chunkAccess.getSections();
		ListTag listTag = new ListTag();
		LevelLightEngine levelLightEngine = serverLevel.getChunkSource().getLightEngine();
		Registry<Biome> registry = serverLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
		Codec<PalettedContainer<Biome>> codec = PalettedContainer.codec(registry, registry, PalettedContainer.Strategy.SECTION_BIOMES);
		boolean bl = chunkAccess.isLightCorrect();

		for (int i = levelLightEngine.getMinLightSection(); i < levelLightEngine.getMaxLightSection(); i++) {
			int j = chunkAccess.getSectionIndexFromSectionY(i);
			boolean bl2 = j >= 0 && j < levelChunkSections.length;
			DataLayer dataLayer = levelLightEngine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkPos, i));
			DataLayer dataLayer2 = levelLightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkPos, i));
			if (bl2 || dataLayer != null || dataLayer2 != null) {
				CompoundTag compoundTag3 = new CompoundTag();
				if (bl2) {
					LevelChunkSection levelChunkSection = levelChunkSections[j];
					compoundTag3.put("block_states", BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, levelChunkSection.getStates()).getOrThrow(false, LOGGER::error));
					compoundTag3.put("biomes", codec.encodeStart(NbtOps.INSTANCE, levelChunkSection.getBiomes()).getOrThrow(false, LOGGER::error));
				}

				if (dataLayer != null && !dataLayer.isEmpty()) {
					compoundTag3.putByteArray("BlockLight", dataLayer.getData());
				}

				if (dataLayer2 != null && !dataLayer2.isEmpty()) {
					compoundTag3.putByteArray("SkyLight", dataLayer2.getData());
				}

				if (!compoundTag3.isEmpty()) {
					compoundTag3.putByte("Y", (byte)i);
					listTag.add(compoundTag3);
				}
			}
		}

		compoundTag2.put("Sections", listTag);
		if (bl) {
			compoundTag2.putBoolean("isLightOn", true);
		}

		ListTag listTag2 = new ListTag();

		for (BlockPos blockPos : chunkAccess.getBlockEntitiesPos()) {
			CompoundTag compoundTag4 = chunkAccess.getBlockEntityNbtForSaving(blockPos);
			if (compoundTag4 != null) {
				listTag2.add(compoundTag4);
			}
		}

		compoundTag2.put("TileEntities", listTag2);
		if (chunkAccess.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
			ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
			ListTag listTag3 = new ListTag();
			listTag3.addAll(protoChunk.getEntities());
			compoundTag2.put("Entities", listTag3);
			compoundTag2.put("Lights", packOffsets(protoChunk.getPackedLights()));
			CompoundTag compoundTag4 = new CompoundTag();

			for (GenerationStep.Carving carving : GenerationStep.Carving.values()) {
				BitSet bitSet = protoChunk.getCarvingMask(carving);
				if (bitSet != null) {
					compoundTag4.putByteArray(carving.toString(), bitSet.toByteArray());
				}
			}

			compoundTag2.put("CarvingMasks", compoundTag4);
		}

		TickList<Block> tickList = chunkAccess.getBlockTicks();
		if (tickList instanceof ProtoTickList) {
			compoundTag2.put("ToBeTicked", ((ProtoTickList)tickList).save());
		} else if (tickList instanceof ChunkTickList) {
			compoundTag2.put("TileTicks", ((ChunkTickList)tickList).save());
		} else {
			compoundTag2.put("TileTicks", serverLevel.getBlockTicks().save(chunkPos));
		}

		TickList<Fluid> tickList2 = chunkAccess.getLiquidTicks();
		if (tickList2 instanceof ProtoTickList) {
			compoundTag2.put("LiquidsToBeTicked", ((ProtoTickList)tickList2).save());
		} else if (tickList2 instanceof ChunkTickList) {
			compoundTag2.put("LiquidTicks", ((ChunkTickList)tickList2).save());
		} else {
			compoundTag2.put("LiquidTicks", serverLevel.getLiquidTicks().save(chunkPos));
		}

		compoundTag2.put("PostProcessing", packOffsets(chunkAccess.getPostProcessing()));
		CompoundTag compoundTag4 = new CompoundTag();

		for (Entry<Heightmap.Types, Heightmap> entry : chunkAccess.getHeightmaps()) {
			if (chunkAccess.getStatus().heightmapsAfter().contains(entry.getKey())) {
				compoundTag4.put(((Heightmap.Types)entry.getKey()).getSerializationKey(), new LongArrayTag(((Heightmap)entry.getValue()).getRawData()));
			}
		}

		compoundTag2.put("Heightmaps", compoundTag4);
		compoundTag2.put(
			"Structures",
			packStructureData(StructurePieceSerializationContext.fromLevel(serverLevel), chunkPos, chunkAccess.getAllStarts(), chunkAccess.getAllReferences())
		);
		return compoundTag;
	}

	public static ChunkStatus.ChunkType getChunkTypeFromTag(@Nullable CompoundTag compoundTag) {
		if (compoundTag != null) {
			ChunkStatus chunkStatus = ChunkStatus.byName(compoundTag.getCompound("Level").getString("Status"));
			if (chunkStatus != null) {
				return chunkStatus.getChunkType();
			}
		}

		return ChunkStatus.ChunkType.PROTOCHUNK;
	}

	private static void postLoadChunk(ServerLevel serverLevel, CompoundTag compoundTag, LevelChunk levelChunk) {
		if (compoundTag.contains("Entities", 9)) {
			ListTag listTag = compoundTag.getList("Entities", 10);
			if (!listTag.isEmpty()) {
				serverLevel.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(listTag, serverLevel));
			}
		}

		ListTag listTag = compoundTag.getList("TileEntities", 10);

		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag2 = listTag.getCompound(i);
			boolean bl = compoundTag2.getBoolean("keepPacked");
			if (bl) {
				levelChunk.setBlockEntityNbt(compoundTag2);
			} else {
				BlockPos blockPos = BlockEntity.getPosFromTag(compoundTag2);
				BlockEntity blockEntity = BlockEntity.loadStatic(blockPos, levelChunk.getBlockState(blockPos), compoundTag2);
				if (blockEntity != null) {
					levelChunk.setBlockEntity(blockEntity);
				}
			}
		}
	}

	private static CompoundTag packStructureData(
		StructurePieceSerializationContext structurePieceSerializationContext,
		ChunkPos chunkPos,
		Map<StructureFeature<?>, StructureStart<?>> map,
		Map<StructureFeature<?>, LongSet> map2
	) {
		CompoundTag compoundTag = new CompoundTag();
		CompoundTag compoundTag2 = new CompoundTag();

		for (Entry<StructureFeature<?>, StructureStart<?>> entry : map.entrySet()) {
			compoundTag2.put(
				((StructureFeature)entry.getKey()).getFeatureName(), ((StructureStart)entry.getValue()).createTag(structurePieceSerializationContext, chunkPos)
			);
		}

		compoundTag.put("Starts", compoundTag2);
		CompoundTag compoundTag3 = new CompoundTag();

		for (Entry<StructureFeature<?>, LongSet> entry2 : map2.entrySet()) {
			compoundTag3.put(((StructureFeature)entry2.getKey()).getFeatureName(), new LongArrayTag((LongSet)entry2.getValue()));
		}

		compoundTag.put("References", compoundTag3);
		return compoundTag;
	}

	private static Map<StructureFeature<?>, StructureStart<?>> unpackStructureStart(
		StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag, long l
	) {
		Map<StructureFeature<?>, StructureStart<?>> map = Maps.<StructureFeature<?>, StructureStart<?>>newHashMap();
		CompoundTag compoundTag2 = compoundTag.getCompound("Starts");

		for (String string : compoundTag2.getAllKeys()) {
			String string2 = string.toLowerCase(Locale.ROOT);
			StructureFeature<?> structureFeature = (StructureFeature<?>)StructureFeature.STRUCTURES_REGISTRY.get(string2);
			if (structureFeature == null) {
				LOGGER.error("Unknown structure start: {}", string2);
			} else {
				StructureStart<?> structureStart = StructureFeature.loadStaticStart(structurePieceSerializationContext, compoundTag2.getCompound(string), l);
				if (structureStart != null) {
					map.put(structureFeature, structureStart);
				}
			}
		}

		return map;
	}

	private static Map<StructureFeature<?>, LongSet> unpackStructureReferences(ChunkPos chunkPos, CompoundTag compoundTag) {
		Map<StructureFeature<?>, LongSet> map = Maps.<StructureFeature<?>, LongSet>newHashMap();
		CompoundTag compoundTag2 = compoundTag.getCompound("References");

		for (String string : compoundTag2.getAllKeys()) {
			String string2 = string.toLowerCase(Locale.ROOT);
			StructureFeature<?> structureFeature = (StructureFeature<?>)StructureFeature.STRUCTURES_REGISTRY.get(string2);
			if (structureFeature == null) {
				LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", string2, chunkPos);
			} else {
				map.put(structureFeature, new LongOpenHashSet(Arrays.stream(compoundTag2.getLongArray(string)).filter(l -> {
					ChunkPos chunkPos2 = new ChunkPos(l);
					if (chunkPos2.getChessboardDistance(chunkPos) > 8) {
						LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", string2, chunkPos2, chunkPos);
						return false;
					} else {
						return true;
					}
				}).toArray()));
			}
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
