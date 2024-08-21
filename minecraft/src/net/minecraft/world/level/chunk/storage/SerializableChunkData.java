package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
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
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
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
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.SavedTick;
import org.slf4j.Logger;

public record SerializableChunkData(
	Registry<Biome> biomeRegistry,
	ChunkPos chunkPos,
	int minSectionY,
	long lastUpdateTime,
	long inhabitedTime,
	ChunkStatus chunkStatus,
	@Nullable BlendingData.Packed blendingData,
	@Nullable BelowZeroRetrogen belowZeroRetrogen,
	UpgradeData upgradeData,
	@Nullable long[] carvingMask,
	Map<Heightmap.Types, long[]> heightmaps,
	ChunkAccess.PackedTicks packedTicks,
	ShortList[] postProcessingSections,
	boolean lightCorrect,
	List<SerializableChunkData.SectionData> sectionData,
	List<CompoundTag> entities,
	List<CompoundTag> blockEntities,
	CompoundTag structureData
) {
	private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codecRW(
		Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState()
	);
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

	@Nullable
	public static SerializableChunkData parse(LevelHeightAccessor levelHeightAccessor, RegistryAccess registryAccess, CompoundTag compoundTag) {
		if (!compoundTag.contains("Status", 8)) {
			return null;
		} else {
			ChunkPos chunkPos = new ChunkPos(compoundTag.getInt("xPos"), compoundTag.getInt("zPos"));
			long l = compoundTag.getLong("LastUpdate");
			long m = compoundTag.getLong("InhabitedTime");
			ChunkStatus chunkStatus = ChunkStatus.byName(compoundTag.getString("Status"));
			UpgradeData upgradeData = compoundTag.contains("UpgradeData", 10)
				? new UpgradeData(compoundTag.getCompound("UpgradeData"), levelHeightAccessor)
				: UpgradeData.EMPTY;
			boolean bl = compoundTag.getBoolean("isLightOn");
			BlendingData.Packed packed;
			if (compoundTag.contains("blending_data", 10)) {
				packed = (BlendingData.Packed)BlendingData.Packed.CODEC
					.parse(NbtOps.INSTANCE, compoundTag.getCompound("blending_data"))
					.resultOrPartial(LOGGER::error)
					.orElse(null);
			} else {
				packed = null;
			}

			BelowZeroRetrogen belowZeroRetrogen;
			if (compoundTag.contains("below_zero_retrogen", 10)) {
				belowZeroRetrogen = (BelowZeroRetrogen)BelowZeroRetrogen.CODEC
					.parse(NbtOps.INSTANCE, compoundTag.getCompound("below_zero_retrogen"))
					.resultOrPartial(LOGGER::error)
					.orElse(null);
			} else {
				belowZeroRetrogen = null;
			}

			long[] ls;
			if (compoundTag.contains("carving_mask", 12)) {
				ls = compoundTag.getLongArray("carving_mask");
			} else {
				ls = null;
			}

			CompoundTag compoundTag2 = compoundTag.getCompound("Heightmaps");
			Map<Heightmap.Types, long[]> map = new EnumMap(Heightmap.Types.class);

			for (Heightmap.Types types : chunkStatus.heightmapsAfter()) {
				String string = types.getSerializationKey();
				if (compoundTag2.contains(string, 12)) {
					map.put(types, compoundTag2.getLongArray(string));
				}
			}

			List<SavedTick<Block>> list = SavedTick.loadTickList(
				compoundTag.getList("block_ticks", 10), string -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(string)), chunkPos
			);
			List<SavedTick<Fluid>> list2 = SavedTick.loadTickList(
				compoundTag.getList("fluid_ticks", 10), string -> BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(string)), chunkPos
			);
			ChunkAccess.PackedTicks packedTicks = new ChunkAccess.PackedTicks(list, list2);
			ListTag listTag = compoundTag.getList("PostProcessing", 9);
			ShortList[] shortLists = new ShortList[listTag.size()];

			for (int i = 0; i < listTag.size(); i++) {
				ListTag listTag2 = listTag.getList(i);
				ShortList shortList = new ShortArrayList(listTag2.size());

				for (int j = 0; j < listTag2.size(); j++) {
					shortList.add(listTag2.getShort(j));
				}

				shortLists[i] = shortList;
			}

			List<CompoundTag> list3 = Lists.transform(compoundTag.getList("entities", 10), tag -> (CompoundTag)tag);
			List<CompoundTag> list4 = Lists.transform(compoundTag.getList("block_entities", 10), tag -> (CompoundTag)tag);
			CompoundTag compoundTag3 = compoundTag.getCompound("structures");
			ListTag listTag3 = compoundTag.getList("sections", 10);
			List<SerializableChunkData.SectionData> list5 = new ArrayList(listTag3.size());
			Registry<Biome> registry = registryAccess.registryOrThrow(Registries.BIOME);
			Codec<PalettedContainerRO<Holder<Biome>>> codec = makeBiomeCodec(registry);

			for (int k = 0; k < listTag3.size(); k++) {
				CompoundTag compoundTag4 = listTag3.getCompound(k);
				int n = compoundTag4.getByte("Y");
				LevelChunkSection levelChunkSection;
				if (n >= levelHeightAccessor.getMinSectionY() && n <= levelHeightAccessor.getMaxSectionY()) {
					PalettedContainer<BlockState> palettedContainer;
					if (compoundTag4.contains("block_states", 10)) {
						palettedContainer = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, compoundTag4.getCompound("block_states"))
							.promotePartial(string -> logErrors(chunkPos, n, string))
							.getOrThrow(SerializableChunkData.ChunkReadException::new);
					} else {
						palettedContainer = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
					}

					PalettedContainerRO<Holder<Biome>> palettedContainerRO;
					if (compoundTag4.contains("biomes", 10)) {
						palettedContainerRO = codec.parse(NbtOps.INSTANCE, compoundTag4.getCompound("biomes"))
							.promotePartial(string -> logErrors(chunkPos, n, string))
							.getOrThrow(SerializableChunkData.ChunkReadException::new);
					} else {
						palettedContainerRO = new PalettedContainer<>(
							registry.asHolderIdMap(), registry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES
						);
					}

					levelChunkSection = new LevelChunkSection(palettedContainer, palettedContainerRO);
				} else {
					levelChunkSection = null;
				}

				DataLayer dataLayer = compoundTag4.contains("BlockLight", 7) ? new DataLayer(compoundTag4.getByteArray("BlockLight")) : null;
				DataLayer dataLayer2 = compoundTag4.contains("SkyLight", 7) ? new DataLayer(compoundTag4.getByteArray("SkyLight")) : null;
				list5.add(new SerializableChunkData.SectionData(n, levelChunkSection, dataLayer, dataLayer2));
			}

			return new SerializableChunkData(
				registry,
				chunkPos,
				levelHeightAccessor.getMinSectionY(),
				l,
				m,
				chunkStatus,
				packed,
				belowZeroRetrogen,
				upgradeData,
				ls,
				map,
				packedTicks,
				shortLists,
				bl,
				list5,
				list3,
				list4,
				compoundTag3
			);
		}
	}

	public ProtoChunk read(ServerLevel serverLevel, PoiManager poiManager, RegionStorageInfo regionStorageInfo, ChunkPos chunkPos) {
		if (!Objects.equals(chunkPos, this.chunkPos)) {
			LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", chunkPos, chunkPos, this.chunkPos);
			serverLevel.getServer().reportMisplacedChunk(this.chunkPos, chunkPos, regionStorageInfo);
		}

		int i = serverLevel.getSectionsCount();
		LevelChunkSection[] levelChunkSections = new LevelChunkSection[i];
		boolean bl = serverLevel.dimensionType().hasSkyLight();
		ChunkSource chunkSource = serverLevel.getChunkSource();
		LevelLightEngine levelLightEngine = chunkSource.getLightEngine();
		Registry<Biome> registry = serverLevel.registryAccess().registryOrThrow(Registries.BIOME);
		boolean bl2 = false;

		for (SerializableChunkData.SectionData sectionData : this.sectionData) {
			SectionPos sectionPos = SectionPos.of(chunkPos, sectionData.y);
			if (sectionData.chunkSection != null) {
				levelChunkSections[serverLevel.getSectionIndexFromSectionY(sectionData.y)] = sectionData.chunkSection;
				poiManager.checkConsistencyWithBlocks(sectionPos, sectionData.chunkSection);
			}

			boolean bl3 = sectionData.blockLight != null;
			boolean bl4 = bl && sectionData.skyLight != null;
			if (bl3 || bl4) {
				if (!bl2) {
					levelLightEngine.retainData(chunkPos, true);
					bl2 = true;
				}

				if (bl3) {
					levelLightEngine.queueSectionData(LightLayer.BLOCK, sectionPos, sectionData.blockLight);
				}

				if (bl4) {
					levelLightEngine.queueSectionData(LightLayer.SKY, sectionPos, sectionData.skyLight);
				}
			}
		}

		ChunkType chunkType = this.chunkStatus.getChunkType();
		ChunkAccess chunkAccess;
		if (chunkType == ChunkType.LEVELCHUNK) {
			LevelChunkTicks<Block> levelChunkTicks = new LevelChunkTicks<>(this.packedTicks.blocks());
			LevelChunkTicks<Fluid> levelChunkTicks2 = new LevelChunkTicks<>(this.packedTicks.fluids());
			chunkAccess = new LevelChunk(
				serverLevel.getLevel(),
				chunkPos,
				this.upgradeData,
				levelChunkTicks,
				levelChunkTicks2,
				this.inhabitedTime,
				levelChunkSections,
				postLoadChunk(serverLevel, this.entities, this.blockEntities),
				BlendingData.unpack(this.blendingData)
			);
		} else {
			ProtoChunkTicks<Block> protoChunkTicks = ProtoChunkTicks.load(this.packedTicks.blocks());
			ProtoChunkTicks<Fluid> protoChunkTicks2 = ProtoChunkTicks.load(this.packedTicks.fluids());
			ProtoChunk protoChunk = new ProtoChunk(
				chunkPos, this.upgradeData, levelChunkSections, protoChunkTicks, protoChunkTicks2, serverLevel, registry, BlendingData.unpack(this.blendingData)
			);
			chunkAccess = protoChunk;
			protoChunk.setInhabitedTime(this.inhabitedTime);
			if (this.belowZeroRetrogen != null) {
				protoChunk.setBelowZeroRetrogen(this.belowZeroRetrogen);
			}

			protoChunk.setPersistedStatus(this.chunkStatus);
			if (this.chunkStatus.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
				protoChunk.setLightEngine(levelLightEngine);
			}
		}

		chunkAccess.setLightCorrect(this.lightCorrect);
		EnumSet<Heightmap.Types> enumSet = EnumSet.noneOf(Heightmap.Types.class);

		for (Heightmap.Types types : chunkAccess.getPersistedStatus().heightmapsAfter()) {
			long[] ls = (long[])this.heightmaps.get(types);
			if (ls != null) {
				chunkAccess.setHeightmap(types, ls);
			} else {
				enumSet.add(types);
			}
		}

		Heightmap.primeHeightmaps(chunkAccess, enumSet);
		chunkAccess.setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel(serverLevel), this.structureData, serverLevel.getSeed()));
		chunkAccess.setAllReferences(unpackStructureReferences(serverLevel.registryAccess(), chunkPos, this.structureData));

		for (int j = 0; j < this.postProcessingSections.length; j++) {
			chunkAccess.addPackedPostProcess(this.postProcessingSections[j], j);
		}

		if (chunkType == ChunkType.LEVELCHUNK) {
			return new ImposterProtoChunk((LevelChunk)chunkAccess, false);
		} else {
			ProtoChunk protoChunk2 = (ProtoChunk)chunkAccess;

			for (CompoundTag compoundTag : this.entities) {
				protoChunk2.addEntity(compoundTag);
			}

			for (CompoundTag compoundTag : this.blockEntities) {
				protoChunk2.setBlockEntityNbt(compoundTag);
			}

			if (this.carvingMask != null) {
				protoChunk2.setCarvingMask(new CarvingMask(this.carvingMask, chunkAccess.getMinY()));
			}

			return protoChunk2;
		}
	}

	private static void logErrors(ChunkPos chunkPos, int i, String string) {
		LOGGER.error("Recoverable errors when loading section [{}, {}, {}]: {}", chunkPos.x, i, chunkPos.z, string);
	}

	private static Codec<PalettedContainerRO<Holder<Biome>>> makeBiomeCodec(Registry<Biome> registry) {
		return PalettedContainer.codecRO(
			registry.asHolderIdMap(), registry.holderByNameCodec(), PalettedContainer.Strategy.SECTION_BIOMES, registry.getHolderOrThrow(Biomes.PLAINS)
		);
	}

	public static SerializableChunkData copyOf(ServerLevel serverLevel, ChunkAccess chunkAccess) {
		ChunkPos chunkPos = chunkAccess.getPos();
		List<SerializableChunkData.SectionData> list = new ArrayList();
		LevelChunkSection[] levelChunkSections = chunkAccess.getSections();
		LevelLightEngine levelLightEngine = serverLevel.getChunkSource().getLightEngine();

		for (int i = levelLightEngine.getMinLightSection(); i < levelLightEngine.getMaxLightSection(); i++) {
			int j = chunkAccess.getSectionIndexFromSectionY(i);
			boolean bl = j >= 0 && j < levelChunkSections.length;
			DataLayer dataLayer = levelLightEngine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkPos, i));
			DataLayer dataLayer2 = levelLightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkPos, i));
			DataLayer dataLayer3 = dataLayer != null && !dataLayer.isEmpty() ? dataLayer.copy() : null;
			DataLayer dataLayer4 = dataLayer2 != null && !dataLayer2.isEmpty() ? dataLayer2.copy() : null;
			if (bl || dataLayer3 != null || dataLayer4 != null) {
				LevelChunkSection levelChunkSection = bl ? levelChunkSections[j].copy() : null;
				list.add(new SerializableChunkData.SectionData(i, levelChunkSection, dataLayer3, dataLayer4));
			}
		}

		List<CompoundTag> list2 = new ArrayList(chunkAccess.getBlockEntitiesPos().size());

		for (BlockPos blockPos : chunkAccess.getBlockEntitiesPos()) {
			CompoundTag compoundTag = chunkAccess.getBlockEntityNbtForSaving(blockPos, serverLevel.registryAccess());
			if (compoundTag != null) {
				list2.add(compoundTag);
			}
		}

		List<CompoundTag> list3 = new ArrayList();
		long[] ls = null;
		if (chunkAccess.getPersistedStatus().getChunkType() == ChunkType.PROTOCHUNK) {
			ProtoChunk protoChunk = (ProtoChunk)chunkAccess;
			list3.addAll(protoChunk.getEntities());
			CarvingMask carvingMask = protoChunk.getCarvingMask();
			if (carvingMask != null) {
				ls = carvingMask.toArray();
			}
		}

		Map<Heightmap.Types, long[]> map = new EnumMap(Heightmap.Types.class);

		for (Entry<Heightmap.Types, Heightmap> entry : chunkAccess.getHeightmaps()) {
			if (chunkAccess.getPersistedStatus().heightmapsAfter().contains(entry.getKey())) {
				long[] ms = ((Heightmap)entry.getValue()).getRawData();
				map.put((Heightmap.Types)entry.getKey(), (long[])ms.clone());
			}
		}

		ChunkAccess.PackedTicks packedTicks = chunkAccess.getTicksForSerialization(serverLevel.getGameTime());
		ShortList[] shortLists = (ShortList[])Arrays.stream(chunkAccess.getPostProcessing())
			.map(shortList -> shortList != null ? new ShortArrayList(shortList) : null)
			.toArray(ShortList[]::new);
		CompoundTag compoundTag2 = packStructureData(
			StructurePieceSerializationContext.fromLevel(serverLevel), chunkPos, chunkAccess.getAllStarts(), chunkAccess.getAllReferences()
		);
		return new SerializableChunkData(
			serverLevel.registryAccess().registryOrThrow(Registries.BIOME),
			chunkPos,
			chunkAccess.getMinSectionY(),
			serverLevel.getGameTime(),
			chunkAccess.getInhabitedTime(),
			chunkAccess.getPersistedStatus(),
			Optionull.map(chunkAccess.getBlendingData(), BlendingData::pack),
			chunkAccess.getBelowZeroRetrogen(),
			chunkAccess.getUpgradeData().copy(),
			ls,
			map,
			packedTicks,
			shortLists,
			chunkAccess.isLightCorrect(),
			list,
			list3,
			list2,
			compoundTag2
		);
	}

	public CompoundTag write() {
		CompoundTag compoundTag = NbtUtils.addCurrentDataVersion(new CompoundTag());
		compoundTag.putInt("xPos", this.chunkPos.x);
		compoundTag.putInt("yPos", this.minSectionY);
		compoundTag.putInt("zPos", this.chunkPos.z);
		compoundTag.putLong("LastUpdate", this.lastUpdateTime);
		compoundTag.putLong("InhabitedTime", this.inhabitedTime);
		compoundTag.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(this.chunkStatus).toString());
		if (this.blendingData != null) {
			BlendingData.Packed.CODEC
				.encodeStart(NbtOps.INSTANCE, this.blendingData)
				.resultOrPartial(LOGGER::error)
				.ifPresent(tag -> compoundTag.put("blending_data", tag));
		}

		if (this.belowZeroRetrogen != null) {
			BelowZeroRetrogen.CODEC
				.encodeStart(NbtOps.INSTANCE, this.belowZeroRetrogen)
				.resultOrPartial(LOGGER::error)
				.ifPresent(tag -> compoundTag.put("below_zero_retrogen", tag));
		}

		if (!this.upgradeData.isEmpty()) {
			compoundTag.put("UpgradeData", this.upgradeData.write());
		}

		ListTag listTag = new ListTag();
		Codec<PalettedContainerRO<Holder<Biome>>> codec = makeBiomeCodec(this.biomeRegistry);

		for (SerializableChunkData.SectionData sectionData : this.sectionData) {
			CompoundTag compoundTag2 = new CompoundTag();
			LevelChunkSection levelChunkSection = sectionData.chunkSection;
			if (levelChunkSection != null) {
				compoundTag2.put("block_states", BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, levelChunkSection.getStates()).getOrThrow());
				compoundTag2.put("biomes", codec.encodeStart(NbtOps.INSTANCE, levelChunkSection.getBiomes()).getOrThrow());
			}

			if (sectionData.blockLight != null) {
				compoundTag2.putByteArray("BlockLight", sectionData.blockLight.getData());
			}

			if (sectionData.skyLight != null) {
				compoundTag2.putByteArray("SkyLight", sectionData.skyLight.getData());
			}

			if (!compoundTag2.isEmpty()) {
				compoundTag2.putByte("Y", (byte)sectionData.y);
				listTag.add(compoundTag2);
			}
		}

		compoundTag.put("sections", listTag);
		if (this.lightCorrect) {
			compoundTag.putBoolean("isLightOn", true);
		}

		ListTag listTag2 = new ListTag();
		listTag2.addAll(this.blockEntities);
		compoundTag.put("block_entities", listTag2);
		if (this.chunkStatus.getChunkType() == ChunkType.PROTOCHUNK) {
			ListTag listTag3 = new ListTag();
			listTag3.addAll(this.entities);
			compoundTag.put("entities", listTag3);
			if (this.carvingMask != null) {
				compoundTag.putLongArray("carving_mask", this.carvingMask);
			}
		}

		saveTicks(compoundTag, this.packedTicks);
		compoundTag.put("PostProcessing", packOffsets(this.postProcessingSections));
		CompoundTag compoundTag3 = new CompoundTag();
		this.heightmaps.forEach((types, ls) -> compoundTag3.put(types.getSerializationKey(), new LongArrayTag(ls)));
		compoundTag.put("Heightmaps", compoundTag3);
		compoundTag.put("structures", this.structureData);
		return compoundTag;
	}

	private static void saveTicks(CompoundTag compoundTag, ChunkAccess.PackedTicks packedTicks) {
		ListTag listTag = new ListTag();

		for (SavedTick<Block> savedTick : packedTicks.blocks()) {
			listTag.add(savedTick.save(block -> BuiltInRegistries.BLOCK.getKey(block).toString()));
		}

		compoundTag.put("block_ticks", listTag);
		ListTag listTag2 = new ListTag();

		for (SavedTick<Fluid> savedTick2 : packedTicks.fluids()) {
			listTag2.add(savedTick2.save(fluid -> BuiltInRegistries.FLUID.getKey(fluid).toString()));
		}

		compoundTag.put("fluid_ticks", listTag2);
	}

	public static ChunkType getChunkTypeFromTag(@Nullable CompoundTag compoundTag) {
		return compoundTag != null ? ChunkStatus.byName(compoundTag.getString("Status")).getChunkType() : ChunkType.PROTOCHUNK;
	}

	@Nullable
	private static LevelChunk.PostLoadProcessor postLoadChunk(ServerLevel serverLevel, List<CompoundTag> list, List<CompoundTag> list2) {
		return list.isEmpty() && list2.isEmpty() ? null : levelChunk -> {
			if (!list.isEmpty()) {
				serverLevel.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(list, serverLevel, EntitySpawnReason.LOAD));
			}

			for (CompoundTag compoundTag : list2) {
				boolean bl = compoundTag.getBoolean("keepPacked");
				if (bl) {
					levelChunk.setBlockEntityNbt(compoundTag);
				} else {
					BlockPos blockPos = BlockEntity.getPosFromTag(compoundTag);
					BlockEntity blockEntity = BlockEntity.loadStatic(blockPos, levelChunk.getBlockState(blockPos), compoundTag, serverLevel.registryAccess());
					if (blockEntity != null) {
						levelChunk.setBlockEntity(blockEntity);
					}
				}
			}
		};
	}

	private static CompoundTag packStructureData(
		StructurePieceSerializationContext structurePieceSerializationContext, ChunkPos chunkPos, Map<Structure, StructureStart> map, Map<Structure, LongSet> map2
	) {
		CompoundTag compoundTag = new CompoundTag();
		CompoundTag compoundTag2 = new CompoundTag();
		Registry<Structure> registry = structurePieceSerializationContext.registryAccess().registryOrThrow(Registries.STRUCTURE);

		for (Entry<Structure, StructureStart> entry : map.entrySet()) {
			ResourceLocation resourceLocation = registry.getKey((Structure)entry.getKey());
			compoundTag2.put(resourceLocation.toString(), ((StructureStart)entry.getValue()).createTag(structurePieceSerializationContext, chunkPos));
		}

		compoundTag.put("starts", compoundTag2);
		CompoundTag compoundTag3 = new CompoundTag();

		for (Entry<Structure, LongSet> entry2 : map2.entrySet()) {
			if (!((LongSet)entry2.getValue()).isEmpty()) {
				ResourceLocation resourceLocation2 = registry.getKey((Structure)entry2.getKey());
				compoundTag3.put(resourceLocation2.toString(), new LongArrayTag((LongSet)entry2.getValue()));
			}
		}

		compoundTag.put("References", compoundTag3);
		return compoundTag;
	}

	private static Map<Structure, StructureStart> unpackStructureStart(
		StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag, long l
	) {
		Map<Structure, StructureStart> map = Maps.<Structure, StructureStart>newHashMap();
		Registry<Structure> registry = structurePieceSerializationContext.registryAccess().registryOrThrow(Registries.STRUCTURE);
		CompoundTag compoundTag2 = compoundTag.getCompound("starts");

		for (String string : compoundTag2.getAllKeys()) {
			ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
			Structure structure = registry.get(resourceLocation);
			if (structure == null) {
				LOGGER.error("Unknown structure start: {}", resourceLocation);
			} else {
				StructureStart structureStart = StructureStart.loadStaticStart(structurePieceSerializationContext, compoundTag2.getCompound(string), l);
				if (structureStart != null) {
					map.put(structure, structureStart);
				}
			}
		}

		return map;
	}

	private static Map<Structure, LongSet> unpackStructureReferences(RegistryAccess registryAccess, ChunkPos chunkPos, CompoundTag compoundTag) {
		Map<Structure, LongSet> map = Maps.<Structure, LongSet>newHashMap();
		Registry<Structure> registry = registryAccess.registryOrThrow(Registries.STRUCTURE);
		CompoundTag compoundTag2 = compoundTag.getCompound("References");

		for (String string : compoundTag2.getAllKeys()) {
			ResourceLocation resourceLocation = ResourceLocation.tryParse(string);
			Structure structure = registry.get(resourceLocation);
			if (structure == null) {
				LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", resourceLocation, chunkPos);
			} else {
				long[] ls = compoundTag2.getLongArray(string);
				if (ls.length != 0) {
					map.put(structure, new LongOpenHashSet(Arrays.stream(ls).filter(l -> {
						ChunkPos chunkPos2 = new ChunkPos(l);
						if (chunkPos2.getChessboardDistance(chunkPos) > 8) {
							LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", resourceLocation, chunkPos2, chunkPos);
							return false;
						} else {
							return true;
						}
					}).toArray()));
				}
			}
		}

		return map;
	}

	private static ListTag packOffsets(ShortList[] shortLists) {
		ListTag listTag = new ListTag();

		for (ShortList shortList : shortLists) {
			ListTag listTag2 = new ListTag();
			if (shortList != null) {
				for (int i = 0; i < shortList.size(); i++) {
					listTag2.add(ShortTag.valueOf(shortList.getShort(i)));
				}
			}

			listTag.add(listTag2);
		}

		return listTag;
	}

	public static class ChunkReadException extends NbtException {
		public ChunkReadException(String string) {
			super(string);
		}
	}

	public static record SectionData(int y, @Nullable LevelChunkSection chunkSection, @Nullable DataLayer blockLight, @Nullable DataLayer skyLight) {
	}
}
