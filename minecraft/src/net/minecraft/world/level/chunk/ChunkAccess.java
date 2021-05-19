package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;

public interface ChunkAccess extends BlockGetter, FeatureAccess {
	default GameEventDispatcher getEventDispatcher(int i) {
		return GameEventDispatcher.NOOP;
	}

	@Nullable
	BlockState setBlockState(BlockPos blockPos, BlockState blockState, boolean bl);

	void setBlockEntity(BlockEntity blockEntity);

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
		return levelChunkSection == null ? this.getMinBuildHeight() : levelChunkSection.bottomBlockY();
	}

	Set<BlockPos> getBlockEntitiesPos();

	LevelChunkSection[] getSections();

	default LevelChunkSection getOrCreateSection(int i) {
		LevelChunkSection[] levelChunkSections = this.getSections();
		if (levelChunkSections[i] == LevelChunk.EMPTY_SECTION) {
			levelChunkSections[i] = new LevelChunkSection(this.getSectionYFromSectionIndex(i));
		}

		return levelChunkSections[i];
	}

	Collection<Entry<Heightmap.Types, Heightmap>> getHeightmaps();

	default void setHeightmap(Heightmap.Types types, long[] ls) {
		this.getOrCreateHeightmapUnprimed(types).setRawData(this, types, ls);
	}

	Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types types);

	int getHeight(Heightmap.Types types, int i, int j);

	BlockPos getHeighestPosition(Heightmap.Types types);

	ChunkPos getPos();

	Map<StructureFeature<?>, StructureStart<?>> getAllStarts();

	void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> map);

	default boolean isYSpaceEmpty(int i, int j) {
		if (i < this.getMinBuildHeight()) {
			i = this.getMinBuildHeight();
		}

		if (j >= this.getMaxBuildHeight()) {
			j = this.getMaxBuildHeight() - 1;
		}

		for (int k = i; k <= j; k += 16) {
			if (!LevelChunkSection.isEmpty(this.getSections()[this.getSectionIndex(k)])) {
				return false;
			}
		}

		return true;
	}

	@Nullable
	ChunkBiomeContainer getBiomes();

	void setUnsaved(boolean bl);

	boolean isUnsaved();

	ChunkStatus getStatus();

	void removeBlockEntity(BlockPos blockPos);

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

	Stream<BlockPos> getLights();

	TickList<Block> getBlockTicks();

	TickList<Fluid> getLiquidTicks();

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
