package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BulkSectionAccess implements AutoCloseable {
	private final LevelAccessor level;
	private final Long2ObjectMap<LevelChunkSection> acquiredSections = new Long2ObjectOpenHashMap<>();
	@Nullable
	private LevelChunkSection lastSection;
	private long lastSectionKey;

	public BulkSectionAccess(LevelAccessor levelAccessor) {
		this.level = levelAccessor;
	}

	@Nullable
	public LevelChunkSection getSection(BlockPos blockPos) {
		int i = this.level.getSectionIndex(blockPos.getY());
		if (i >= 0 && i < this.level.getSectionsCount()) {
			long l = SectionPos.asLong(blockPos);
			if (this.lastSection == null || this.lastSectionKey != l) {
				this.lastSection = this.acquiredSections.computeIfAbsent(l, lx -> {
					ChunkAccess chunkAccess = this.level.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
					LevelChunkSection levelChunkSection = chunkAccess.getSection(i);
					levelChunkSection.acquire();
					return levelChunkSection;
				});
				this.lastSectionKey = l;
			}

			return this.lastSection;
		} else {
			return null;
		}
	}

	public BlockState getBlockState(BlockPos blockPos) {
		LevelChunkSection levelChunkSection = this.getSection(blockPos);
		if (levelChunkSection == null) {
			return Blocks.AIR.defaultBlockState();
		} else {
			int i = SectionPos.sectionRelative(blockPos.getX());
			int j = SectionPos.sectionRelative(blockPos.getY());
			int k = SectionPos.sectionRelative(blockPos.getZ());
			return levelChunkSection.getBlockState(i, j, k);
		}
	}

	public void close() {
		for (LevelChunkSection levelChunkSection : this.acquiredSections.values()) {
			levelChunkSection.release();
		}
	}
}
