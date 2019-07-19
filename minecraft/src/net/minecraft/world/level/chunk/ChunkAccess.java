package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;

public interface ChunkAccess extends FeatureAccess {
	@Nullable
	BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl);

	void setBlockEntity(BlockPos blockPos, BlockEntity blockEntity);

	void addEntity(Entity entity);

	@Nullable
	default LevelChunkSection getHighestSection() {
		LevelChunkSection[] levelChunkSections = this.getSections();

		for (int i = levelChunkSections.length - 1; i >= 0; i--) {
			LevelChunkSection levelChunkSection = levelChunkSections[i];
			if (!LevelChunkSection.isEmpty(levelChunkSection)) {
				return levelChunkSection;
			}
		}

		return null;
	}

	default int getHighestSectionPosition() {
		LevelChunkSection levelChunkSection = this.getHighestSection();
		return levelChunkSection == null ? 0 : levelChunkSection.bottomBlockY();
	}

	Set<BlockPos> getBlockEntitiesPos();

	LevelChunkSection[] getSections();

	@Nullable
	LevelLightEngine getLightEngine();

	default int getRawBrightness(BlockPos blockPos, int i, boolean bl) {
		LevelLightEngine levelLightEngine = this.getLightEngine();
		if (levelLightEngine != null && this.getStatus().isOrAfter(ChunkStatus.LIGHT)) {
			int j = bl ? levelLightEngine.getLayerListener(LightLayer.SKY).getLightValue(blockPos) - i : 0;
			int k = levelLightEngine.getLayerListener(LightLayer.BLOCK).getLightValue(blockPos);
			return Math.max(k, j);
		} else {
			return 0;
		}
	}

	Collection<Entry<Heightmap.Types, Heightmap>> getHeightmaps();

	void setHeightmap(Heightmap.Types types, long[] ls);

	Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types types);

	int getHeight(Heightmap.Types types, int i, int j);

	ChunkPos getPos();

	void setLastSaveTime(long l);

	Map<String, StructureStart> getAllStarts();

	void setAllStarts(Map<String, StructureStart> map);

	default Biome getBiome(BlockPos blockPos) {
		int i = blockPos.getX() & 15;
		int j = blockPos.getZ() & 15;
		return this.getBiomes()[j << 4 | i];
	}

	default boolean isYSpaceEmpty(int i, int j) {
		if (i < 0) {
			i = 0;
		}

		if (j >= 256) {
			j = 255;
		}

		for (int k = i; k <= j; k += 16) {
			if (!LevelChunkSection.isEmpty(this.getSections()[k >> 4])) {
				return false;
			}
		}

		return true;
	}

	Biome[] getBiomes();

	void setUnsaved(boolean bl);

	boolean isUnsaved();

	ChunkStatus getStatus();

	void removeBlockEntity(BlockPos blockPos);

	void setLightEngine(LevelLightEngine levelLightEngine);

	default void markPosForPostprocessing(BlockPos blockPos) {
		LogManager.getLogger().warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", blockPos);
	}

	ShortList[] getPostProcessing();

	default void addPackedPostProcess(short s, int i) {
		getOrCreateOffsetList(this.getPostProcessing(), i).add(s);
	}

	default void setBlockEntityNbt(CompoundTag compoundTag) {
		LogManager.getLogger().warn("Trying to set a BlockEntity, but this operation is not supported.");
	}

	@Nullable
	CompoundTag getBlockEntityNbt(BlockPos blockPos);

	@Nullable
	CompoundTag getBlockEntityNbtForSaving(BlockPos blockPos);

	default void setBiomes(Biome[] biomes) {
		throw new UnsupportedOperationException();
	}

	Stream<BlockPos> getLights();

	TickList<Block> getBlockTicks();

	TickList<Fluid> getLiquidTicks();

	default BitSet getCarvingMask(GenerationStep.Carving carving) {
		throw new RuntimeException("Meaningless in this context");
	}

	UpgradeData getUpgradeData();

	void setInhabitedTime(long l);

	long getInhabitedTime();

	static ShortList getOrCreateOffsetList(ShortList[] shortLists, int i) {
		if (shortLists[i] == null) {
			shortLists[i] = new ShortArrayList();
		}

		return shortLists[i];
	}

	boolean isLightCorrect();

	void setLightCorrect(boolean bl);
}
