package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
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
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
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
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;
import org.slf4j.Logger;

public class LevelChunk extends ChunkAccess {
	static final Logger LOGGER = LogUtils.getLogger();
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
	private final Map<BlockPos, LevelChunk.RebindableTickingBlockEntityWrapper> tickersInLevel = Maps.<BlockPos, LevelChunk.RebindableTickingBlockEntityWrapper>newHashMap();
	private boolean loaded;
	private boolean clientLightReady = false;
	final Level level;
	@Nullable
	private Supplier<ChunkHolder.FullChunkStatus> fullStatus;
	@Nullable
	private LevelChunk.PostLoadProcessor postLoad;
	private final Int2ObjectMap<GameEventDispatcher> gameEventDispatcherSections;
	private final LevelChunkTicks<Block> blockTicks;
	private final LevelChunkTicks<Fluid> fluidTicks;

	public LevelChunk(Level level, ChunkPos chunkPos) {
		this(level, chunkPos, UpgradeData.EMPTY, new LevelChunkTicks<>(), new LevelChunkTicks<>(), 0L, null, null, null);
	}

	public LevelChunk(
		Level level,
		ChunkPos chunkPos,
		UpgradeData upgradeData,
		LevelChunkTicks<Block> levelChunkTicks,
		LevelChunkTicks<Fluid> levelChunkTicks2,
		long l,
		@Nullable LevelChunkSection[] levelChunkSections,
		@Nullable LevelChunk.PostLoadProcessor postLoadProcessor,
		@Nullable BlendingData blendingData
	) {
		super(chunkPos, upgradeData, level, level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), l, levelChunkSections, blendingData);
		this.level = level;
		this.gameEventDispatcherSections = new Int2ObjectOpenHashMap<>();

		for (Heightmap.Types types : Heightmap.Types.values()) {
			if (ChunkStatus.FULL.heightmapsAfter().contains(types)) {
				this.heightmaps.put(types, new Heightmap(this, types));
			}
		}

