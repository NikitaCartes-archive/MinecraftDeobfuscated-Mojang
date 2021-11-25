package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

@Environment(EnvType.CLIENT)
public class RenderRegionCache {
	private final Long2ObjectMap<RenderRegionCache.ChunkInfo> chunkInfoCache = new Long2ObjectOpenHashMap<>();

	@Nullable
	public RenderChunkRegion createRegion(Level level, BlockPos blockPos, BlockPos blockPos2, int i) {
		int j = SectionPos.blockToSectionCoord(blockPos.getX() - i);
		int k = SectionPos.blockToSectionCoord(blockPos.getZ() - i);
		int l = SectionPos.blockToSectionCoord(blockPos2.getX() + i);
		int m = SectionPos.blockToSectionCoord(blockPos2.getZ() + i);
		RenderRegionCache.ChunkInfo[][] chunkInfos = new RenderRegionCache.ChunkInfo[l - j + 1][m - k + 1];

		for (int n = j; n <= l; n++) {
			for (int o = k; o <= m; o++) {
				chunkInfos[n - j][o - k] = this.chunkInfoCache
					.computeIfAbsent(
						ChunkPos.asLong(n, o),
						(Long2ObjectFunction<? extends RenderRegionCache.ChunkInfo>)(lx -> new RenderRegionCache.ChunkInfo(level.getChunk(ChunkPos.getX(lx), ChunkPos.getZ(lx))))
					);
			}
		}

		if (isAllEmpty(blockPos, blockPos2, j, k, chunkInfos)) {
			return null;
		} else {
			RenderChunk[][] renderChunks = new RenderChunk[l - j + 1][m - k + 1];

			for (int o = j; o <= l; o++) {
				for (int p = k; p <= m; p++) {
					renderChunks[o - j][p - k] = chunkInfos[o - j][p - k].renderChunk();
				}
			}

			return new RenderChunkRegion(level, j, k, renderChunks);
		}
	}

	private static boolean isAllEmpty(BlockPos blockPos, BlockPos blockPos2, int i, int j, RenderRegionCache.ChunkInfo[][] chunkInfos) {
		int k = SectionPos.blockToSectionCoord(blockPos.getX());
		int l = SectionPos.blockToSectionCoord(blockPos.getZ());
		int m = SectionPos.blockToSectionCoord(blockPos2.getX());
		int n = SectionPos.blockToSectionCoord(blockPos2.getZ());

		for (int o = k; o <= m; o++) {
			for (int p = l; p <= n; p++) {
				LevelChunk levelChunk = chunkInfos[o - i][p - j].chunk();
				if (!levelChunk.isYSpaceEmpty(blockPos.getY(), blockPos2.getY())) {
					return false;
				}
			}
		}

		return true;
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
