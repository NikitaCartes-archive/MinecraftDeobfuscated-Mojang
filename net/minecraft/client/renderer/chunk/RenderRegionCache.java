/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RenderRegionCache {
    private final Long2ObjectMap<ChunkInfo> chunkInfoCache = new Long2ObjectOpenHashMap<ChunkInfo>();

    @Nullable
    public RenderChunkRegion createRegion(Level level, BlockPos blockPos, BlockPos blockPos2, int i) {
        int o;
        int j = SectionPos.blockToSectionCoord(blockPos.getX() - i);
        int k = SectionPos.blockToSectionCoord(blockPos.getZ() - i);
        int l2 = SectionPos.blockToSectionCoord(blockPos2.getX() + i);
        int m = SectionPos.blockToSectionCoord(blockPos2.getZ() + i);
        ChunkInfo[][] chunkInfos = new ChunkInfo[l2 - j + 1][m - k + 1];
        for (int n = j; n <= l2; ++n) {
            for (o = k; o <= m; ++o) {
                chunkInfos[n - j][o - k] = this.chunkInfoCache.computeIfAbsent(ChunkPos.asLong(n, o), l -> new ChunkInfo(level.getChunk(ChunkPos.getX(l), ChunkPos.getZ(l))));
            }
        }
        if (RenderRegionCache.isAllEmpty(blockPos, blockPos2, j, k, chunkInfos)) {
            return null;
        }
        RenderChunk[][] renderChunks = new RenderChunk[l2 - j + 1][m - k + 1];
        for (o = j; o <= l2; ++o) {
            for (int p = k; p <= m; ++p) {
                renderChunks[o - j][p - k] = chunkInfos[o - j][p - k].renderChunk();
            }
        }
        return new RenderChunkRegion(level, j, k, renderChunks);
    }

    private static boolean isAllEmpty(BlockPos blockPos, BlockPos blockPos2, int i, int j, ChunkInfo[][] chunkInfos) {
        int k = SectionPos.blockToSectionCoord(blockPos.getX());
        int l = SectionPos.blockToSectionCoord(blockPos.getZ());
        int m = SectionPos.blockToSectionCoord(blockPos2.getX());
        int n = SectionPos.blockToSectionCoord(blockPos2.getZ());
        for (int o = k; o <= m; ++o) {
            for (int p = l; p <= n; ++p) {
                LevelChunk levelChunk = chunkInfos[o - i][p - j].chunk();
                if (levelChunk.isYSpaceEmpty(blockPos.getY(), blockPos2.getY())) continue;
                return false;
            }
        }
        return true;
    }

    @Environment(value=EnvType.CLIENT)
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