		this.postLoad = postLoadProcessor;
		this.blockTicks = levelChunkTicks;
		this.fluidTicks = levelChunkTicks2;
	}

	public LevelChunk(ServerLevel serverLevel, ProtoChunk protoChunk, @Nullable LevelChunk.PostLoadProcessor postLoadProcessor) {
		this(
			serverLevel,
			protoChunk.getPos(),
			protoChunk.getUpgradeData(),
			protoChunk.unpackBlockTicks(),
			protoChunk.unpackFluidTicks(),
			protoChunk.getInhabitedTime(),
			protoChunk.getSections(),
			postLoadProcessor,
			protoChunk.getBlendingData()
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
				this.setHeightmap((Heightmap.Types)entry.getKey(), ((Heightmap)entry.getValue()).getRawData());
			}
		}

		this.setLightCorrect(protoChunk.isLightCorrect());
		this.unsaved = true;
	}

	@Override
	public TickContainerAccess<Block> getBlockTicks() {
		return this.blockTicks;
	}

	@Override
	public TickContainerAccess<Fluid> getFluidTicks() {
		return this.fluidTicks;
	}

	@Override
	public ChunkAccess.TicksToSave getTicksForSerialization() {
		return new ChunkAccess.TicksToSave(this.blockTicks, this.fluidTicks);
	}

	@Override
	public GameEventDispatcher getEventDispatcher(int i) {
		return this.level instanceof ServerLevel serverLevel
			? this.gameEventDispatcherSections
				.computeIfAbsent(i, (Int2ObjectFunction<? extends GameEventDispatcher>)(ix -> new EuclideanGameEventDispatcher(serverLevel)))
			: super.getEventDispatcher(i);
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
					if (!levelChunkSection.hasOnlyAir()) {
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
				if (!levelChunkSection.hasOnlyAir()) {
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
		LevelChunkSection levelChunkSection = this.getSection(this.getSectionIndex(i));
		boolean bl2 = levelChunkSection.hasOnlyAir();
		if (bl2 && blockState.isAir()) {
			return null;
		} else {
			int j = blockPos.getX() & 15;
			int k = i & 15;
			int l = blockPos.getZ() & 15;
			BlockState blockState2 = levelChunkSection.setBlockState(j, k, l, blockState);
			if (blockState2 == blockState) {
				return null;
			} else {
				Block block = blockState.getBlock();
				((Heightmap)this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING)).update(j, i, l, blockState);
				((Heightmap)this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES)).update(j, i, l, blockState);
				((Heightmap)this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR)).update(j, i, l, blockState);
				((Heightmap)this.heightmaps.get(Heightmap.Types.WORLD_SURFACE)).update(j, i, l, blockState);
				boolean bl3 = levelChunkSection.hasOnlyAir();
				if (bl2 != bl3) {
					this.level.getChunkSource().getLightEngine().updateSectionStatus(blockPos, bl3);
				}

				boolean bl4 = blockState2.hasBlockEntity();
				if (!this.level.isClientSide) {
					blockState2.onRemove(this.level, blockPos, blockState, bl);
				} else if (!blockState2.is(block) && bl4) {
					this.removeBlockEntity(blockPos);
				}

				if (!levelChunkSection.getBlockState(j, k, l).is(block)) {
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
	}

	@Deprecated
	@Override
	public void addEntity(Entity entity) {
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
			if (this.level instanceof ServerLevel serverLevel) {
				this.addGameEventListener(blockEntity, serverLevel);
			}

			this.updateBlockEntityTicker(blockEntity);
		}
	}

	private boolean isInLevel() {
		return this.loaded || this.level.isClientSide();
	}

	boolean isTicking(BlockPos blockPos) {
		if (!this.level.getWorldBorder().isWithinBounds(blockPos)) {
			return false;
		} else {
			return !(this.level instanceof ServerLevel serverLevel)
				? true
				: this.getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING) && serverLevel.areEntitiesLoaded(ChunkPos.asLong(blockPos));
		}
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

	@Nullable
	@Override
	public CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos) {
		BlockEntity blockEntity = this.getBlockEntity(blockPos);
		if (blockEntity != null && !blockEntity.isRemoved()) {
			CompoundTag compoundTag = blockEntity.saveWithFullMetadata();
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
				if (this.level instanceof ServerLevel serverLevel) {
					this.removeGameEventListener(blockEntity, serverLevel);
				}

				blockEntity.setRemoved();
			}
		}

		this.removeBlockEntityTicker(blockPos);
	}

	private <T extends BlockEntity> void removeGameEventListener(T blockEntity, ServerLevel serverLevel) {
		Block block = blockEntity.getBlockState().getBlock();
		if (block instanceof EntityBlock) {
			GameEventListener gameEventListener = ((EntityBlock)block).getListener(serverLevel, blockEntity);
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

	private void removeBlockEntityTicker(BlockPos blockPos) {
		LevelChunk.RebindableTickingBlockEntityWrapper rebindableTickingBlockEntityWrapper = (LevelChunk.RebindableTickingBlockEntityWrapper)this.tickersInLevel
			.remove(blockPos);
		if (rebindableTickingBlockEntityWrapper != null) {
			rebindableTickingBlockEntityWrapper.rebind(NULL_TICKER);
		}
	}

	public void runPostLoad() {
		if (this.postLoad != null) {
			this.postLoad.run(this);
			this.postLoad = null;
		}
	}

	public boolean isEmpty() {
		return false;
	}

	public void replaceWithPacketData(
		FriendlyByteBuf friendlyByteBuf, CompoundTag compoundTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer
	) {
		this.clearAllBlockEntities();

		for (LevelChunkSection levelChunkSection : this.sections) {
			levelChunkSection.read(friendlyByteBuf);
		}

		for (Heightmap.Types types : Heightmap.Types.values()) {
			String string = types.getSerializationKey();
			if (compoundTag.contains(string, 12)) {
				this.setHeightmap(types, compoundTag.getLongArray(string));
			}
		}

		consumer.accept((ClientboundLevelChunkPacketData.BlockEntityTagOutput)(blockPos, blockEntityType, compoundTagx) -> {
			BlockEntity blockEntity = this.getBlockEntity(blockPos, LevelChunk.EntityCreationType.IMMEDIATE);
			if (blockEntity != null && compoundTagx != null && blockEntity.getType() == blockEntityType) {
				blockEntity.load(compoundTagx);
			}
		});
	}

	public void setLoaded(boolean bl) {
		this.loaded = bl;
	}

	public Level getLevel() {
		return this.level;
	}

	public Map<BlockPos, BlockEntity> getBlockEntities() {
		return this.blockEntities;
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

	public void postProcessGeneration() {
		ChunkPos chunkPos = this.getPos();

		for (int i = 0; i < this.postProcessing.length; i++) {
			if (this.postProcessing[i] != null) {
				for (Short short_ : this.postProcessing[i]) {
					BlockPos blockPos = ProtoChunk.unpackOffsetCoordinates(short_, this.getSectionYFromSectionIndex(i), chunkPos);
					BlockState blockState = this.getBlockState(blockPos);
					FluidState fluidState = blockState.getFluidState();
					if (!fluidState.isEmpty()) {
						fluidState.tick(this.level, blockPos);
					}

					if (!(blockState.getBlock() instanceof LiquidBlock)) {
						BlockState blockState2 = Block.updateFromNeighbourShapes(blockState, this.level, blockPos);
						this.level.setBlock(blockPos, blockState2, 20);
					}
				}

				this.postProcessing[i].clear();
			}
		}

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

	public void unpackTicks(long l) {
		this.blockTicks.unpack(l);
		this.fluidTicks.unpack(l);
	}

	public void registerTickContainerInLevel(ServerLevel serverLevel) {
		serverLevel.getBlockTicks().addContainer(this.chunkPos, this.blockTicks);
		serverLevel.getFluidTicks().addContainer(this.chunkPos, this.fluidTicks);
	}

	public void unregisterTickContainerFromLevel(ServerLevel serverLevel) {
		serverLevel.getBlockTicks().removeContainer(this.chunkPos);
		serverLevel.getFluidTicks().removeContainer(this.chunkPos);
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

	public void clearAllBlockEntities() {
		this.blockEntities.values().forEach(BlockEntity::setRemoved);
		this.blockEntities.clear();
		this.tickersInLevel.values().forEach(rebindableTickingBlockEntityWrapper -> rebindableTickingBlockEntityWrapper.rebind(NULL_TICKER));
		this.tickersInLevel.clear();
	}

	public void registerAllBlockEntitiesAfterLevelLoad() {
		this.blockEntities.values().forEach(blockEntity -> {
			if (this.level instanceof ServerLevel serverLevel) {
				this.addGameEventListener(blockEntity, serverLevel);
			}

			this.updateBlockEntityTicker(blockEntity);
		});
	}

	private <T extends BlockEntity> void addGameEventListener(T blockEntity, ServerLevel serverLevel) {
		Block block = blockEntity.getBlockState().getBlock();
		if (block instanceof EntityBlock) {
			GameEventListener gameEventListener = ((EntityBlock)block).getListener(serverLevel, blockEntity);
			if (gameEventListener != null) {
				GameEventDispatcher gameEventDispatcher = this.getEventDispatcher(SectionPos.blockToSectionCoord(blockEntity.getBlockPos().getY()));
				gameEventDispatcher.register(gameEventListener);
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

	public boolean isClientLightReady() {
		return this.clientLightReady;
	}

	public void setClientLightReady(boolean bl) {
		this.clientLightReady = bl;
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
							LevelChunk.LOGGER.warn("Block entity {} @ {} state {} invalid for ticking:", LogUtils.defer(this::getType), LogUtils.defer(this::getPos), blockState);
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

	@FunctionalInterface
	public interface PostLoadProcessor {
		void run(LevelChunk levelChunk);
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
