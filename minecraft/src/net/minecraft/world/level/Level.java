package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Scoreboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Level implements LevelAccessor, AutoCloseable {
	protected static final Logger LOGGER = LogManager.getLogger();
	public static final Codec<ResourceKey<Level>> RESOURCE_KEY_CODEC = ResourceLocation.CODEC
		.xmap(ResourceKey.elementKey(Registry.DIMENSION_REGISTRY), ResourceKey::location);
	public static final ResourceKey<Level> OVERWORLD = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("overworld"));
	public static final ResourceKey<Level> NETHER = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("the_nether"));
	public static final ResourceKey<Level> END = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("the_end"));
	private static final Direction[] DIRECTIONS = Direction.values();
	public final List<BlockEntity> blockEntityList = Lists.<BlockEntity>newArrayList();
	public final List<BlockEntity> tickableBlockEntities = Lists.<BlockEntity>newArrayList();
	protected final List<BlockEntity> pendingBlockEntities = Lists.<BlockEntity>newArrayList();
	protected final List<BlockEntity> blockEntitiesToUnload = Lists.<BlockEntity>newArrayList();
	private final Thread thread;
	private final boolean isDebug;
	private int skyDarken;
	protected int randValue = new Random().nextInt();
	protected final int addend = 1013904223;
	protected float oRainLevel;
	protected float rainLevel;
	protected float oThunderLevel;
	protected float thunderLevel;
	public final Random random = new Random();
	private final DimensionType dimensionType;
	protected final WritableLevelData levelData;
	private final Supplier<ProfilerFiller> profiler;
	public final boolean isClientSide;
	protected boolean updatingBlockEntities;
	private final WorldBorder worldBorder;
	private final BiomeManager biomeManager;
	private final ResourceKey<Level> dimension;

	protected Level(
		WritableLevelData writableLevelData,
		ResourceKey<Level> resourceKey,
		DimensionType dimensionType,
		Supplier<ProfilerFiller> supplier,
		boolean bl,
		boolean bl2,
		long l
	) {
		this.profiler = supplier;
		this.levelData = writableLevelData;
		this.dimensionType = dimensionType;
		this.dimension = resourceKey;
		this.isClientSide = bl;
		if (dimensionType.coordinateScale() != 1.0) {
			this.worldBorder = new WorldBorder() {
				@Override
				public double getCenterX() {
					return super.getCenterX() / dimensionType.coordinateScale();
				}

				@Override
				public double getCenterZ() {
					return super.getCenterZ() / dimensionType.coordinateScale();
				}
			};
		} else {
			this.worldBorder = new WorldBorder();
		}

		this.thread = Thread.currentThread();
		this.biomeManager = new BiomeManager(this, l, dimensionType.getBiomeZoomer());
		this.isDebug = bl2;
	}

	@Override
	public boolean isClientSide() {
		return this.isClientSide;
	}

	@Nullable
	public MinecraftServer getServer() {
		return null;
	}

	public static boolean isInWorldBounds(BlockPos blockPos) {
		return !isOutsideBuildHeight(blockPos) && isInWorldBoundsHorizontal(blockPos);
	}

	public static boolean isInSpawnableBounds(BlockPos blockPos) {
		return !isOutsideSpawnableHeight(blockPos.getY()) && isInWorldBoundsHorizontal(blockPos);
	}

	private static boolean isInWorldBoundsHorizontal(BlockPos blockPos) {
		return blockPos.getX() >= -30000000 && blockPos.getZ() >= -30000000 && blockPos.getX() < 30000000 && blockPos.getZ() < 30000000;
	}

	private static boolean isOutsideSpawnableHeight(int i) {
		return i < -20000000 || i >= 20000000;
	}

	public static boolean isOutsideBuildHeight(BlockPos blockPos) {
		return isOutsideBuildHeight(blockPos.getY());
	}

	public static boolean isOutsideBuildHeight(int i) {
		return i < 0 || i >= 256;
	}

	public LevelChunk getChunkAt(BlockPos blockPos) {
		return this.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
	}

	public LevelChunk getChunk(int i, int j) {
		return (LevelChunk)this.getChunk(i, j, ChunkStatus.FULL);
	}

	@Override
	public ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl) {
		ChunkAccess chunkAccess = this.getChunkSource().getChunk(i, j, chunkStatus, bl);
		if (chunkAccess == null && bl) {
			throw new IllegalStateException("Should always be able to create a chunk!");
		} else {
			return chunkAccess;
		}
	}

	@Override
	public boolean setBlock(BlockPos blockPos, BlockState blockState, int i) {
		return this.setBlock(blockPos, blockState, i, 512);
	}

	@Override
	public boolean setBlock(BlockPos blockPos, BlockState blockState, int i, int j) {
		if (isOutsideBuildHeight(blockPos)) {
			return false;
		} else if (!this.isClientSide && this.isDebug()) {
			return false;
		} else {
			LevelChunk levelChunk = this.getChunkAt(blockPos);
			Block block = blockState.getBlock();
			BlockState blockState2 = levelChunk.setBlockState(blockPos, blockState, (i & 64) != 0);
			if (blockState2 == null) {
				return false;
			} else {
				BlockState blockState3 = this.getBlockState(blockPos);
				if ((i & 128) == 0
					&& blockState3 != blockState2
					&& (
						blockState3.getLightBlock(this, blockPos) != blockState2.getLightBlock(this, blockPos)
							|| blockState3.getLightEmission() != blockState2.getLightEmission()
							|| blockState3.useShapeForLightOcclusion()
							|| blockState2.useShapeForLightOcclusion()
					)) {
					this.getProfiler().push("queueCheckLight");
					this.getChunkSource().getLightEngine().checkBlock(blockPos);
					this.getProfiler().pop();
				}

				if (blockState3 == blockState) {
					if (blockState2 != blockState3) {
						this.setBlocksDirty(blockPos, blockState2, blockState3);
					}

					if ((i & 2) != 0
						&& (!this.isClientSide || (i & 4) == 0)
						&& (this.isClientSide || levelChunk.getFullStatus() != null && levelChunk.getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING))) {
						this.sendBlockUpdated(blockPos, blockState2, blockState, i);
					}

					if ((i & 1) != 0) {
						this.blockUpdated(blockPos, blockState2.getBlock());
						if (!this.isClientSide && blockState.hasAnalogOutputSignal()) {
							this.updateNeighbourForOutputSignal(blockPos, block);
						}
					}

					if ((i & 16) == 0 && j > 0) {
						int k = i & -34;
						blockState2.updateIndirectNeighbourShapes(this, blockPos, k, j - 1);
						blockState.updateNeighbourShapes(this, blockPos, k, j - 1);
						blockState.updateIndirectNeighbourShapes(this, blockPos, k, j - 1);
					}

					this.onBlockStateChange(blockPos, blockState2, blockState3);
				}

				return true;
			}
		}
	}

	public void onBlockStateChange(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
	}

	@Override
	public boolean removeBlock(BlockPos blockPos, boolean bl) {
		FluidState fluidState = this.getFluidState(blockPos);
		return this.setBlock(blockPos, fluidState.createLegacyBlock(), 3 | (bl ? 64 : 0));
	}

	@Override
	public boolean destroyBlock(BlockPos blockPos, boolean bl, @Nullable Entity entity, int i) {
		BlockState blockState = this.getBlockState(blockPos);
		if (blockState.isAir()) {
			return false;
		} else {
			FluidState fluidState = this.getFluidState(blockPos);
			if (!(blockState.getBlock() instanceof BaseFireBlock)) {
				this.levelEvent(2001, blockPos, Block.getId(blockState));
			}

			if (bl) {
				BlockEntity blockEntity = blockState.getBlock().isEntityBlock() ? this.getBlockEntity(blockPos) : null;
				Block.dropResources(blockState, this, blockPos, blockEntity, entity, ItemStack.EMPTY);
			}

			return this.setBlock(blockPos, fluidState.createLegacyBlock(), 3, i);
		}
	}

	public boolean setBlockAndUpdate(BlockPos blockPos, BlockState blockState) {
		return this.setBlock(blockPos, blockState, 3);
	}

	public abstract void sendBlockUpdated(BlockPos blockPos, BlockState blockState, BlockState blockState2, int i);

	public void setBlocksDirty(BlockPos blockPos, BlockState blockState, BlockState blockState2) {
	}

	public void updateNeighborsAt(BlockPos blockPos, Block block) {
		this.neighborChanged(blockPos.west(), block, blockPos);
		this.neighborChanged(blockPos.east(), block, blockPos);
		this.neighborChanged(blockPos.below(), block, blockPos);
		this.neighborChanged(blockPos.above(), block, blockPos);
		this.neighborChanged(blockPos.north(), block, blockPos);
		this.neighborChanged(blockPos.south(), block, blockPos);
	}

	public void updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, Direction direction) {
		if (direction != Direction.WEST) {
			this.neighborChanged(blockPos.west(), block, blockPos);
		}

		if (direction != Direction.EAST) {
			this.neighborChanged(blockPos.east(), block, blockPos);
		}

		if (direction != Direction.DOWN) {
			this.neighborChanged(blockPos.below(), block, blockPos);
		}

		if (direction != Direction.UP) {
			this.neighborChanged(blockPos.above(), block, blockPos);
		}

		if (direction != Direction.NORTH) {
			this.neighborChanged(blockPos.north(), block, blockPos);
		}

		if (direction != Direction.SOUTH) {
			this.neighborChanged(blockPos.south(), block, blockPos);
		}
	}

	public void neighborChanged(BlockPos blockPos, Block block, BlockPos blockPos2) {
		if (!this.isClientSide) {
			BlockState blockState = this.getBlockState(blockPos);

			try {
				blockState.neighborChanged(this, blockPos, block, blockPos2, false);
			} catch (Throwable var8) {
				CrashReport crashReport = CrashReport.forThrowable(var8, "Exception while updating neighbours");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Block being updated");
				crashReportCategory.setDetail("Source block type", (CrashReportDetail<String>)(() -> {
					try {
						return String.format("ID #%s (%s // %s)", Registry.BLOCK.getKey(block), block.getDescriptionId(), block.getClass().getCanonicalName());
					} catch (Throwable var2) {
						return "ID #" + Registry.BLOCK.getKey(block);
					}
				}));
				CrashReportCategory.populateBlockDetails(crashReportCategory, blockPos, blockState);
				throw new ReportedException(crashReport);
			}
		}
	}

	@Override
	public int getHeight(Heightmap.Types types, int i, int j) {
		int k;
		if (i >= -30000000 && j >= -30000000 && i < 30000000 && j < 30000000) {
			if (this.hasChunk(i >> 4, j >> 4)) {
				k = this.getChunk(i >> 4, j >> 4).getHeight(types, i & 15, j & 15) + 1;
			} else {
				k = 0;
			}
		} else {
			k = this.getSeaLevel() + 1;
		}

		return k;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return this.getChunkSource().getLightEngine();
	}

	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		if (isOutsideBuildHeight(blockPos)) {
			return Blocks.VOID_AIR.defaultBlockState();
		} else {
			LevelChunk levelChunk = this.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
			return levelChunk.getBlockState(blockPos);
		}
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		if (isOutsideBuildHeight(blockPos)) {
			return Fluids.EMPTY.defaultFluidState();
		} else {
			LevelChunk levelChunk = this.getChunkAt(blockPos);
			return levelChunk.getFluidState(blockPos);
		}
	}

	public boolean isDay() {
		return !this.dimensionType().hasFixedTime() && this.skyDarken < 4;
	}

	public boolean isNight() {
		return !this.dimensionType().hasFixedTime() && !this.isDay();
	}

	@Override
	public void playSound(@Nullable Player player, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float g) {
		this.playSound(player, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, soundEvent, soundSource, f, g);
	}

	public abstract void playSound(@Nullable Player player, double d, double e, double f, SoundEvent soundEvent, SoundSource soundSource, float g, float h);

	public abstract void playSound(@Nullable Player player, Entity entity, SoundEvent soundEvent, SoundSource soundSource, float f, float g);

	public void playLocalSound(double d, double e, double f, SoundEvent soundEvent, SoundSource soundSource, float g, float h, boolean bl) {
	}

	@Override
	public void addParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i) {
	}

	@Environment(EnvType.CLIENT)
	public void addParticle(ParticleOptions particleOptions, boolean bl, double d, double e, double f, double g, double h, double i) {
	}

	public void addAlwaysVisibleParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i) {
	}

	public void addAlwaysVisibleParticle(ParticleOptions particleOptions, boolean bl, double d, double e, double f, double g, double h, double i) {
	}

	public float getSunAngle(float f) {
		float g = this.getTimeOfDay(f);
		return g * (float) (Math.PI * 2);
	}

	public boolean addBlockEntity(BlockEntity blockEntity) {
		if (this.updatingBlockEntities) {
			LOGGER.error("Adding block entity while ticking: {} @ {}", () -> Registry.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()), blockEntity::getBlockPos);
		}

		boolean bl = this.blockEntityList.add(blockEntity);
		if (bl && blockEntity instanceof TickableBlockEntity) {
			this.tickableBlockEntities.add(blockEntity);
		}

		if (this.isClientSide) {
			BlockPos blockPos = blockEntity.getBlockPos();
			BlockState blockState = this.getBlockState(blockPos);
			this.sendBlockUpdated(blockPos, blockState, blockState, 2);
		}

		return bl;
	}

	public void addAllPendingBlockEntities(Collection<BlockEntity> collection) {
		if (this.updatingBlockEntities) {
			this.pendingBlockEntities.addAll(collection);
		} else {
			for (BlockEntity blockEntity : collection) {
				this.addBlockEntity(blockEntity);
			}
		}
	}

	public void tickBlockEntities() {
		ProfilerFiller profilerFiller = this.getProfiler();
		profilerFiller.push("blockEntities");
		if (!this.blockEntitiesToUnload.isEmpty()) {
			this.tickableBlockEntities.removeAll(this.blockEntitiesToUnload);
			this.blockEntityList.removeAll(this.blockEntitiesToUnload);
			this.blockEntitiesToUnload.clear();
		}

		this.updatingBlockEntities = true;
		Iterator<BlockEntity> iterator = this.tickableBlockEntities.iterator();

		while (iterator.hasNext()) {
			BlockEntity blockEntity = (BlockEntity)iterator.next();
			if (!blockEntity.isRemoved() && blockEntity.hasLevel()) {
				BlockPos blockPos = blockEntity.getBlockPos();
				if (this.getChunkSource().isTickingChunk(blockPos) && this.getWorldBorder().isWithinBounds(blockPos)) {
					try {
						profilerFiller.push((Supplier<String>)(() -> String.valueOf(BlockEntityType.getKey(blockEntity.getType()))));
						if (blockEntity.getType().isValid(this.getBlockState(blockPos).getBlock())) {
							((TickableBlockEntity)blockEntity).tick();
						} else {
							blockEntity.logInvalidState();
						}

						profilerFiller.pop();
					} catch (Throwable var8) {
						CrashReport crashReport = CrashReport.forThrowable(var8, "Ticking block entity");
						CrashReportCategory crashReportCategory = crashReport.addCategory("Block entity being ticked");
						blockEntity.fillCrashReportCategory(crashReportCategory);
						throw new ReportedException(crashReport);
					}
				}
			}

			if (blockEntity.isRemoved()) {
				iterator.remove();
				this.blockEntityList.remove(blockEntity);
				if (this.hasChunkAt(blockEntity.getBlockPos())) {
					this.getChunkAt(blockEntity.getBlockPos()).removeBlockEntity(blockEntity.getBlockPos());
				}
			}
		}

		this.updatingBlockEntities = false;
		profilerFiller.popPush("pendingBlockEntities");
		if (!this.pendingBlockEntities.isEmpty()) {
			for (int i = 0; i < this.pendingBlockEntities.size(); i++) {
				BlockEntity blockEntity2 = (BlockEntity)this.pendingBlockEntities.get(i);
				if (!blockEntity2.isRemoved()) {
					if (!this.blockEntityList.contains(blockEntity2)) {
						this.addBlockEntity(blockEntity2);
					}

					if (this.hasChunkAt(blockEntity2.getBlockPos())) {
						LevelChunk levelChunk = this.getChunkAt(blockEntity2.getBlockPos());
						BlockState blockState = levelChunk.getBlockState(blockEntity2.getBlockPos());
						levelChunk.setBlockEntity(blockEntity2.getBlockPos(), blockEntity2);
						this.sendBlockUpdated(blockEntity2.getBlockPos(), blockState, blockState, 3);
					}
				}
			}

			this.pendingBlockEntities.clear();
		}

		profilerFiller.pop();
	}

	public void guardEntityTick(Consumer<Entity> consumer, Entity entity) {
		try {
			consumer.accept(entity);
		} catch (Throwable var6) {
			CrashReport crashReport = CrashReport.forThrowable(var6, "Ticking entity");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being ticked");
			entity.fillCrashReportCategory(crashReportCategory);
			throw new ReportedException(crashReport);
		}
	}

	public Explosion explode(@Nullable Entity entity, double d, double e, double f, float g, Explosion.BlockInteraction blockInteraction) {
		return this.explode(entity, null, null, d, e, f, g, false, blockInteraction);
	}

	public Explosion explode(@Nullable Entity entity, double d, double e, double f, float g, boolean bl, Explosion.BlockInteraction blockInteraction) {
		return this.explode(entity, null, null, d, e, f, g, bl, blockInteraction);
	}

	public Explosion explode(
		@Nullable Entity entity,
		@Nullable DamageSource damageSource,
		@Nullable ExplosionDamageCalculator explosionDamageCalculator,
		double d,
		double e,
		double f,
		float g,
		boolean bl,
		Explosion.BlockInteraction blockInteraction
	) {
		Explosion explosion = new Explosion(this, entity, damageSource, explosionDamageCalculator, d, e, f, g, bl, blockInteraction);
		explosion.explode();
		explosion.finalizeExplosion(true);
		return explosion;
	}

	@Environment(EnvType.CLIENT)
	public String gatherChunkSourceStats() {
		return this.getChunkSource().gatherStats();
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		if (isOutsideBuildHeight(blockPos)) {
			return null;
		} else if (!this.isClientSide && Thread.currentThread() != this.thread) {
			return null;
		} else {
			BlockEntity blockEntity = null;
			if (this.updatingBlockEntities) {
				blockEntity = this.getPendingBlockEntityAt(blockPos);
			}

			if (blockEntity == null) {
				blockEntity = this.getChunkAt(blockPos).getBlockEntity(blockPos, LevelChunk.EntityCreationType.IMMEDIATE);
			}

			if (blockEntity == null) {
				blockEntity = this.getPendingBlockEntityAt(blockPos);
			}

			return blockEntity;
		}
	}

	@Nullable
	private BlockEntity getPendingBlockEntityAt(BlockPos blockPos) {
		for (int i = 0; i < this.pendingBlockEntities.size(); i++) {
			BlockEntity blockEntity = (BlockEntity)this.pendingBlockEntities.get(i);
			if (!blockEntity.isRemoved() && blockEntity.getBlockPos().equals(blockPos)) {
				return blockEntity;
			}
		}

		return null;
	}

	public void setBlockEntity(BlockPos blockPos, @Nullable BlockEntity blockEntity) {
		if (!isOutsideBuildHeight(blockPos)) {
			if (blockEntity != null && !blockEntity.isRemoved()) {
				if (this.updatingBlockEntities) {
					blockEntity.setLevelAndPosition(this, blockPos);
					Iterator<BlockEntity> iterator = this.pendingBlockEntities.iterator();

					while (iterator.hasNext()) {
						BlockEntity blockEntity2 = (BlockEntity)iterator.next();
						if (blockEntity2.getBlockPos().equals(blockPos)) {
							blockEntity2.setRemoved();
							iterator.remove();
						}
					}

					this.pendingBlockEntities.add(blockEntity);
				} else {
					this.getChunkAt(blockPos).setBlockEntity(blockPos, blockEntity);
					this.addBlockEntity(blockEntity);
				}
			}
		}
	}

	public void removeBlockEntity(BlockPos blockPos) {
		BlockEntity blockEntity = this.getBlockEntity(blockPos);
		if (blockEntity != null && this.updatingBlockEntities) {
			blockEntity.setRemoved();
			this.pendingBlockEntities.remove(blockEntity);
		} else {
			if (blockEntity != null) {
				this.pendingBlockEntities.remove(blockEntity);
				this.blockEntityList.remove(blockEntity);
				this.tickableBlockEntities.remove(blockEntity);
			}

			this.getChunkAt(blockPos).removeBlockEntity(blockPos);
		}
	}

	public boolean isLoaded(BlockPos blockPos) {
		return isOutsideBuildHeight(blockPos) ? false : this.getChunkSource().hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
	}

	public boolean loadedAndEntityCanStandOnFace(BlockPos blockPos, Entity entity, Direction direction) {
		if (isOutsideBuildHeight(blockPos)) {
			return false;
		} else {
			ChunkAccess chunkAccess = this.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4, ChunkStatus.FULL, false);
			return chunkAccess == null ? false : chunkAccess.getBlockState(blockPos).entityCanStandOnFace(this, blockPos, entity, direction);
		}
	}

	public boolean loadedAndEntityCanStandOn(BlockPos blockPos, Entity entity) {
		return this.loadedAndEntityCanStandOnFace(blockPos, entity, Direction.UP);
	}

	public void updateSkyBrightness() {
		double d = 1.0 - (double)(this.getRainLevel(1.0F) * 5.0F) / 16.0;
		double e = 1.0 - (double)(this.getThunderLevel(1.0F) * 5.0F) / 16.0;
		double f = 0.5 + 2.0 * Mth.clamp((double)Mth.cos(this.getTimeOfDay(1.0F) * (float) (Math.PI * 2)), -0.25, 0.25);
		this.skyDarken = (int)((1.0 - f * d * e) * 11.0);
	}

	public void setSpawnSettings(boolean bl, boolean bl2) {
		this.getChunkSource().setSpawnSettings(bl, bl2);
	}

	protected void prepareWeather() {
		if (this.levelData.isRaining()) {
			this.rainLevel = 1.0F;
			if (this.levelData.isThundering()) {
				this.thunderLevel = 1.0F;
			}
		}
	}

	public void close() throws IOException {
		this.getChunkSource().close();
	}

	@Nullable
	@Override
	public BlockGetter getChunkForCollisions(int i, int j) {
		return this.getChunk(i, j, ChunkStatus.FULL, false);
	}

	@Override
	public List<Entity> getEntities(@Nullable Entity entity, AABB aABB, @Nullable Predicate<? super Entity> predicate) {
		this.getProfiler().incrementCounter("getEntities");
		List<Entity> list = Lists.<Entity>newArrayList();
		int i = Mth.floor((aABB.minX - 2.0) / 16.0);
		int j = Mth.floor((aABB.maxX + 2.0) / 16.0);
		int k = Mth.floor((aABB.minZ - 2.0) / 16.0);
		int l = Mth.floor((aABB.maxZ + 2.0) / 16.0);
		ChunkSource chunkSource = this.getChunkSource();

		for (int m = i; m <= j; m++) {
			for (int n = k; n <= l; n++) {
				LevelChunk levelChunk = chunkSource.getChunk(m, n, false);
				if (levelChunk != null) {
					levelChunk.getEntities(entity, aABB, list, predicate);
				}
			}
		}

		return list;
	}

	public <T extends Entity> List<T> getEntities(@Nullable EntityType<T> entityType, AABB aABB, Predicate<? super T> predicate) {
		this.getProfiler().incrementCounter("getEntities");
		int i = Mth.floor((aABB.minX - 2.0) / 16.0);
		int j = Mth.ceil((aABB.maxX + 2.0) / 16.0);
		int k = Mth.floor((aABB.minZ - 2.0) / 16.0);
		int l = Mth.ceil((aABB.maxZ + 2.0) / 16.0);
		List<T> list = Lists.<T>newArrayList();

		for (int m = i; m < j; m++) {
			for (int n = k; n < l; n++) {
				LevelChunk levelChunk = this.getChunkSource().getChunk(m, n, false);
				if (levelChunk != null) {
					levelChunk.getEntities(entityType, aABB, list, predicate);
				}
			}
		}

		return list;
	}

	@Override
	public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> class_, AABB aABB, @Nullable Predicate<? super T> predicate) {
		this.getProfiler().incrementCounter("getEntities");
		int i = Mth.floor((aABB.minX - 2.0) / 16.0);
		int j = Mth.ceil((aABB.maxX + 2.0) / 16.0);
		int k = Mth.floor((aABB.minZ - 2.0) / 16.0);
		int l = Mth.ceil((aABB.maxZ + 2.0) / 16.0);
		List<T> list = Lists.<T>newArrayList();
		ChunkSource chunkSource = this.getChunkSource();

		for (int m = i; m < j; m++) {
			for (int n = k; n < l; n++) {
				LevelChunk levelChunk = chunkSource.getChunk(m, n, false);
				if (levelChunk != null) {
					levelChunk.getEntitiesOfClass(class_, aABB, list, predicate);
				}
			}
		}

		return list;
	}

	@Override
	public <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> class_, AABB aABB, @Nullable Predicate<? super T> predicate) {
		this.getProfiler().incrementCounter("getLoadedEntities");
		int i = Mth.floor((aABB.minX - 2.0) / 16.0);
		int j = Mth.ceil((aABB.maxX + 2.0) / 16.0);
		int k = Mth.floor((aABB.minZ - 2.0) / 16.0);
		int l = Mth.ceil((aABB.maxZ + 2.0) / 16.0);
		List<T> list = Lists.<T>newArrayList();
		ChunkSource chunkSource = this.getChunkSource();

		for (int m = i; m < j; m++) {
			for (int n = k; n < l; n++) {
				LevelChunk levelChunk = chunkSource.getChunkNow(m, n);
				if (levelChunk != null) {
					levelChunk.getEntitiesOfClass(class_, aABB, list, predicate);
				}
			}
		}

		return list;
	}

	@Nullable
	public abstract Entity getEntity(int i);

	public void blockEntityChanged(BlockPos blockPos, BlockEntity blockEntity) {
		if (this.hasChunkAt(blockPos)) {
			this.getChunkAt(blockPos).markUnsaved();
		}
	}

	@Override
	public int getSeaLevel() {
		return 63;
	}

	public int getDirectSignalTo(BlockPos blockPos) {
		int i = 0;
		i = Math.max(i, this.getDirectSignal(blockPos.below(), Direction.DOWN));
		if (i >= 15) {
			return i;
		} else {
			i = Math.max(i, this.getDirectSignal(blockPos.above(), Direction.UP));
			if (i >= 15) {
				return i;
			} else {
				i = Math.max(i, this.getDirectSignal(blockPos.north(), Direction.NORTH));
				if (i >= 15) {
					return i;
				} else {
					i = Math.max(i, this.getDirectSignal(blockPos.south(), Direction.SOUTH));
					if (i >= 15) {
						return i;
					} else {
						i = Math.max(i, this.getDirectSignal(blockPos.west(), Direction.WEST));
						if (i >= 15) {
							return i;
						} else {
							i = Math.max(i, this.getDirectSignal(blockPos.east(), Direction.EAST));
							return i >= 15 ? i : i;
						}
					}
				}
			}
		}
	}

	public boolean hasSignal(BlockPos blockPos, Direction direction) {
		return this.getSignal(blockPos, direction) > 0;
	}

	public int getSignal(BlockPos blockPos, Direction direction) {
		BlockState blockState = this.getBlockState(blockPos);
		int i = blockState.getSignal(this, blockPos, direction);
		return blockState.isRedstoneConductor(this, blockPos) ? Math.max(i, this.getDirectSignalTo(blockPos)) : i;
	}

	public boolean hasNeighborSignal(BlockPos blockPos) {
		if (this.getSignal(blockPos.below(), Direction.DOWN) > 0) {
			return true;
		} else if (this.getSignal(blockPos.above(), Direction.UP) > 0) {
			return true;
		} else if (this.getSignal(blockPos.north(), Direction.NORTH) > 0) {
			return true;
		} else if (this.getSignal(blockPos.south(), Direction.SOUTH) > 0) {
			return true;
		} else {
			return this.getSignal(blockPos.west(), Direction.WEST) > 0 ? true : this.getSignal(blockPos.east(), Direction.EAST) > 0;
		}
	}

	public int getBestNeighborSignal(BlockPos blockPos) {
		int i = 0;

		for (Direction direction : DIRECTIONS) {
			int j = this.getSignal(blockPos.relative(direction), direction);
			if (j >= 15) {
				return 15;
			}

			if (j > i) {
				i = j;
			}
		}

		return i;
	}

	@Environment(EnvType.CLIENT)
	public void disconnect() {
	}

	public long getGameTime() {
		return this.levelData.getGameTime();
	}

	public long getDayTime() {
		return this.levelData.getDayTime();
	}

	public boolean mayInteract(Player player, BlockPos blockPos) {
		return true;
	}

	public void broadcastEntityEvent(Entity entity, byte b) {
	}

	public void blockEvent(BlockPos blockPos, Block block, int i, int j) {
		this.getBlockState(blockPos).triggerEvent(this, blockPos, i, j);
	}

	@Override
	public LevelData getLevelData() {
		return this.levelData;
	}

	public GameRules getGameRules() {
		return this.levelData.getGameRules();
	}

	public float getThunderLevel(float f) {
		return Mth.lerp(f, this.oThunderLevel, this.thunderLevel) * this.getRainLevel(f);
	}

	@Environment(EnvType.CLIENT)
	public void setThunderLevel(float f) {
		this.oThunderLevel = f;
		this.thunderLevel = f;
	}

	public float getRainLevel(float f) {
		return Mth.lerp(f, this.oRainLevel, this.rainLevel);
	}

	@Environment(EnvType.CLIENT)
	public void setRainLevel(float f) {
		this.oRainLevel = f;
		this.rainLevel = f;
	}

	public boolean isThundering() {
		return this.dimensionType().hasSkyLight() && !this.dimensionType().hasCeiling() ? (double)this.getThunderLevel(1.0F) > 0.9 : false;
	}

	public boolean isRaining() {
		return (double)this.getRainLevel(1.0F) > 0.2;
	}

	public boolean isRainingAt(BlockPos blockPos) {
		if (!this.isRaining()) {
			return false;
		} else if (!this.canSeeSky(blockPos)) {
			return false;
		} else if (this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > blockPos.getY()) {
			return false;
		} else {
			Biome biome = this.getBiome(blockPos);
			return biome.getPrecipitation() == Biome.Precipitation.RAIN && biome.getTemperature(blockPos) >= 0.15F;
		}
	}

	public boolean isHumidAt(BlockPos blockPos) {
		Biome biome = this.getBiome(blockPos);
		return biome.isHumid();
	}

	@Nullable
	public abstract MapItemSavedData getMapData(String string);

	public abstract void setMapData(MapItemSavedData mapItemSavedData);

	public abstract int getFreeMapId();

	public void globalLevelEvent(int i, BlockPos blockPos, int j) {
	}

	public CrashReportCategory fillReportDetails(CrashReport crashReport) {
		CrashReportCategory crashReportCategory = crashReport.addCategory("Affected level", 1);
		crashReportCategory.setDetail("All players", (CrashReportDetail<String>)(() -> this.players().size() + " total; " + this.players()));
		crashReportCategory.setDetail("Chunk stats", this.getChunkSource()::gatherStats);
		crashReportCategory.setDetail("Level dimension", (CrashReportDetail<String>)(() -> this.dimension().location().toString()));

		try {
			this.levelData.fillCrashReportCategory(crashReportCategory);
		} catch (Throwable var4) {
			crashReportCategory.setDetailError("Level Data Unobtainable", var4);
		}

		return crashReportCategory;
	}

	public abstract void destroyBlockProgress(int i, BlockPos blockPos, int j);

	@Environment(EnvType.CLIENT)
	public void createFireworks(double d, double e, double f, double g, double h, double i, @Nullable CompoundTag compoundTag) {
	}

	public abstract Scoreboard getScoreboard();

	public void updateNeighbourForOutputSignal(BlockPos blockPos, Block block) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos blockPos2 = blockPos.relative(direction);
			if (this.hasChunkAt(blockPos2)) {
				BlockState blockState = this.getBlockState(blockPos2);
				if (blockState.is(Blocks.COMPARATOR)) {
					blockState.neighborChanged(this, blockPos2, block, blockPos, false);
				} else if (blockState.isRedstoneConductor(this, blockPos2)) {
					blockPos2 = blockPos2.relative(direction);
					blockState = this.getBlockState(blockPos2);
					if (blockState.is(Blocks.COMPARATOR)) {
						blockState.neighborChanged(this, blockPos2, block, blockPos, false);
					}
				}
			}
		}
	}

	@Override
	public DifficultyInstance getCurrentDifficultyAt(BlockPos blockPos) {
		long l = 0L;
		float f = 0.0F;
		if (this.hasChunkAt(blockPos)) {
			f = this.getMoonBrightness();
			l = this.getChunkAt(blockPos).getInhabitedTime();
		}

		return new DifficultyInstance(this.getDifficulty(), this.getDayTime(), l, f);
	}

	@Override
	public int getSkyDarken() {
		return this.skyDarken;
	}

	public void setSkyFlashTime(int i) {
	}

	@Override
	public WorldBorder getWorldBorder() {
		return this.worldBorder;
	}

	public void sendPacketToServer(Packet<?> packet) {
		throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
	}

	@Override
	public DimensionType dimensionType() {
		return this.dimensionType;
	}

	public ResourceKey<Level> dimension() {
		return this.dimension;
	}

	@Override
	public Random getRandom() {
		return this.random;
	}

	@Override
	public boolean isStateAtPosition(BlockPos blockPos, Predicate<BlockState> predicate) {
		return predicate.test(this.getBlockState(blockPos));
	}

	public abstract RecipeManager getRecipeManager();

	public abstract TagContainer getTagManager();

	public BlockPos getBlockRandomPos(int i, int j, int k, int l) {
		this.randValue = this.randValue * 3 + 1013904223;
		int m = this.randValue >> 2;
		return new BlockPos(i + (m & 15), j + (m >> 16 & l), k + (m >> 8 & 15));
	}

	public boolean noSave() {
		return false;
	}

	public ProfilerFiller getProfiler() {
		return (ProfilerFiller)this.profiler.get();
	}

	public Supplier<ProfilerFiller> getProfilerSupplier() {
		return this.profiler;
	}

	@Override
	public BiomeManager getBiomeManager() {
		return this.biomeManager;
	}

	public final boolean isDebug() {
		return this.isDebug;
	}
}
