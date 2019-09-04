package net.minecraft.world.level.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LevelChunk implements ChunkAccess {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final LevelChunkSection EMPTY_SECTION = null;
	private final LevelChunkSection[] sections = new LevelChunkSection[16];
	private ChunkBiomeContainer biomes;
	private final Map<BlockPos, CompoundTag> pendingBlockEntities = Maps.<BlockPos, CompoundTag>newHashMap();
	private boolean loaded;
	private final Level level;
	private final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
	private final UpgradeData upgradeData;
	private final Map<BlockPos, BlockEntity> blockEntities = Maps.<BlockPos, BlockEntity>newHashMap();
	private final ClassInstanceMultiMap<Entity>[] entitySections;
	private final Map<String, StructureStart> structureStarts = Maps.<String, StructureStart>newHashMap();
	private final Map<String, LongSet> structuresRefences = Maps.<String, LongSet>newHashMap();
	private final ShortList[] postProcessing = new ShortList[16];
	private TickList<Block> blockTicks;
	private TickList<Fluid> liquidTicks;
	private boolean lastSaveHadEntities;
	private long lastSaveTime;
	private volatile boolean unsaved;
	private long inhabitedTime;
	@Nullable
	private Supplier<ChunkHolder.FullChunkStatus> fullStatus;
	@Nullable
	private Consumer<LevelChunk> postLoad;
	private final ChunkPos chunkPos;
	private volatile boolean isLightCorrect;

	public LevelChunk(Level level, ChunkPos chunkPos, ChunkBiomeContainer chunkBiomeContainer) {
		this(level, chunkPos, chunkBiomeContainer, UpgradeData.EMPTY, EmptyTickList.empty(), EmptyTickList.empty(), 0L, null, null);
	}

	public LevelChunk(
		Level level,
		ChunkPos chunkPos,
		ChunkBiomeContainer chunkBiomeContainer,
		UpgradeData upgradeData,
		TickList<Block> tickList,
		TickList<Fluid> tickList2,
		long l,
		@Nullable LevelChunkSection[] levelChunkSections,
		@Nullable Consumer<LevelChunk> consumer
	) {
		this.entitySections = new ClassInstanceMultiMap[16];
		this.level = level;
		this.chunkPos = chunkPos;
		this.upgradeData = upgradeData;

		for (Heightmap.Types types : Heightmap.Types.values()) {
			if (ChunkStatus.FULL.heightmapsAfter().contains(types)) {
				this.heightmaps.put(types, new Heightmap(this, types));
			}
		}

		for (int i = 0; i < this.entitySections.length; i++) {
			this.entitySections[i] = new ClassInstanceMultiMap<>(Entity.class);
		}

		this.biomes = chunkBiomeContainer;
		this.blockTicks = tickList;
		this.liquidTicks = tickList2;
		this.inhabitedTime = l;
		this.postLoad = consumer;
		if (levelChunkSections != null) {
			if (this.sections.length == levelChunkSections.length) {
				System.arraycopy(levelChunkSections, 0, this.sections, 0, this.sections.length);
			} else {
				LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", levelChunkSections.length, this.sections.length);
			}
		}
	}

	public LevelChunk(Level level, ProtoChunk protoChunk) {
		this(
			level,
			protoChunk.getPos(),
			protoChunk.getBiomes(),
			protoChunk.getUpgradeData(),
			protoChunk.getBlockTicks(),
			protoChunk.getLiquidTicks(),
			protoChunk.getInhabitedTime(),
			protoChunk.getSections(),
			null
		);

		for (CompoundTag compoundTag : protoChunk.getEntities()) {
			EntityType.loadEntityRecursive(compoundTag, level, entity -> {
				this.addEntity(entity);
				return entity;
			});
		}

		for (BlockEntity blockEntity : protoChunk.getBlockEntities().values()) {
			this.addBlockEntity(blockEntity);
		}

		this.pendingBlockEntities.putAll(protoChunk.getBlockEntityNbts());

		for (int i = 0; i < protoChunk.getPostProcessing().length; i++) {
			this.postProcessing[i] = protoChunk.getPostProcessing()[i];
		}

		this.setAllStarts(protoChunk.getAllStarts());
		this.setAllReferences(protoChunk.getAllReferences());

		for (Entry<Heightmap.Types, Heightmap> entry : protoChunk.getHeightmaps()) {
			if (ChunkStatus.FULL.heightmapsAfter().contains(entry.getKey())) {
				this.getOrCreateHeightmapUnprimed((Heightmap.Types)entry.getKey()).setRawData(((Heightmap)entry.getValue()).getRawData());
			}
		}

		this.setLightCorrect(protoChunk.isLightCorrect());
		this.unsaved = true;
	}

	@Override
	public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types types) {
		return (Heightmap)this.heightmaps.computeIfAbsent(types, typesx -> new Heightmap(this, typesx));
	}

	@Override
	public Set<BlockPos> getBlockEntitiesPos() {
		Set<BlockPos> set = Sets.<BlockPos>newHashSet(this.pendingBlockEntities.keySet());
		set.addAll(this.blockEntities.keySet());
		return set;
	}

	@Override
	public LevelChunkSection[] getSections() {
		return this.sections;
	}

	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();
		if (this.level.getGeneratorType() == LevelType.DEBUG_ALL_BLOCK_STATES) {
			BlockState blockState = null;
			if (j == 60) {
				blockState = Blocks.BARRIER.defaultBlockState();
			}

			if (j == 70) {
				blockState = DebugLevelSource.getBlockStateFor(i, k);
			}

			return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
		} else {
			try {
				if (j >= 0 && j >> 4 < this.sections.length) {
					LevelChunkSection levelChunkSection = this.sections[j >> 4];
					if (!LevelChunkSection.isEmpty(levelChunkSection)) {
						return levelChunkSection.getBlockState(i & 15, j & 15, k & 15);
					}
				}

				return Blocks.AIR.defaultBlockState();
			} catch (Throwable var8) {
				CrashReport crashReport = CrashReport.forThrowable(var8, "Getting block state");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Block being got");
				crashReportCategory.setDetail("Location", (CrashReportDetail<String>)(() -> CrashReportCategory.formatLocation(i, j, k)));
				throw new ReportedException(crashReport);
			}
		}
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		return this.getFluidState(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	public FluidState getFluidState(int i, int j, int k) {
		try {
			if (j >= 0 && j >> 4 < this.sections.length) {
				LevelChunkSection levelChunkSection = this.sections[j >> 4];
				if (!LevelChunkSection.isEmpty(levelChunkSection)) {
					return levelChunkSection.getFluidState(i & 15, j & 15, k & 15);
				}
			}

			return Fluids.EMPTY.defaultFluidState();
		} catch (Throwable var7) {
			CrashReport crashReport = CrashReport.forThrowable(var7, "Getting fluid state");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Block being got");
			crashReportCategory.setDetail("Location", (CrashReportDetail<String>)(() -> CrashReportCategory.formatLocation(i, j, k)));
			throw new ReportedException(crashReport);
		}
	}

	@Nullable
	@Override
	public BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl) {
		int i = blockPos.getX() & 15;
		int j = blockPos.getY();
		int k = blockPos.getZ() & 15;
		LevelChunkSection levelChunkSection = this.sections[j >> 4];
		if (levelChunkSection == EMPTY_SECTION) {
			if (blockState.isAir()) {
				return null;
			}

			levelChunkSection = new LevelChunkSection(j >> 4 << 4);
			this.sections[j >> 4] = levelChunkSection;
		}

		boolean bl2 = levelChunkSection.isEmpty();
		BlockState blockState2 = levelChunkSection.setBlockState(i, j & 15, k, blockState);
		if (blockState2 == blockState) {
			return null;
		} else {
			Block block = blockState.getBlock();
			Block block2 = blockState2.getBlock();
			((Heightmap)this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING)).update(i, j, k, blockState);
			((Heightmap)this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES)).update(i, j, k, blockState);
			((Heightmap)this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR)).update(i, j, k, blockState);
			((Heightmap)this.heightmaps.get(Heightmap.Types.WORLD_SURFACE)).update(i, j, k, blockState);
			boolean bl3 = levelChunkSection.isEmpty();
			if (bl2 != bl3) {
				this.level.getChunkSource().getLightEngine().updateSectionStatus(blockPos, bl3);
			}

			if (!this.level.isClientSide) {
				blockState2.onRemove(this.level, blockPos, blockState, bl);
			} else if (block2 != block && block2 instanceof EntityBlock) {
				this.level.removeBlockEntity(blockPos);
			}

			if (levelChunkSection.getBlockState(i, j & 15, k).getBlock() != block) {
				return null;
			} else {
				if (block2 instanceof EntityBlock) {
					BlockEntity blockEntity = this.getBlockEntity(blockPos, LevelChunk.EntityCreationType.CHECK);
					if (blockEntity != null) {
						blockEntity.clearCache();
					}
				}

				if (!this.level.isClientSide) {
					blockState.onPlace(this.level, blockPos, blockState2, bl);
				}

				if (block instanceof EntityBlock) {
					BlockEntity blockEntity = this.getBlockEntity(blockPos, LevelChunk.EntityCreationType.CHECK);
					if (blockEntity == null) {
						blockEntity = ((EntityBlock)block).newBlockEntity(this.level);
						this.level.setBlockEntity(blockPos, blockEntity);
					} else {
						blockEntity.clearCache();
					}
				}

				this.unsaved = true;
				return blockState2;
			}
		}
	}

	@Nullable
	public LevelLightEngine getLightEngine() {
		return this.level.getChunkSource().getLightEngine();
	}

	@Override
	public void addEntity(Entity entity) {
		this.lastSaveHadEntities = true;
		int i = Mth.floor(entity.x / 16.0);
		int j = Mth.floor(entity.z / 16.0);
		if (i != this.chunkPos.x || j != this.chunkPos.z) {
			LOGGER.warn("Wrong location! ({}, {}) should be ({}, {}), {}", i, j, this.chunkPos.x, this.chunkPos.z, entity);
			entity.removed = true;
		}

		int k = Mth.floor(entity.y / 16.0);
		if (k < 0) {
			k = 0;
		}

		if (k >= this.entitySections.length) {
			k = this.entitySections.length - 1;
		}

		entity.inChunk = true;
		entity.xChunk = this.chunkPos.x;
		entity.yChunk = k;
		entity.zChunk = this.chunkPos.z;
		this.entitySections[k].add(entity);
	}

	@Override
	public void setHeightmap(Heightmap.Types types, long[] ls) {
		((Heightmap)this.heightmaps.get(types)).setRawData(ls);
	}

	public void removeEntity(Entity entity) {
		this.removeEntity(entity, entity.yChunk);
	}

	public void removeEntity(Entity entity, int i) {
		if (i < 0) {
			i = 0;
		}

		if (i >= this.entitySections.length) {
			i = this.entitySections.length - 1;
		}

		this.entitySections[i].remove(entity);
	}

	@Override
	public int getHeight(Heightmap.Types types, int i, int j) {
		return ((Heightmap)this.heightmaps.get(types)).getFirstAvailable(i & 15, j & 15) - 1;
	}

	@Nullable
	private BlockEntity createBlockEntity(BlockPos blockPos) {
		BlockState blockState = this.getBlockState(blockPos);
		Block block = blockState.getBlock();
		return !block.isEntityBlock() ? null : ((EntityBlock)block).newBlockEntity(this.level);
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		return this.getBlockEntity(blockPos, LevelChunk.EntityCreationType.CHECK);
	}

	@Nullable
	public BlockEntity getBlockEntity(BlockPos blockPos, LevelChunk.EntityCreationType entityCreationType) {
		BlockEntity blockEntity = (BlockEntity)this.blockEntities.get(blockPos);
		if (blockEntity == null) {
			CompoundTag compoundTag = (CompoundTag)this.pendingBlockEntities.remove(blockPos);
			if (compoundTag != null) {
				BlockEntity blockEntity2 = this.promotePendingBlockEntity(blockPos, compoundTag);
				if (blockEntity2 != null) {
					return blockEntity2;
				}
			}
		}

		if (blockEntity == null) {
			if (entityCreationType == LevelChunk.EntityCreationType.IMMEDIATE) {
				blockEntity = this.createBlockEntity(blockPos);
				this.level.setBlockEntity(blockPos, blockEntity);
			}
		} else if (blockEntity.isRemoved()) {
			this.blockEntities.remove(blockPos);
			return null;
		}

		return blockEntity;
	}

	public void addBlockEntity(BlockEntity blockEntity) {
		this.setBlockEntity(blockEntity.getBlockPos(), blockEntity);
		if (this.loaded || this.level.isClientSide()) {
			this.level.setBlockEntity(blockEntity.getBlockPos(), blockEntity);
		}
	}

	@Override
	public void setBlockEntity(BlockPos blockPos, BlockEntity blockEntity) {
		if (this.getBlockState(blockPos).getBlock() instanceof EntityBlock) {
			blockEntity.setLevel(this.level);
			blockEntity.setPosition(blockPos);
			blockEntity.clearRemoved();
			BlockEntity blockEntity2 = (BlockEntity)this.blockEntities.put(blockPos.immutable(), blockEntity);
			if (blockEntity2 != null && blockEntity2 != blockEntity) {
				blockEntity2.setRemoved();
			}
		}
	}

	@Override
	public void setBlockEntityNbt(CompoundTag compoundTag) {
		this.pendingBlockEntities.put(new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z")), compoundTag);
	}

	@Nullable
	@Override
	public CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos) {
		BlockEntity blockEntity = this.getBlockEntity(blockPos);
		if (blockEntity != null && !blockEntity.isRemoved()) {
			CompoundTag compoundTag = blockEntity.save(new CompoundTag());
			compoundTag.putBoolean("keepPacked", false);
			return compoundTag;
		} else {
			CompoundTag compoundTag = (CompoundTag)this.pendingBlockEntities.get(blockPos);
			if (compoundTag != null) {
				compoundTag = compoundTag.copy();
				compoundTag.putBoolean("keepPacked", true);
			}

			return compoundTag;
		}
	}

	@Override
	public void removeBlockEntity(BlockPos blockPos) {
		if (this.loaded || this.level.isClientSide()) {
			BlockEntity blockEntity = (BlockEntity)this.blockEntities.remove(blockPos);
			if (blockEntity != null) {
				blockEntity.setRemoved();
			}
		}
	}

	public void runPostLoad() {
		if (this.postLoad != null) {
			this.postLoad.accept(this);
			this.postLoad = null;
		}
	}

	public void markUnsaved() {
		this.unsaved = true;
	}

	public void getEntities(@Nullable Entity entity, AABB aABB, List<Entity> list, @Nullable Predicate<? super Entity> predicate) {
		int i = Mth.floor((aABB.minY - 2.0) / 16.0);
		int j = Mth.floor((aABB.maxY + 2.0) / 16.0);
		i = Mth.clamp(i, 0, this.entitySections.length - 1);
		j = Mth.clamp(j, 0, this.entitySections.length - 1);

		for (int k = i; k <= j; k++) {
			if (!this.entitySections[k].isEmpty()) {
				for (Entity entity2 : this.entitySections[k]) {
					if (entity2.getBoundingBox().intersects(aABB) && entity2 != entity) {
						if (predicate == null || predicate.test(entity2)) {
							list.add(entity2);
						}

						if (entity2 instanceof EnderDragon) {
							for (EnderDragonPart enderDragonPart : ((EnderDragon)entity2).getSubEntities()) {
								if (enderDragonPart != entity && enderDragonPart.getBoundingBox().intersects(aABB) && (predicate == null || predicate.test(enderDragonPart))) {
									list.add(enderDragonPart);
								}
							}
						}
					}
				}
			}
		}
	}

	public void getEntities(@Nullable EntityType<?> entityType, AABB aABB, List<Entity> list, Predicate<? super Entity> predicate) {
		int i = Mth.floor((aABB.minY - 2.0) / 16.0);
		int j = Mth.floor((aABB.maxY + 2.0) / 16.0);
		i = Mth.clamp(i, 0, this.entitySections.length - 1);
		j = Mth.clamp(j, 0, this.entitySections.length - 1);

		for (int k = i; k <= j; k++) {
			for (Entity entity : this.entitySections[k].find(Entity.class)) {
				if ((entityType == null || entity.getType() == entityType) && entity.getBoundingBox().intersects(aABB) && predicate.test(entity)) {
					list.add(entity);
				}
			}
		}
	}

	public <T extends Entity> void getEntitiesOfClass(Class<? extends T> class_, AABB aABB, List<T> list, @Nullable Predicate<? super T> predicate) {
		int i = Mth.floor((aABB.minY - 2.0) / 16.0);
		int j = Mth.floor((aABB.maxY + 2.0) / 16.0);
		i = Mth.clamp(i, 0, this.entitySections.length - 1);
		j = Mth.clamp(j, 0, this.entitySections.length - 1);

		for (int k = i; k <= j; k++) {
			for (T entity : this.entitySections[k].find(class_)) {
				if (entity.getBoundingBox().intersects(aABB) && (predicate == null || predicate.test(entity))) {
					list.add(entity);
				}
			}
		}
	}

	public boolean isEmpty() {
		return false;
	}

	@Override
	public ChunkPos getPos() {
		return this.chunkPos;
	}

	@Environment(EnvType.CLIENT)
	public void replaceWithPacketData(@Nullable ChunkBiomeContainer chunkBiomeContainer, FriendlyByteBuf friendlyByteBuf, CompoundTag compoundTag, int i) {
		boolean bl = chunkBiomeContainer != null;
		Predicate<BlockPos> predicate = bl ? blockPos -> true : blockPos -> (i & 1 << (blockPos.getY() >> 4)) != 0;
		Sets.newHashSet(this.blockEntities.keySet()).stream().filter(predicate).forEach(this.level::removeBlockEntity);

		for (int j = 0; j < this.sections.length; j++) {
			LevelChunkSection levelChunkSection = this.sections[j];
			if ((i & 1 << j) == 0) {
				if (bl && levelChunkSection != EMPTY_SECTION) {
					this.sections[j] = EMPTY_SECTION;
				}
			} else {
				if (levelChunkSection == EMPTY_SECTION) {
					levelChunkSection = new LevelChunkSection(j << 4);
					this.sections[j] = levelChunkSection;
				}

				levelChunkSection.read(friendlyByteBuf);
			}
		}

		if (chunkBiomeContainer != null) {
			this.biomes = chunkBiomeContainer;
		}

		for (Heightmap.Types types : Heightmap.Types.values()) {
			String string = types.getSerializationKey();
			if (compoundTag.contains(string, 12)) {
				this.setHeightmap(types, compoundTag.getLongArray(string));
			}
		}

		for (BlockEntity blockEntity : this.blockEntities.values()) {
			blockEntity.clearCache();
		}
	}

	@Override
	public ChunkBiomeContainer getBiomes() {
		return this.biomes;
	}

	public void setLoaded(boolean bl) {
		this.loaded = bl;
	}

	public Level getLevel() {
		return this.level;
	}

	@Override
	public Collection<Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
		return Collections.unmodifiableSet(this.heightmaps.entrySet());
	}

	public Map<BlockPos, BlockEntity> getBlockEntities() {
		return this.blockEntities;
	}

	public ClassInstanceMultiMap<Entity>[] getEntitySections() {
		return this.entitySections;
	}

	@Override
	public CompoundTag getBlockEntityNbt(BlockPos blockPos) {
		return (CompoundTag)this.pendingBlockEntities.get(blockPos);
	}

	@Override
	public Stream<BlockPos> getLights() {
		return StreamSupport.stream(
				BlockPos.betweenClosed(this.chunkPos.getMinBlockX(), 0, this.chunkPos.getMinBlockZ(), this.chunkPos.getMaxBlockX(), 255, this.chunkPos.getMaxBlockZ())
					.spliterator(),
				false
			)
			.filter(blockPos -> this.getBlockState(blockPos).getLightEmission() != 0);
	}

	@Override
	public TickList<Block> getBlockTicks() {
		return this.blockTicks;
	}

	@Override
	public TickList<Fluid> getLiquidTicks() {
		return this.liquidTicks;
	}

	@Override
	public void setUnsaved(boolean bl) {
		this.unsaved = bl;
	}

	@Override
	public boolean isUnsaved() {
		return this.unsaved || this.lastSaveHadEntities && this.level.getGameTime() != this.lastSaveTime;
	}

	public void setLastSaveHadEntities(boolean bl) {
		this.lastSaveHadEntities = bl;
	}

	@Override
	public void setLastSaveTime(long l) {
		this.lastSaveTime = l;
	}

	@Nullable
	@Override
	public StructureStart getStartForFeature(String string) {
		return (StructureStart)this.structureStarts.get(string);
	}

	@Override
	public void setStartForFeature(String string, StructureStart structureStart) {
		this.structureStarts.put(string, structureStart);
	}

	@Override
	public Map<String, StructureStart> getAllStarts() {
		return this.structureStarts;
	}

	@Override
	public void setAllStarts(Map<String, StructureStart> map) {
		this.structureStarts.clear();
		this.structureStarts.putAll(map);
	}

	@Override
	public LongSet getReferencesForFeature(String string) {
		return (LongSet)this.structuresRefences.computeIfAbsent(string, stringx -> new LongOpenHashSet());
	}

	@Override
	public void addReferenceForFeature(String string, long l) {
		((LongSet)this.structuresRefences.computeIfAbsent(string, stringx -> new LongOpenHashSet())).add(l);
	}

	@Override
	public Map<String, LongSet> getAllReferences() {
		return this.structuresRefences;
	}

	@Override
	public void setAllReferences(Map<String, LongSet> map) {
		this.structuresRefences.clear();
		this.structuresRefences.putAll(map);
	}

	@Override
	public long getInhabitedTime() {
		return this.inhabitedTime;
	}

	@Override
	public void setInhabitedTime(long l) {
		this.inhabitedTime = l;
	}

	public void postProcessGeneration() {
		ChunkPos chunkPos = this.getPos();

		for (int i = 0; i < this.postProcessing.length; i++) {
			if (this.postProcessing[i] != null) {
				for (Short short_ : this.postProcessing[i]) {
					BlockPos blockPos = ProtoChunk.unpackOffsetCoordinates(short_, i, chunkPos);
					BlockState blockState = this.getBlockState(blockPos);
					BlockState blockState2 = Block.updateFromNeighbourShapes(blockState, this.level, blockPos);
					this.level.setBlock(blockPos, blockState2, 20);
				}

				this.postProcessing[i].clear();
			}
		}

		this.unpackTicks();

		for (BlockPos blockPos2 : Sets.newHashSet(this.pendingBlockEntities.keySet())) {
			this.getBlockEntity(blockPos2);
		}

		this.pendingBlockEntities.clear();
		this.upgradeData.upgrade(this);
	}

	@Nullable
	private BlockEntity promotePendingBlockEntity(BlockPos blockPos, CompoundTag compoundTag) {
		BlockEntity blockEntity;
		if ("DUMMY".equals(compoundTag.getString("id"))) {
			Block block = this.getBlockState(blockPos).getBlock();
			if (block instanceof EntityBlock) {
				blockEntity = ((EntityBlock)block).newBlockEntity(this.level);
			} else {
				blockEntity = null;
				LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", blockPos, this.getBlockState(blockPos));
			}
		} else {
			blockEntity = BlockEntity.loadStatic(compoundTag);
		}

		if (blockEntity != null) {
			blockEntity.setPosition(blockPos);
			this.addBlockEntity(blockEntity);
		} else {
			LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", this.getBlockState(blockPos), blockPos);
		}

		return blockEntity;
	}

	@Override
	public UpgradeData getUpgradeData() {
		return this.upgradeData;
	}

	@Override
	public ShortList[] getPostProcessing() {
		return this.postProcessing;
	}

	public void unpackTicks() {
		if (this.blockTicks instanceof ProtoTickList) {
			((ProtoTickList)this.blockTicks).copyOut(this.level.getBlockTicks(), blockPos -> this.getBlockState(blockPos).getBlock());
			this.blockTicks = EmptyTickList.empty();
		} else if (this.blockTicks instanceof ChunkTickList) {
			this.level.getBlockTicks().addAll(((ChunkTickList)this.blockTicks).ticks());
			this.blockTicks = EmptyTickList.empty();
		}

		if (this.liquidTicks instanceof ProtoTickList) {
			((ProtoTickList)this.liquidTicks).copyOut(this.level.getLiquidTicks(), blockPos -> this.getFluidState(blockPos).getType());
			this.liquidTicks = EmptyTickList.empty();
		} else if (this.liquidTicks instanceof ChunkTickList) {
			this.level.getLiquidTicks().addAll(((ChunkTickList)this.liquidTicks).ticks());
			this.liquidTicks = EmptyTickList.empty();
		}
	}

	public void packTicks(ServerLevel serverLevel) {
		if (this.blockTicks == EmptyTickList.empty()) {
			this.blockTicks = new ChunkTickList<>(Registry.BLOCK::getKey, serverLevel.getBlockTicks().fetchTicksInChunk(this.chunkPos, true, false));
			this.setUnsaved(true);
		}

		if (this.liquidTicks == EmptyTickList.empty()) {
			this.liquidTicks = new ChunkTickList<>(Registry.FLUID::getKey, serverLevel.getLiquidTicks().fetchTicksInChunk(this.chunkPos, true, false));
			this.setUnsaved(true);
		}
	}

	@Override
	public ChunkStatus getStatus() {
		return ChunkStatus.FULL;
	}

	public ChunkHolder.FullChunkStatus getFullStatus() {
		return this.fullStatus == null ? ChunkHolder.FullChunkStatus.BORDER : (ChunkHolder.FullChunkStatus)this.fullStatus.get();
	}

	public void setFullStatus(Supplier<ChunkHolder.FullChunkStatus> supplier) {
		this.fullStatus = supplier;
	}

	@Override
	public boolean isLightCorrect() {
		return this.isLightCorrect;
	}

	@Override
	public void setLightCorrect(boolean bl) {
		this.isLightCorrect = bl;
		this.setUnsaved(true);
	}

	public static enum EntityCreationType {
		IMMEDIATE,
		QUEUED,
		CHECK;
	}
}
