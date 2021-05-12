package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.EuclideanGameEventDispatcher;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LevelChunk implements ChunkAccess {
	static final Logger LOGGER = LogManager.getLogger();
	private static final TickingBlockEntity NULL_TICKER = new TickingBlockEntity() {
		@Override
		public void tick() {
		}

		@Override
		public boolean isRemoved() {
			return true;
		}

		@Override
		public BlockPos getPos() {
			return BlockPos.ZERO;
		}

		@Override
		public String getType() {
			return "<null>";
		}
	};
	@Nullable
	public static final LevelChunkSection EMPTY_SECTION = null;
	private final LevelChunkSection[] sections;
	private ChunkBiomeContainer biomes;
	private final Map<BlockPos, CompoundTag> pendingBlockEntities = Maps.<BlockPos, CompoundTag>newHashMap();
	private final Map<BlockPos, LevelChunk.RebindableTickingBlockEntityWrapper> tickersInLevel = Maps.<BlockPos, LevelChunk.RebindableTickingBlockEntityWrapper>newHashMap();
	private boolean loaded;
	final Level level;
	private final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
	private final UpgradeData upgradeData;
	private final Map<BlockPos, BlockEntity> blockEntities = Maps.<BlockPos, BlockEntity>newHashMap();
	private final Map<StructureFeature<?>, StructureStart<?>> structureStarts = Maps.<StructureFeature<?>, StructureStart<?>>newHashMap();
	private final Map<StructureFeature<?>, LongSet> structuresRefences = Maps.<StructureFeature<?>, LongSet>newHashMap();
	private final ShortList[] postProcessing;
	private TickList<Block> blockTicks;
	private TickList<Fluid> liquidTicks;
	private volatile boolean unsaved;
	private long inhabitedTime;
	@Nullable
	private Supplier<ChunkHolder.FullChunkStatus> fullStatus;
	@Nullable
	private Consumer<LevelChunk> postLoad;
	private final ChunkPos chunkPos;
	private volatile boolean isLightCorrect;
	private final Int2ObjectMap<GameEventDispatcher> gameEventDispatcherSections;

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
		this.level = level;
		this.chunkPos = chunkPos;
		this.upgradeData = upgradeData;
		this.gameEventDispatcherSections = new Int2ObjectOpenHashMap<>();

		for (Heightmap.Types types : Heightmap.Types.values()) {
			if (ChunkStatus.FULL.heightmapsAfter().contains(types)) {
				this.heightmaps.put(types, new Heightmap(this, types));
			}
		}

		this.biomes = chunkBiomeContainer;
		this.blockTicks = tickList;
		this.liquidTicks = tickList2;
		this.inhabitedTime = l;
		this.postLoad = consumer;
		this.sections = new LevelChunkSection[level.getSectionsCount()];
		if (levelChunkSections != null) {
			if (this.sections.length == levelChunkSections.length) {
				System.arraycopy(levelChunkSections, 0, this.sections, 0, this.sections.length);
			} else {
				LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", levelChunkSections.length, this.sections.length);
			}
		}

		this.postProcessing = new ShortList[level.getSectionsCount()];
	}

	public LevelChunk(ServerLevel serverLevel, ProtoChunk protoChunk, @Nullable Consumer<LevelChunk> consumer) {
		this(
			serverLevel,
			protoChunk.getPos(),
			protoChunk.getBiomes(),
			protoChunk.getUpgradeData(),
			protoChunk.getBlockTicks(),
			protoChunk.getLiquidTicks(),
			protoChunk.getInhabitedTime(),
			protoChunk.getSections(),
			consumer
		);

		for (BlockEntity blockEntity : protoChunk.getBlockEntities().values()) {
			this.setBlockEntity(blockEntity);
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
	public GameEventDispatcher getEventDispatcher(int i) {
		return this.gameEventDispatcherSections.computeIfAbsent(i, ix -> new EuclideanGameEventDispatcher(this.level));
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
		if (this.level.isDebug()) {
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
				int l = this.getSectionIndex(j);
				if (l >= 0 && l < this.sections.length) {
					LevelChunkSection levelChunkSection = this.sections[l];
					if (!LevelChunkSection.isEmpty(levelChunkSection)) {
						return levelChunkSection.getBlockState(i & 15, j & 15, k & 15);
					}
				}

				return Blocks.AIR.defaultBlockState();
			} catch (Throwable var8) {
				CrashReport crashReport = CrashReport.forThrowable(var8, "Getting block state");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Block being got");
				crashReportCategory.setDetail("Location", (CrashReportDetail<String>)(() -> CrashReportCategory.formatLocation(this, i, j, k)));
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
			int l = this.getSectionIndex(j);
			if (l >= 0 && l < this.sections.length) {
				LevelChunkSection levelChunkSection = this.sections[l];
				if (!LevelChunkSection.isEmpty(levelChunkSection)) {
					return levelChunkSection.getFluidState(i & 15, j & 15, k & 15);
				}
			}

			return Fluids.EMPTY.defaultFluidState();
		} catch (Throwable var7) {
			CrashReport crashReport = CrashReport.forThrowable(var7, "Getting fluid state");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Block being got");
			crashReportCategory.setDetail("Location", (CrashReportDetail<String>)(() -> CrashReportCategory.formatLocation(this, i, j, k)));
			throw new ReportedException(crashReport);
		}
	}

	@Nullable
	@Override
	public BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl) {
		int i = blockPos.getY();
		int j = this.getSectionIndex(i);
		LevelChunkSection levelChunkSection = this.sections[j];
		if (levelChunkSection == EMPTY_SECTION) {
			if (blockState.isAir()) {
				return null;
			}

			levelChunkSection = new LevelChunkSection(SectionPos.blockToSectionCoord(i));
			this.sections[j] = levelChunkSection;
		}

		boolean bl2 = levelChunkSection.isEmpty();
		int k = blockPos.getX() & 15;
		int l = i & 15;
		int m = blockPos.getZ() & 15;
		BlockState blockState2 = levelChunkSection.setBlockState(k, l, m, blockState);
		if (blockState2 == blockState) {
			return null;
		} else {
			Block block = blockState.getBlock();
			((Heightmap)this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING)).update(k, i, m, blockState);
			((Heightmap)this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES)).update(k, i, m, blockState);
			((Heightmap)this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR)).update(k, i, m, blockState);
			((Heightmap)this.heightmaps.get(Heightmap.Types.WORLD_SURFACE)).update(k, i, m, blockState);
			boolean bl3 = levelChunkSection.isEmpty();
			if (bl2 != bl3) {
				this.level.getChunkSource().getLightEngine().updateSectionStatus(blockPos, bl3);
			}

			boolean bl4 = blockState2.hasBlockEntity();
			if (!this.level.isClientSide) {
				blockState2.onRemove(this.level, blockPos, blockState, bl);
			} else if (!blockState2.is(block) && bl4) {
				this.removeBlockEntity(blockPos);
			}

			if (!levelChunkSection.getBlockState(k, l, m).is(block)) {
				return null;
			} else {
				if (!this.level.isClientSide) {
					blockState.onPlace(this.level, blockPos, blockState2, bl);
				}

				if (blockState.hasBlockEntity()) {
					BlockEntity blockEntity = this.getBlockEntity(blockPos, LevelChunk.EntityCreationType.CHECK);
					if (blockEntity == null) {
						blockEntity = ((EntityBlock)block).newBlockEntity(blockPos, blockState);
						if (blockEntity != null) {
							this.addAndRegisterBlockEntity(blockEntity);
						}
					} else {
						blockEntity.setBlockState(blockState);
						this.updateBlockEntityTicker(blockEntity);
					}
				}

				this.unsaved = true;
				return blockState2;
			}
		}
	}

	@Deprecated
	@Override
	public void addEntity(Entity entity) {
	}

	@Override
	public void setHeightmap(Heightmap.Types types, long[] ls) {
		((Heightmap)this.heightmaps.get(types)).setRawData(ls);
	}

	@Override
	public int getHeight(Heightmap.Types types, int i, int j) {
		return ((Heightmap)this.heightmaps.get(types)).getFirstAvailable(i & 15, j & 15) - 1;
	}

	@Override
	public BlockPos getHeighestPosition(Heightmap.Types types) {
		ChunkPos chunkPos = this.getPos();
		int i = this.getMinBuildHeight();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int j = chunkPos.getMinBlockX(); j <= chunkPos.getMaxBlockX(); j++) {
			for (int k = chunkPos.getMinBlockZ(); k <= chunkPos.getMaxBlockZ(); k++) {
				int l = this.getHeight(types, j & 15, k & 15);
				if (l > i) {
					i = l;
					mutableBlockPos.set(j, l, k);
				}
			}
		}

		return mutableBlockPos.immutable();
	}

	@Nullable
	private BlockEntity createBlockEntity(BlockPos blockPos) {
		BlockState blockState = this.getBlockState(blockPos);
		return !blockState.hasBlockEntity() ? null : ((EntityBlock)blockState.getBlock()).newBlockEntity(blockPos, blockState);
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
				if (blockEntity != null) {
					this.addAndRegisterBlockEntity(blockEntity);
				}
			}
		} else if (blockEntity.isRemoved()) {
			this.blockEntities.remove(blockPos);
			return null;
		}

		return blockEntity;
	}

	public void addAndRegisterBlockEntity(BlockEntity blockEntity) {
		this.setBlockEntity(blockEntity);
		if (this.isInLevel()) {
			this.addGameEventListener(blockEntity);
			this.updateBlockEntityTicker(blockEntity);
		}
	}

	private boolean isInLevel() {
		return this.loaded || this.level.isClientSide();
	}

	boolean isTicking(BlockPos blockPos) {
		return (this.level.isClientSide() || this.getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING))
			&& this.level.getWorldBorder().isWithinBounds(blockPos);
	}

	@Override
	public void setBlockEntity(BlockEntity blockEntity) {
		BlockPos blockPos = blockEntity.getBlockPos();
		if (this.getBlockState(blockPos).hasBlockEntity()) {
			blockEntity.setLevel(this.level);
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
		if (this.isInLevel()) {
			BlockEntity blockEntity = (BlockEntity)this.blockEntities.remove(blockPos);
			if (blockEntity != null) {
				this.removeGameEventListener(blockEntity);
				blockEntity.setRemoved();
			}
		}

		this.removeBlockEntityTicker(blockPos);
	}

	private <T extends BlockEntity> void removeGameEventListener(T blockEntity) {
		if (!this.level.isClientSide) {
			Block block = blockEntity.getBlockState().getBlock();
			if (block instanceof EntityBlock) {
				GameEventListener gameEventListener = ((EntityBlock)block).getListener(this.level, blockEntity);
				if (gameEventListener != null) {
					int i = SectionPos.blockToSectionCoord(blockEntity.getBlockPos().getY());
					GameEventDispatcher gameEventDispatcher = this.getEventDispatcher(i);
					gameEventDispatcher.unregister(gameEventListener);
					if (gameEventDispatcher.isEmpty()) {
						this.gameEventDispatcherSections.remove(i);
					}
				}
			}
		}
	}

	private void removeBlockEntityTicker(BlockPos blockPos) {
		LevelChunk.RebindableTickingBlockEntityWrapper rebindableTickingBlockEntityWrapper = (LevelChunk.RebindableTickingBlockEntityWrapper)this.tickersInLevel
			.remove(blockPos);
		if (rebindableTickingBlockEntityWrapper != null) {
			rebindableTickingBlockEntityWrapper.rebind(NULL_TICKER);
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

	public boolean isEmpty() {
		return false;
	}

	@Override
	public ChunkPos getPos() {
		return this.chunkPos;
	}

	public void replaceWithPacketData(@Nullable ChunkBiomeContainer chunkBiomeContainer, FriendlyByteBuf friendlyByteBuf, CompoundTag compoundTag, BitSet bitSet) {
		boolean bl = chunkBiomeContainer != null;
		if (bl) {
			this.blockEntities.values().forEach(this::onBlockEntityRemove);
			this.blockEntities.clear();
		} else {
			this.blockEntities.values().removeIf(blockEntity -> {
				int ix = this.getSectionIndex(blockEntity.getBlockPos().getY());
				if (bitSet.get(ix)) {
					blockEntity.setRemoved();
					return true;
				} else {
					return false;
				}
			});
		}

		for (int i = 0; i < this.sections.length; i++) {
			LevelChunkSection levelChunkSection = this.sections[i];
			if (!bitSet.get(i)) {
				if (bl && levelChunkSection != EMPTY_SECTION) {
					this.sections[i] = EMPTY_SECTION;
				}
			} else {
				if (levelChunkSection == EMPTY_SECTION) {
					levelChunkSection = new LevelChunkSection(this.getSectionYFromSectionIndex(i));
					this.sections[i] = levelChunkSection;
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
	}

	private void onBlockEntityRemove(BlockEntity blockEntity) {
		blockEntity.setRemoved();
		this.tickersInLevel.remove(blockEntity.getBlockPos());
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

	@Override
	public CompoundTag getBlockEntityNbt(BlockPos blockPos) {
		return (CompoundTag)this.pendingBlockEntities.get(blockPos);
	}

	@Override
	public Stream<BlockPos> getLights() {
		return StreamSupport.stream(
				BlockPos.betweenClosed(
						this.chunkPos.getMinBlockX(),
						this.getMinBuildHeight(),
						this.chunkPos.getMinBlockZ(),
						this.chunkPos.getMaxBlockX(),
						this.getMaxBuildHeight() - 1,
						this.chunkPos.getMaxBlockZ()
					)
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
		return this.unsaved;
	}

	@Nullable
	@Override
	public StructureStart<?> getStartForFeature(StructureFeature<?> structureFeature) {
		return (StructureStart<?>)this.structureStarts.get(structureFeature);
	}

	@Override
	public void setStartForFeature(StructureFeature<?> structureFeature, StructureStart<?> structureStart) {
		this.structureStarts.put(structureFeature, structureStart);
	}

	@Override
	public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
		return this.structureStarts;
	}

	@Override
	public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> map) {
		this.structureStarts.clear();
		this.structureStarts.putAll(map);
	}

	@Override
	public LongSet getReferencesForFeature(StructureFeature<?> structureFeature) {
		return (LongSet)this.structuresRefences.computeIfAbsent(structureFeature, structureFeaturex -> new LongOpenHashSet());
	}

	@Override
	public void addReferenceForFeature(StructureFeature<?> structureFeature, long l) {
		((LongSet)this.structuresRefences.computeIfAbsent(structureFeature, structureFeaturex -> new LongOpenHashSet())).add(l);
	}

	@Override
	public Map<StructureFeature<?>, LongSet> getAllReferences() {
		return this.structuresRefences;
	}

	@Override
	public void setAllReferences(Map<StructureFeature<?>, LongSet> map) {
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
					BlockPos blockPos = ProtoChunk.unpackOffsetCoordinates(short_, this.getSectionYFromSectionIndex(i), chunkPos);
					BlockState blockState = this.getBlockState(blockPos);
					BlockState blockState2 = Block.updateFromNeighbourShapes(blockState, this.level, blockPos);
					this.level.setBlock(blockPos, blockState2, 20);
				}

				this.postProcessing[i].clear();
			}
		}

		this.unpackTicks();

		for (BlockPos blockPos2 : ImmutableList.copyOf(this.pendingBlockEntities.keySet())) {
			this.getBlockEntity(blockPos2);
		}

		this.pendingBlockEntities.clear();
		this.upgradeData.upgrade(this);
	}

	@Nullable
	private BlockEntity promotePendingBlockEntity(BlockPos blockPos, CompoundTag compoundTag) {
		BlockState blockState = this.getBlockState(blockPos);
		BlockEntity blockEntity;
		if ("DUMMY".equals(compoundTag.getString("id"))) {
			if (blockState.hasBlockEntity()) {
				blockEntity = ((EntityBlock)blockState.getBlock()).newBlockEntity(blockPos, blockState);
			} else {
				blockEntity = null;
				LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", blockPos, blockState);
			}
		} else {
			blockEntity = BlockEntity.loadStatic(blockPos, blockState, compoundTag);
		}

		if (blockEntity != null) {
			blockEntity.setLevel(this.level);
			this.addAndRegisterBlockEntity(blockEntity);
		} else {
			LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", blockState, blockPos);
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
			((ChunkTickList)this.blockTicks).copyOut(this.level.getBlockTicks());
			this.blockTicks = EmptyTickList.empty();
		}

		if (this.liquidTicks instanceof ProtoTickList) {
			((ProtoTickList)this.liquidTicks).copyOut(this.level.getLiquidTicks(), blockPos -> this.getFluidState(blockPos).getType());
			this.liquidTicks = EmptyTickList.empty();
		} else if (this.liquidTicks instanceof ChunkTickList) {
			((ChunkTickList)this.liquidTicks).copyOut(this.level.getLiquidTicks());
			this.liquidTicks = EmptyTickList.empty();
		}
	}

	public void packTicks(ServerLevel serverLevel) {
		if (this.blockTicks == EmptyTickList.empty()) {
			this.blockTicks = new ChunkTickList<>(
				Registry.BLOCK::getKey, serverLevel.getBlockTicks().fetchTicksInChunk(this.chunkPos, true, false), serverLevel.getGameTime()
			);
			this.setUnsaved(true);
		}

		if (this.liquidTicks == EmptyTickList.empty()) {
			this.liquidTicks = new ChunkTickList<>(
				Registry.FLUID::getKey, serverLevel.getLiquidTicks().fetchTicksInChunk(this.chunkPos, true, false), serverLevel.getGameTime()
			);
			this.setUnsaved(true);
		}
	}

	@Override
	public int getMinBuildHeight() {
		return this.level.getMinBuildHeight();
	}

	@Override
	public int getHeight() {
		return this.level.getHeight();
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

	public void invalidateAllBlockEntities() {
		this.blockEntities.values().forEach(this::onBlockEntityRemove);
	}

	public void registerAllBlockEntitiesAfterLevelLoad() {
		this.blockEntities.values().forEach(blockEntity -> {
			this.addGameEventListener(blockEntity);
			this.updateBlockEntityTicker(blockEntity);
		});
	}

	private <T extends BlockEntity> void addGameEventListener(T blockEntity) {
		if (!this.level.isClientSide) {
			Block block = blockEntity.getBlockState().getBlock();
			if (block instanceof EntityBlock) {
				GameEventListener gameEventListener = ((EntityBlock)block).getListener(this.level, blockEntity);
				if (gameEventListener != null) {
					GameEventDispatcher gameEventDispatcher = this.getEventDispatcher(SectionPos.blockToSectionCoord(blockEntity.getBlockPos().getY()));
					gameEventDispatcher.register(gameEventListener);
				}
			}
		}
	}

	private <T extends BlockEntity> void updateBlockEntityTicker(T blockEntity) {
		BlockState blockState = blockEntity.getBlockState();
		BlockEntityTicker<T> blockEntityTicker = blockState.getTicker(this.level, (BlockEntityType<T>)blockEntity.getType());
		if (blockEntityTicker == null) {
			this.removeBlockEntityTicker(blockEntity.getBlockPos());
		} else {
			this.tickersInLevel
				.compute(
					blockEntity.getBlockPos(),
					(blockPos, rebindableTickingBlockEntityWrapper) -> {
						TickingBlockEntity tickingBlockEntity = this.createTicker(blockEntity, blockEntityTicker);
						if (rebindableTickingBlockEntityWrapper != null) {
							rebindableTickingBlockEntityWrapper.rebind(tickingBlockEntity);
							return rebindableTickingBlockEntityWrapper;
						} else if (this.isInLevel()) {
							LevelChunk.RebindableTickingBlockEntityWrapper rebindableTickingBlockEntityWrapper2 = new LevelChunk.RebindableTickingBlockEntityWrapper(
								tickingBlockEntity
							);
							this.level.addBlockEntityTicker(rebindableTickingBlockEntityWrapper2);
							return rebindableTickingBlockEntityWrapper2;
						} else {
							return null;
						}
					}
				);
		}
	}

	private <T extends BlockEntity> TickingBlockEntity createTicker(T blockEntity, BlockEntityTicker<T> blockEntityTicker) {
		return new LevelChunk.BoundTickingBlockEntity<>(blockEntity, blockEntityTicker);
	}

	class BoundTickingBlockEntity<T extends BlockEntity> implements TickingBlockEntity {
		private final T blockEntity;
		private final BlockEntityTicker<T> ticker;
		private boolean loggedInvalidBlockState;

		BoundTickingBlockEntity(T blockEntity, BlockEntityTicker<T> blockEntityTicker) {
			this.blockEntity = blockEntity;
			this.ticker = blockEntityTicker;
		}

		@Override
		public void tick() {
			if (!this.blockEntity.isRemoved() && this.blockEntity.hasLevel()) {
				BlockPos blockPos = this.blockEntity.getBlockPos();
				if (LevelChunk.this.isTicking(blockPos)) {
					try {
						ProfilerFiller profilerFiller = LevelChunk.this.level.getProfiler();
						profilerFiller.push(this::getType);
						BlockState blockState = LevelChunk.this.getBlockState(blockPos);
						if (this.blockEntity.getType().isValid(blockState)) {
							this.ticker.tick(LevelChunk.this.level, this.blockEntity.getBlockPos(), blockState, this.blockEntity);
							this.loggedInvalidBlockState = false;
						} else if (!this.loggedInvalidBlockState) {
							this.loggedInvalidBlockState = true;
							LevelChunk.LOGGER.warn("Block entity {} @ {} state {} invalid for ticking:", this::getType, this::getPos, () -> blockState);
						}

						profilerFiller.pop();
					} catch (Throwable var5) {
						CrashReport crashReport = CrashReport.forThrowable(var5, "Ticking block entity");
						CrashReportCategory crashReportCategory = crashReport.addCategory("Block entity being ticked");
						this.blockEntity.fillCrashReportCategory(crashReportCategory);
						throw new ReportedException(crashReport);
					}
				}
			}
		}

		@Override
		public boolean isRemoved() {
			return this.blockEntity.isRemoved();
		}

		@Override
		public BlockPos getPos() {
			return this.blockEntity.getBlockPos();
		}

		@Override
		public String getType() {
			return BlockEntityType.getKey(this.blockEntity.getType()).toString();
		}

		public String toString() {
			return "Level ticker for " + this.getType() + "@" + this.getPos();
		}
	}

	public static enum EntityCreationType {
		IMMEDIATE,
		QUEUED,
		CHECK;
	}

	class RebindableTickingBlockEntityWrapper implements TickingBlockEntity {
		private TickingBlockEntity ticker;

		RebindableTickingBlockEntityWrapper(TickingBlockEntity tickingBlockEntity) {
			this.ticker = tickingBlockEntity;
		}

		void rebind(TickingBlockEntity tickingBlockEntity) {
			this.ticker = tickingBlockEntity;
		}

		@Override
		public void tick() {
			this.ticker.tick();
		}

		@Override
		public boolean isRemoved() {
			return this.ticker.isRemoved();
		}

		@Override
		public BlockPos getPos() {
			return this.ticker.getPos();
		}

		@Override
		public String getType() {
			return this.ticker.getType();
		}

		public String toString() {
			return this.ticker.toString() + " <wrapped>";
		}
	}
}
