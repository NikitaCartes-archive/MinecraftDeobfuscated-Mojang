package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

@Environment(EnvType.CLIENT)
public class RenderRegionCache {
	private final Long2ObjectMap<RenderRegionCache.ChunkInfo> chunkInfoCache = new Long2ObjectOpenHashMap<>();

	@Nullable
	public RenderChunkRegion createRegion(Level level, SectionPos sectionPos) {
		RenderRegionCache.ChunkInfo chunkInfo = this.getChunkInfo(level, sectionPos.x(), sectionPos.z());
		if (chunkInfo.chunk().isSectionEmpty(sectionPos.y())) {
			return null;
		} else {
			int i = sectionPos.x() - 1;
			int j = sectionPos.z() - 1;
			int k = sectionPos.x() + 1;
			int l = sectionPos.z() + 1;
			RenderChunk[] renderChunks = new RenderChunk[9];

			for (int m = j; m <= l; m++) {
				for (int n = i; n <= k; n++) {
					int o = RenderChunkRegion.index(i, j, n, m);
					RenderRegionCache.ChunkInfo chunkInfo2 = n == sectionPos.x() && m == sectionPos.z() ? chunkInfo : this.getChunkInfo(level, n, m);
					renderChunks[o] = chunkInfo2.renderChunk();
				}
			}

			return new RenderChunkRegion(level, i, j, renderChunks);
		}
	}

	private RenderRegionCache.ChunkInfo getChunkInfo(Level level, int i, int j) {
		return this.chunkInfoCache
			.computeIfAbsent(
				ChunkPos.asLong(i, j),
				(Long2ObjectFunction<? extends RenderRegionCache.ChunkInfo>)(l -> new RenderRegionCache.ChunkInfo(level.getChunk(ChunkPos.getX(l), ChunkPos.getZ(l))))
			);
	}

	@Environment(EnvType.CLIENT)
	static final class ChunkInfo {
		private final LevelChunk chunk;
		@Nullable
		private RenderChunk renderChunk;

		ChunkInfo(LevelChunk levelChunk) {
			this.chunk = levelChunk;
		}

		public LevelChunk chunk() {
			return this.chunk;
		}

		public RenderChunk renderChunk() {
			if (this.renderChunk == null) {
				this.renderChunk = new RenderChunk(this.chunk);
			}

			return this.renderChunk;
		}
	}
}
