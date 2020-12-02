package net.minecraft.server.level;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenRegion implements WorldGenLevel {
	private static final Logger LOGGER = LogManager.getLogger();
	private final List<ChunkAccess> cache;
	private final int x;
	private final int z;
	private final int size;
	private final ServerLevel level;
	private final long seed;
	private final LevelData levelData;
	private final Random random;
	private final DimensionType dimensionType;
	private final TickList<Block> blockTicks = new WorldGenTickList<>(blockPos -> this.getChunk(blockPos).getBlockTicks());
	private final TickList<Fluid> liquidTicks = new WorldGenTickList<>(blockPos -> this.getChunk(blockPos).getLiquidTicks());
	private final BiomeManager biomeManager;
	private final ChunkPos firstPos;
	private final ChunkPos lastPos;
	private final StructureFeatureManager structureFeatureManager;

	public WorldGenRegion(ServerLevel serverLevel, List<ChunkAccess> list) {
		int i = Mth.floor(Math.sqrt((double)list.size()));
		if (i * i != list.size()) {
			throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Cache size is not a square."));
		} else {
			ChunkPos chunkPos = ((ChunkAccess)list.get(list.size() / 2)).getPos();
			this.cache = list;
			this.x = chunkPos.x;
			this.z = chunkPos.z;
			this.size = i;
			this.level = serverLevel;
			this.seed = serverLevel.getSeed();
			this.levelData = serverLevel.getLevelData();
			this.random = serverLevel.getRandom();
			this.dimensionType = serverLevel.dimensionType();
			this.biomeManager = new BiomeManager(this, BiomeManager.obfuscateSeed(this.seed), serverLevel.dimensionType().getBiomeZoomer());
			this.firstPos = ((ChunkAccess)list.get(0)).getPos();
			this.lastPos = ((ChunkAccess)list.get(list.size() - 1)).getPos();
			this.structureFeatureManager = serverLevel.structureFeatureManager().forWorldGenRegion(this);
		}
	}

	public int getCenterX() {
		return this.x;
	}

	public int getCenterZ() {
		return this.z;
	}

	@Override
	public ChunkAccess getChunk(int i, int j) {
		return this.getChunk(i, j, ChunkStatus.EMPTY);
	}

	@Nullable
	@Override
	public ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl) {
		ChunkAccess chunkAccess;
		if (this.hasChunk(i, j)) {
			int k = i - this.firstPos.x;
			int l = j - this.firstPos.z;
			chunkAccess = (ChunkAccess)this.cache.get(k + l * this.size);
			if (chunkAccess.getStatus().isOrAfter(chunkStatus)) {
				return chunkAccess;
			}
		} else {
			chunkAccess = null;
		}

		if (!bl) {
			return null;
		} else {
			LOGGER.error("Requested chunk : {} {}", i, j);
			LOGGER.error("Region bounds : {} {} | {} {}", this.firstPos.x, this.firstPos.z, this.lastPos.x, this.lastPos.z);
			if (chunkAccess != null) {
				throw (RuntimeException)Util.pauseInIde(
					new RuntimeException(String.format("Chunk is not of correct status. Expecting %s, got %s | %s %s", chunkStatus, chunkAccess.getStatus(), i, j))
				);
			} else {
				throw (RuntimeException)Util.pauseInIde(new RuntimeException(String.format("We are asking a region for a chunk out of bound | %s %s", i, j)));
			}
		}
	}

	@Override
	public boolean hasChunk(int i, int j) {
		return i >= this.firstPos.x && i <= this.lastPos.x && j >= this.firstPos.z && j <= this.lastPos.z;
	}

	@Override
	public BlockState getBlockState(BlockPos blockPos) {
		return this.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ())).getBlockState(blockPos);
	}

	@Override
	public FluidState getFluidState(BlockPos blockPos) {
		return this.getChunk(blockPos).getFluidState(blockPos);
	}

	@Nullable
	@Override
	public Player getNearestPlayer(double d, double e, double f, double g, Predicate<Entity> predicate) {
		return null;
	}

	@Override
	public int getSkyDarken() {
		return 0;
	}

	@Override
	public BiomeManager getBiomeManager() {
		return this.biomeManager;
	}

	@Override
	public Biome getUncachedNoiseBiome(int i, int j, int k) {
		return this.level.getUncachedNoiseBiome(i, j, k);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public float getShade(Direction direction, boolean bl) {
		return 1.0F;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return this.level.getLightEngine();
	}

	@Override
	public boolean destroyBlock(BlockPos blockPos, boolean bl, @Nullable Entity entity, int i) {
		BlockState blockState = this.getBlockState(blockPos);
		if (blockState.isAir()) {
			return false;
		} else {
			if (bl) {
				BlockEntity blockEntity = blockState.hasBlockEntity() ? this.getBlockEntity(blockPos) : null;
				Block.dropResources(blockState, this.level, blockPos, blockEntity, entity, ItemStack.EMPTY);
			}

			return this.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3, i);
		}
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		ChunkAccess chunkAccess = this.getChunk(blockPos);
		BlockEntity blockEntity = chunkAccess.getBlockEntity(blockPos);
		if (blockEntity != null) {
			return blockEntity;
		} else {
			CompoundTag compoundTag = chunkAccess.getBlockEntityNbt(blockPos);
			BlockState blockState = chunkAccess.getBlockState(blockPos);
			if (compoundTag != null) {
				if ("DUMMY".equals(compoundTag.getString("id"))) {
					if (!blockState.hasBlockEntity()) {
						return null;
					}

					blockEntity = ((EntityBlock)blockState.getBlock()).newBlockEntity(blockPos, blockState);
				} else {
					blockEntity = BlockEntity.loadStatic(blockPos, blockState, compoundTag);
				}

				if (blockEntity != null) {
					chunkAccess.setBlockEntity(blockEntity);
					return blockEntity;
				}
			}

			if (blockState.hasBlockEntity()) {
				LOGGER.warn("Tried to access a block entity before it was created. {}", blockPos);
			}

			return null;
		}
	}

	@Override
	public boolean setBlock(BlockPos blockPos, BlockState blockState, int i, int j) {
		ChunkAccess chunkAccess = this.getChunk(blockPos);
		BlockState blockState2 = chunkAccess.setBlockState(blockPos, blockState, false);
		if (blockState2 != null) {
			this.level.onBlockStateChange(blockPos, blockState2, blockState);
		}

		if (blockState.hasBlockEntity()) {
			if (chunkAccess.getStatus().getChunkType() == ChunkStatus.ChunkType.LEVELCHUNK) {
				BlockEntity blockEntity = ((EntityBlock)blockState.getBlock()).newBlockEntity(blockPos, blockState);
				if (blockEntity != null) {
					chunkAccess.setBlockEntity(blockEntity);
				} else {
					chunkAccess.removeBlockEntity(blockPos);
				}
			} else {
				CompoundTag compoundTag = new CompoundTag();
				compoundTag.putInt("x", blockPos.getX());
				compoundTag.putInt("y", blockPos.getY());
				compoundTag.putInt("z", blockPos.getZ());
				compoundTag.putString("id", "DUMMY");
				chunkAccess.setBlockEntityNbt(compoundTag);
			}
		} else if (blockState2 != null && blockState2.hasBlockEntity()) {
			chunkAccess.removeBlockEntity(blockPos);
		}

		if (blockState.hasPostProcess(this, blockPos)) {
			this.markPosForPostprocessing(blockPos);
		}

		return true;
	}

	private void markPosForPostprocessing(BlockPos blockPos) {
		this.getChunk(blockPos).markPosForPostprocessing(blockPos);
	}

	@Override
	public boolean addFreshEntity(Entity entity) {
		int i = SectionPos.blockToSectionCoord(entity.getBlockX());
		int j = SectionPos.blockToSectionCoord(entity.getBlockZ());
		this.getChunk(i, j).addEntity(entity);
		return true;
	}

	@Override
	public boolean removeBlock(BlockPos blockPos, boolean bl) {
		return this.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
	}

	@Override
	public WorldBorder getWorldBorder() {
		return this.level.getWorldBorder();
	}

	@Override
	public boolean isClientSide() {
		return false;
	}

	@Deprecated
	@Override
	public ServerLevel getLevel() {
		return this.level;
	}

	@Override
	public RegistryAccess registryAccess() {
		return this.level.registryAccess();
	}

	@Override
	public LevelData getLevelData() {
		return this.levelData;
	}

	@Override
	public DifficultyInstance getCurrentDifficultyAt(BlockPos blockPos) {
		if (!this.hasChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()))) {
			throw new RuntimeException("We are asking a region for a chunk out of bound");
		} else {
			return new DifficultyInstance(this.level.getDifficulty(), this.level.getDayTime(), 0L, this.level.getMoonBrightness());
		}
	}

	@Override
	public ChunkSource getChunkSource() {
		return this.level.getChunkSource();
	}

	@Override
	public long getSeed() {
		return this.seed;
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
	public int getSeaLevel() {
		return this.level.getSeaLevel();
	}

	@Override
	public Random getRandom() {
		return this.random;
	}

	@Override
	public int getHeight(Heightmap.Types types, int i, int j) {
		return this.getChunk(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(j)).getHeight(types, i & 15, j & 15) + 1;
	}

	@Override
	public void playSound(@Nullable Player player, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float g) {
	}

	@Override
	public void addParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i) {
	}

	@Override
	public void levelEvent(@Nullable Player player, int i, BlockPos blockPos, int j) {
	}

	@Override
	public void gameEvent(@Nullable Entity entity, GameEvent gameEvent, BlockPos blockPos) {
	}

	@Override
	public DimensionType dimensionType() {
		return this.dimensionType;
	}

	@Override
	public boolean isStateAtPosition(BlockPos blockPos, Predicate<BlockState> predicate) {
		return predicate.test(this.getBlockState(blockPos));
	}

	@Override
	public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB aABB, Predicate<? super T> predicate) {
		return Collections.emptyList();
	}

	@Override
	public List<Entity> getEntities(@Nullable Entity entity, AABB aABB, @Nullable Predicate<? super Entity> predicate) {
		return Collections.emptyList();
	}

	@Override
	public List<Player> players() {
		return Collections.emptyList();
	}

	@Override
	public Stream<? extends StructureStart<?>> startsForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature) {
		return this.structureFeatureManager.startsForFeature(sectionPos, structureFeature);
	}

	@Override
	public int getMinBuildHeight() {
		return this.level.getMinBuildHeight();
	}

	@Override
	public int getHeight() {
		return this.level.getHeight();
	}
}
