/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jetbrains.annotations.Nullable;

public class BulkSectionAccess
implements AutoCloseable {
    private final LevelAccessor level;
    private final Long2ObjectMap<LevelChunkSection> acquiredSections = new Long2ObjectOpenHashMap<LevelChunkSection>();
    @Nullable
    private LevelChunkSection lastSection;
    private long lastSectionKey;

    public BulkSectionAccess(LevelAccessor levelAccessor) {
        this.level = levelAccessor;
    }

    public LevelChunkSection getSection(BlockPos blockPos) {
        long l2 = SectionPos.asLong(blockPos);
        if (this.lastSection != null && this.lastSectionKey == l2) {
            return this.lastSection;
        }
        this.lastSection = this.acquiredSections.computeIfAbsent(l2, l -> {
            ChunkAccess chunkAccess = this.level.getChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
            LevelChunkSection levelChunkSection = chunkAccess.getOrCreateSection(chunkAccess.getSectionIndex(blockPos.getY()));
            levelChunkSection.acquire();
            return levelChunkSection;
        });
        this.lastSectionKey = l2;
        return this.lastSection;
    }

    public BlockState getBlockState(BlockPos blockPos) {
        LevelChunkSection levelChunkSection = this.getSection(blockPos);
        int i = SectionPos.sectionRelative(blockPos.getX());
        int j = SectionPos.sectionRelative(blockPos.getY());
        int k = SectionPos.sectionRelative(blockPos.getZ());
        return levelChunkSection.getBlockState(i, j, k);
    }

    @Override
    public void close() {
        for (LevelChunkSection levelChunkSection : this.acquiredSections.values()) {
            levelChunkSection.release();
        }
    }
}

